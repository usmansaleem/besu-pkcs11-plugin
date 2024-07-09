// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
package info.usmans.besu.plugin.softhsm;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.NoSuchElementException;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DLSequence;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModuleException;
import org.hyperledger.besu.plugin.services.securitymodule.data.Signature;

/** Helper class to provide signature utility methods. */
public class SignatureUtil {
  private static final BigInteger SECP_256K1_CURVE_ORDER =
      new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
  private static final BigInteger HALF_CURVE_ORDER = SECP_256K1_CURVE_ORDER.shiftRight(1);

  /**
   * Uses Bouncycastle to decode DER signature. A DER signature format is SEQUENCE := {r INTEGER, s
   * INTEGER}
   *
   * @param der DER encoded byte[]
   * @return Array of BigInteger containing R and S.
   */
  public static Signature extractRAndSFromDERSignature(final byte[] der) {
    try (final ASN1InputStream asn1InputStream = new ASN1InputStream(der)) {
      final DLSequence seq = (DLSequence) asn1InputStream.readObject();
      if (seq == null) {
        throw new SecurityModuleException("Unexpected end of ASN.1 stream.");
      }

      final ASN1Integer r = (ASN1Integer) seq.getObjectAt(0);
      final ASN1Integer s = (ASN1Integer) seq.getObjectAt(1);

      return canonicalizeSignature(r.getPositiveValue(), s.getPositiveValue());
    } catch (final Exception e) {
      throw new SecurityModuleException(e);
    }
  }

  private static SignatureImpl canonicalizeSignature(final BigInteger r, final BigInteger s) {
    // Check if R is within the valid range
    if (r.signum() <= 0 || r.compareTo(SECP_256K1_CURVE_ORDER) >= 0) {
      throw new SecurityModuleException("Invalid signature: R is out of range");
    }

    // Check if S is in the upper half of the curve order
    // If S is in the upper half, subtract it from the curve order
    final BigInteger sCopy =
        s.compareTo(HALF_CURVE_ORDER) > 0 ? SECP_256K1_CURVE_ORDER.subtract(s) : s;

    // Final check to ensure S is still in the valid range after canonicalization
    if (sCopy.signum() <= 0 || sCopy.compareTo(SECP_256K1_CURVE_ORDER) >= 0) {
      throw new SecurityModuleException(
          "Invalid signature: S is out of range after canonicalization");
    }

    return new SignatureImpl(r, sCopy);
  }

  /**
   * Converts ECPoint to PublicKey using PKCS11 provider.
   *
   * @param theirECPoint ECPoint of other party
   * @param pkcs11Provider PKCS11 provider
   * @return PublicKey of other party generated from ECPoint
   * @throws SecurityModuleException wrapping cause of original exception =
   */
  public static PublicKey eCPointToPublicKey(
      final ECPoint theirECPoint, final Provider pkcs11Provider) throws SecurityModuleException {
    try {
      return KeyFactory.getInstance("EC", pkcs11Provider)
          .generatePublic(new ECPublicKeySpec(theirECPoint, SECP256K1.SECP256K1_PARAM_SPEC));
    } catch (InvalidKeySpecException | NoSuchElementException | NoSuchAlgorithmException e) {
      throw new SecurityModuleException("Unexpected error converting ECPoint to PublicKey", e);
    }
  }

  /** Static inner class to represent a signature. */
  static class SignatureImpl implements Signature {
    private final BigInteger r;
    private final BigInteger s;

    public SignatureImpl(final BigInteger r, final BigInteger s) {
      this.r = r;
      this.s = s;
    }

    @Override
    public BigInteger getR() {
      return r;
    }

    @Override
    public BigInteger getS() {
      return s;
    }
  }
}
