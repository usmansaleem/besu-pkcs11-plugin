// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
package info.usmans.besu.plugin.softhsm;

import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModule;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModuleException;
import org.hyperledger.besu.plugin.services.securitymodule.data.PublicKey;
import org.hyperledger.besu.plugin.services.securitymodule.data.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Module implementation that interacts with a HSM (such as SoftHSM) using PKCS11
 * interface.
 */
public class Pkcs11SecurityModuleService implements SecurityModule {
  private static final Logger LOG = LoggerFactory.getLogger(Pkcs11SecurityModuleService.class);
  private final Pkcs11PluginCliOptions cliParams;

  public Pkcs11SecurityModuleService(final Pkcs11PluginCliOptions cliParams) {
    LOG.debug("Creating Pkcs11SecurityModuleService ...");
    this.cliParams = cliParams;
    // verify we have config file and able to access softhsm
  }

  @Override
  public Signature sign(Bytes32 dataHash) throws SecurityModuleException {
    return null;
  }

  @Override
  public PublicKey getPublicKey() throws SecurityModuleException {
    return null;
  }

  @Override
  public Bytes32 calculateECDHKeyAgreement(PublicKey partyKey) throws SecurityModuleException {
    return null;
  }
}
