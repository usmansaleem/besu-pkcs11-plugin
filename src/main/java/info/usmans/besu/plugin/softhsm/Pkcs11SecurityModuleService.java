// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
package info.usmans.besu.plugin.softhsm;

import static info.usmans.besu.plugin.softhsm.SignatureUtil.extractRAndSFromDERSignature;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import javax.crypto.KeyAgreement;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModule;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModuleException;
import org.hyperledger.besu.plugin.services.securitymodule.data.PublicKey;
import org.hyperledger.besu.plugin.services.securitymodule.data.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A PKCS11 based implementation of Besu SecurityModule interface. */
public class Pkcs11SecurityModuleService implements SecurityModule {
  private static final Logger LOG = LoggerFactory.getLogger(Pkcs11SecurityModuleService.class);
  private final Pkcs11PluginCliOptions cliParams;
  private Provider provider;
  private KeyStore keyStore;
  private PrivateKey privateKey;
  private ECPublicKey ecPublicKey;
  private ECParameterSpec secp256k1Param;

  public Pkcs11SecurityModuleService(final Pkcs11PluginCliOptions cliParams) {
    LOG.debug("Creating Pkcs11SecurityModuleService ...");
    this.cliParams = cliParams;
    validateCliOptions();
    loadPkcs11Provider();
    loadPkcs11Keystore();
    loadPkcs11PrivateKey();
    loadPkcs11PublicKey();
  }

  private void validateCliOptions() {
    if (cliParams.getPkcs11ConfigPath() == null) {
      throw new SecurityModuleException("PKCS11 configuration file path is not provided");
    }
    if (cliParams.getPkcs11PasswordPath() == null) {
      throw new SecurityModuleException("PKCS11 password file path is not provided");
    }
    if (cliParams.getPrivateKeyAlias() == null) {
      throw new SecurityModuleException("PKCS11 private key alias is not provided");
    }
  }

  private void loadPkcs11Provider() {
    // initialize PKCS11 provider
    LOG.info("Initializing PKCS11 provider ...");

    try {
      provider =
          Security.getProvider("SUNPKCS11").configure(cliParams.getPkcs11ConfigPath().toString());
      Security.addProvider(provider);
    } catch (final Exception e) {
      throw new SecurityModuleException(
          "Error encountered while loading SunPKCS11 provider with configuration: "
              + cliParams.getPkcs11ConfigPath().toString(),
          e);
    }
  }

  private void loadPkcs11Keystore() {
    LOG.info("Loading PKCS11 keystore ...");
    final char[] charArray;
    try {
      charArray = Files.readString(cliParams.getPkcs11PasswordPath()).toCharArray();
    } catch (final IOException e) {
      throw new SecurityModuleException(
          "Error reading file: " + cliParams.getPkcs11PasswordPath(), e);
    }

    try {
      keyStore = KeyStore.getInstance("PKCS11", provider);
      keyStore.load(null, charArray);
    } catch (final Exception e) {
      throw new SecurityModuleException("Error loading PKCS11 keystore", e);
    }
  }

  private void loadPkcs11PrivateKey() {
    LOG.info("Loading private key ...");
    final Key key;
    try {
      key = keyStore.getKey(cliParams.getPrivateKeyAlias(), new char[0]);
    } catch (final Exception e) {
      throw new SecurityModuleException(
          "Error loading private key for alias: " + cliParams.getPrivateKeyAlias(), e);
    }

    if (!(key instanceof PrivateKey)) {
      throw new SecurityModuleException(
          "Loaded key is not a PrivateKey for alias: " + cliParams.getPrivateKeyAlias());
    }

    privateKey = (PrivateKey) key;
  }

  private void loadPkcs11PublicKey() {
    LOG.info("Loading public key ...");
    final Certificate certificate;
    try {
      certificate = keyStore.getCertificate(cliParams.getPrivateKeyAlias());
      if (certificate == null) {
        throw new SecurityModuleException(
            "Certificate not found for private key alias: " + cliParams.getPrivateKeyAlias());
      }
    } catch (final Exception e) {
      throw new SecurityModuleException(
          "Error while loading certificate for private key alias: "
              + cliParams.getPrivateKeyAlias(),
          e);
    }

    final java.security.PublicKey publicKey;
    try {
      publicKey = certificate.getPublicKey();
    } catch (final Exception e) {
      throw new SecurityModuleException(
          "Error while loading public key for alias: " + cliParams.getPrivateKeyAlias(), e);
    }

    if (!(publicKey instanceof ECPublicKey)) {
      throw new RuntimeException(
          "Public Key is not a valid ECPublicKey for alias: " + cliParams.getPrivateKeyAlias());
    }
    ecPublicKey = (ECPublicKey) publicKey;
    // we could use a constant, for now we will get it from the public key
    secp256k1Param = ecPublicKey.getParams();
  }

  @Override
  public Signature sign(Bytes32 dataHash) throws SecurityModuleException {
    try {
      // Java classes generate ASN1 encoded signature,
      // Besu needs P1363 i.e. R and S of the signature
      final java.security.Signature signature =
          java.security.Signature.getInstance("SHA256WithECDSA", provider);
      signature.initSign(privateKey);
      signature.update(dataHash.toArray());
      final byte[] sigBytes = signature.sign();
      return extractRAndSFromDERSignature(sigBytes);
    } catch (final Exception e) {
      if (e instanceof SecurityModuleException) {
        throw (SecurityModuleException) e;
      }
      throw new SecurityModuleException("Error initializing signature", e);
    }
  }

  @Override
  public PublicKey getPublicKey() throws SecurityModuleException {
    return ecPublicKey::getW;
  }

  @Override
  public Bytes32 calculateECDHKeyAgreement(PublicKey theirKey) throws SecurityModuleException {
    LOG.debug("Calculating ECDH key agreement ...");
    // convert Besu PublicKey (which wraps ECPoint) to java.security.PublicKey
    java.security.PublicKey theirPublicKey =
        SignatureUtil.eCPointToPublicKey(theirKey.getW(), secp256k1Param, provider);

    // generate ECDH Key Agreement
    try {
      final KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", provider);
      keyAgreement.init(privateKey);
      keyAgreement.doPhase(theirPublicKey, true);
      return Bytes32.wrap(keyAgreement.generateSecret());
    } catch (final Exception e) {
      throw new SecurityModuleException("Error calculating ECDH key agreement", e);
    }
  }
}
