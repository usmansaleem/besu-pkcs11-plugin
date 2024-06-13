plugins {
  `java-library`
  alias(libs.plugins.spotless)
  alias(libs.plugins.jgitver)
}

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

jgitver {
  nonQualifierBranches = "main"
  useDirty = true
}
