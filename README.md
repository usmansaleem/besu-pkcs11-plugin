# Besu Plugin - PKCS11 SoftHSM

A [Besu plugin][1] that provides a custom security module to load the [node key][2] from an HSM, such as [SoftHSM][3], 
using PKCS11 libraries.

[1]: <https://besu.hyperledger.org/private-networks/reference/plugin-api-interfaces>
[2]: <https://besu.hyperledger.org/public-networks/concepts/node-keys>
[3]: <https://www.opendnssec.org/softhsm/>

![GitHub Actions Workflow Status](https://github.com/usmansaleem/besu-pkcs11-plugin/actions/workflows/ci.yml/badge.svg?branch=main)
![GitHub Release](https://img.shields.io/github/v/release/usmansaleem/besu-pkcs11-plugin?include_prereleases)

## Build Instructions
You can either use pre-built jar from Assets section in [releases](https://github.com/usmansaleem/besu-pkcs11-plugin/releases) 
or build it yourself.

> [!NOTE] 
> This project requires Java 21 or later. If it is not available, the gradle build will attempt to download one and use it.

- Check [Besu releases](https://github.com/hyperledger/besu/releases) for latest stable version and update it in 
[`gradle/libs.versions.toml`](gradle/libs.versions.toml). For example:

```toml
[versions]
besu = "24.6.0"
```

- Build the plugin:

```shell
./gradlew clean build
```

The plugin jar will be available at `build/libs/besu-pkcs11-plugin-<version>.jar`.

## Usage

Drop the `besu-pkcs11-plugin-<version>.jar` in the `/plugins` folder under Besu installation. This plugin will expose 
following additional cli options:
`TBA`

## Linux SoftHSM Setup
Following steps are tested on Ubuntu 24.04 LTS. Install following packages.
`TBA`

## Docker setup 
See Dockerfile for details.

## License

Licensed under either of

* Apache License, Version 2.0, ([LICENSE-APACHE](LICENSE-APACHE-2.0) or <http://www.apache.org/licenses/LICENSE-2.0>)
* MIT license ([LICENSE-MIT](LICENSE-MIT) or <http://opensource.org/licenses/MIT>)

at your option.
`SPDX-License-Identifier: (Apache-2.0 OR MIT)`

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in the work by you, as 
defined in the Apache-2.0 license, shall be dual licensed as above, without any additional terms or conditions.