// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
package info.usmans.besu.plugin.softhsm;

import static info.usmans.besu.plugin.softhsm.BesuPkcs11SoftHsmPlugin.SECURITY_MODULE_NAME;

import java.nio.file.Path;
import picocli.CommandLine.Option;

/**
 * Represents cli options that are required by the Besu PKCS11-SoftHSM plugin. Provides {@code
 * --plugin-pkcs11-softhsm-config-path} option.
 */
public class Pkcs11PluginCliOptions {
  @Option(
      names = "--plugin-" + SECURITY_MODULE_NAME + "-config-path",
      description = "Path to the PKCS11 configuration file",
      required = true,
      paramLabel = "<path>")
  private Path pkcs11ConfigPath;

  /** Default constructor. Performs no initialization. */
  public Pkcs11PluginCliOptions() {}

  /**
   * Constructor that initializes the PKCS11 configuration file path.
   *
   * @param pkcs11ConfigPath the path to the PKCS11 configuration file
   */
  public Pkcs11PluginCliOptions(final Path pkcs11ConfigPath) {
    this.pkcs11ConfigPath = pkcs11ConfigPath;
  }

  /**
   * Returns the path to the PKCS11 configuration file.
   *
   * @return the path to the PKCS11 configuration file
   */
  public Path getPkcs11ConfigPath() {
    return pkcs11ConfigPath;
  }
}
