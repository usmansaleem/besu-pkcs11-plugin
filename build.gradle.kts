import org.jreleaser.model.Active
import org.jreleaser.model.Distribution
import org.jreleaser.model.UpdateSection

plugins {
  `java-library`
  alias(libs.plugins.spotless)
  alias(libs.plugins.gradle.semver)
  alias(libs.plugins.jreleaser)
}

semver {
  tagPrefix("v")
  initialVersion("0.0.0")
  findProperty("semver.overrideVersion")?.toString()?.let { overrideVersion(it) }
}

project.group = "info.usmans.tools"

version = semver.version // project version, also used for jreleaser

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
}

dependencies {
  // Use JUnit Jupiter for testing.
  testImplementation(libs.junit.jupiter)

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  // This dependency is exported to consumers, that is to say found on their compile classpath.
  api(libs.bcprov)
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
