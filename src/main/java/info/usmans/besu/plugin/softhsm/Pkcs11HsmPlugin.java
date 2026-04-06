// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
package info.usmans.besu.plugin.softhsm;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.ServiceManager;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.hyperledger.besu.plugin.services.SecurityModuleService;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Besu plugin that provides a custom security module to load the node key from an HSM using
 * PKCS11 libraries.
 */
@AutoService(BesuPlugin.class)
public class Pkcs11HsmPlugin implements BesuPlugin {
  static final String SECURITY_MODULE_NAME = "pkcs11-hsm";
  private static final Logger LOG = LoggerFactory.getLogger(Pkcs11HsmPlugin.class);
  private final Pkcs11PluginCliOptions cliParams = new Pkcs11PluginCliOptions();

  @Override
  public void register(final ServiceManager serviceManager) {
    LOG.info("Registering plugin ...");
    registerCliOptions(serviceManager);
    registerSecurityModule(serviceManager);
  }

  /**
   * Registers {@code Pkcs11PluginCliOptions} with {@code PicoCLIOptions} from the provided {@code
   * ServiceManager}.
   *
   * @param serviceManager the Besu service manager
   */
  private void registerCliOptions(final ServiceManager serviceManager) {
    serviceManager
        .getService(PicoCLIOptions.class)
        .orElseThrow(() -> new IllegalStateException("Expecting PicoCLIOptions to be present"))
        .addPicoCLIOptions(SECURITY_MODULE_NAME, cliParams);
  }

  /**
   * Registers {@code Pkcs11SecurityModule} with the {@code SecurityModuleService} from the provided
   * {@code ServiceManager}.
   *
   * @param serviceManager the Besu service manager
   */
  private void registerSecurityModule(final ServiceManager serviceManager) {
    // lazy-init our security module implementation during register phase
    final SecurityModuleService securityModuleService =
        serviceManager
            .getService(SecurityModuleService.class)
            .orElseThrow(
                () -> new IllegalStateException("Expecting SecurityModuleService to be present"));

    securityModuleService.register(SECURITY_MODULE_NAME, this::getSecurityModuleSupplier);
  }

  private SecurityModule getSecurityModuleSupplier() {
    return new Pkcs11SecurityModuleService(cliParams);
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
