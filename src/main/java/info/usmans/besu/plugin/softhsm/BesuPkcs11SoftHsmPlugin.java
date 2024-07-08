// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
package info.usmans.besu.plugin.softhsm;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.plugin.BesuContext;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.hyperledger.besu.plugin.services.SecurityModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(BesuPlugin.class)
public class BesuPkcs11SoftHsmPlugin implements BesuPlugin {
  static final String SECURITY_MODULE_NAME = "pkcs11-softhsm";
  private static final Logger LOG = LoggerFactory.getLogger(BesuPkcs11SoftHsmPlugin.class);
  private final Pkcs11PluginCliOptions cliParams = new Pkcs11PluginCliOptions();

  @Override
  public void register(final BesuContext besuContext) {
    LOG.debug("Registering plugin ...");
    registerCliOptions(besuContext);
    registerSecurityModule(besuContext);
  }

  private void registerCliOptions(final BesuContext besuContext) {
    besuContext
        .getService(PicoCLIOptions.class)
        .orElseThrow(() -> new IllegalStateException("Expecting PicoCLIOptions to be present"))
        .addPicoCLIOptions(SECURITY_MODULE_NAME, cliParams);
  }

  private void registerSecurityModule(final BesuContext besuContext) {
    // lazy-init our security module implementation during register phase
    besuContext
        .getService(SecurityModuleService.class)
        .orElseThrow(
            () -> new IllegalStateException("Expecting SecurityModuleService to be present"))
        .register(SECURITY_MODULE_NAME, () -> new Pkcs11SecurityModuleService(cliParams));
  }

  @Override
  public void start() {
    LOG.debug("Starting plugin ...");
  }

  @Override
  public void stop() {
    LOG.debug("Stopping plugin ...");
  }
}
