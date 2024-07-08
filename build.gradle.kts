// Copyright 2024, Usman Saleem.
// SPDX-License-Identifier: (Apache-2.0 OR MIT)
import org.jreleaser.model.Active
import org.jreleaser.model.Distribution
import org.jreleaser.model.UpdateSection

plugins {
  `java-library`
  alias(libs.plugins.spotless)
  alias(libs.plugins.jgitver)
  alias(libs.plugins.jreleaser)
}

project.group = "info.usmans.tools"

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()

  // For Besu plugin dependencies
  maven {
    url = uri("https://hyperledger.jfrog.io/artifactory/besu-maven/")
    content { includeGroupByRegex("org\\.hyperledger\\.besu($|\\..*)") }
  }
}

dependencies {
  // This project jar is not supposed to be used as compilation dependency.
  // `api` is used here to distinguish between dependencies which should be used IF it is to be used
  // as a dependency during compiling some other library that depends on this project.
  api(libs.besu.plugin.api)
  api(libs.bcprov)

  // https://github.com/google/auto/tree/main/service
  annotationProcessor(libs.google.auto.service)
  implementation(libs.google.auto.service.annotations)
  implementation(libs.slf4j.api)
  implementation(libs.picocli)

  // testing dependencies
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments.
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

tasks.named<Test>("test") {
  // Use JUnit Platform for unit tests.
  useJUnitPlatform()
}

spotless {
  java {
    importOrder()
    removeUnusedImports()
    googleJavaFormat()
    licenseHeaderFile(layout.projectDirectory.file("gradle/spotless/java.license.template"))
  }

  kotlinGradle { ktfmt() }
}

jgitver { nonQualifierBranches = "main" }

tasks.register("printVersion") {
  group = "Help"
  description = "Prints the project version"
  doLast { println("Version: ${project.version}") }
}

tasks.jar {
  manifest {
    attributes(
        mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
  }
}

jreleaser {
  project {
    description.set("Besu PKCS11-SoftHSM plugin")
    authors.set(listOf("Usman Saleem"))
    license.set("(Apache-2.0 OR MIT)")
    inceptionYear.set("2024")
    copyright.set("2024, Usman Saleem")
    links {
      homepage.set("https://github.com/usmansaleem/besu-pkcs11-plugin")
      documentation.set("https://github.com/usmansaleem/besu-pkcs11-plugin")
    }
  }
  dependsOnAssemble.set(true)
  gitRootSearch.set(true)
  distributions {
    create("besu-pkcs11-plugin") {
      distributionType.set(Distribution.DistributionType.SINGLE_JAR)
      artifact {
        path.set(layout.buildDirectory.file("libs/{{distributionName}}-{{projectVersion}}.jar"))
      }
    }
  }

  release {
    github {
      repoOwner = "usmansaleem"
      // append artifacts to an existing release with matching tag
      update {
        enabled = true
        sections.set(listOf(UpdateSection.ASSETS, UpdateSection.TITLE, UpdateSection.BODY))
      }
      // We need to create tag manually because our version calculation depends on it.
      skipTag = true
      changelog {
        formatted.set(Active.ALWAYS)
        preset.set("conventional-commits")
        contributors {
          enabled.set(true)
          format.set(
              "- {{contributorName}}{{#contributorUsernameAsLink}} ({{.}}){{/contributorUsernameAsLink}}")
        }
      }
    }
  }
}
