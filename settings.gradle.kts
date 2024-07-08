// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
plugins {
  // Apply the foojay-resolver plugin to allow automatic download of JDKs
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "besu-pkcs11-plugin"
