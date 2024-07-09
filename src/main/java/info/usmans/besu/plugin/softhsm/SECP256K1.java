// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
package info.usmans.besu.plugin.softhsm;

import java.security.spec.ECParameterSpec;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;

/** Constants for the SECP256K1 curve. */
public class SECP256K1 {

  public static final ECParameterSpec SECP256K1_PARAM_SPEC;

  static {
    // Get the named curve parameters
    X9ECParameters params = SECNamedCurves.getByName("secp256k1");

    // Create the ECDomainParameters
    ECDomainParameters ecParams =
        new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());

    // Create the ECParameterSpec
    SECP256K1_PARAM_SPEC =
        new ECNamedCurveSpec(
            "secp256k1", ecParams.getCurve(), ecParams.getG(), ecParams.getN(), ecParams.getH());
  }
}
