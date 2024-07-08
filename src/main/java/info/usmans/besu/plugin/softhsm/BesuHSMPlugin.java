// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
package info.usmans.besu.plugin.softhsm;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.plugin.BesuContext;
import org.hyperledger.besu.plugin.BesuPlugin;

@AutoService(BesuPlugin.class)
public class BesuHSMPlugin implements BesuPlugin {

  @Override
  public void register(final BesuContext besuContext) {}

  @Override
  public void start() {}

  @Override
  public void stop() {}
}
