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
```shell
--plugin-pkcs11-softhsm-config-path=<path>
                             Path to the PKCS11 configuration file
--plugin-pkcs11-softhsm-key-alias=<path>
                             Alias or label of the private key that is stored in the HSM
--plugin-pkcs11-softhsm-password-path=<path>
                             Path to the file that contains password or PIN to access PKCS11 token
```


## Docker setup 
- The plugin can be tested as a docker image. The provided `Dockerfile` is based on Besu's official docker image.
It installs following additional package to manage SECP256K1 private keys and SoftHSM:

```
apt-get install -y --no-install-recommends \
    openssl \
    libssl3 \
    softhsm2 \
    opensc \
    gnutls-bin
```
- The Dockerfile uses `scripts/entrypoint.sh` as entrypoint. This script initializes SoftHSM and generates a private key 
if required.
- The Dockerfile copies the plugin jar to `/plugins` folder.
- To persist SoftHSM data, a volume should be mounted to `/softhsm2`. The host directory should have ownership of userid 1000:1000.
- Decide the token/pin to use for SoftHSM.
- See [Besu documentation](https://besu.hyperledger.org/public-networks/get-started/install/run-docker-image) for further details about other docker options.
- Following is an example to build the docker image:
```shell
docker build --no-cache -t besu-pkcs11:latest .
```
- To run Besu node for testing with SoftHSM, Following directories be mounted as volumes. 
Change the path according to your requirements:
    - `./docker/volumes/data` for Besu data. Will be mounted to `/var/lib/besu`
    - `./docker/volumes/tokens` for SoftHSM data. Will be mounted to `/var/lib/tokens`
    - `./docker/volumes/config` for Besu and PKCS11 config files. Will be mounted to `/etc/besu/config`. This directory already contains sample configurations.

> [!NOTE]
> To initialize the SoftHSM tokens, the entrypoint script will attempt to generate a SECP256K1 private key and 
> initialize SoftHSM on the first run. The SoftHSM `PIN` is defined in `./docker/volumes/config/pkcs11-hsm-password.txt`.
> The `SO_PIN` can be overridden via environment variable, however, it is not required once initialization is done.

- To run the Besu node:
```shell
docker run --rm -it \
    -v ./docker/volumes/data:/var/lib/besu \
    -v ./docker/volumes/tokens:/var/lib/tokens \
    -v ./docker/volumes/config:/etc/besu/config \
    besu-pkcs11:latest --config-file=/etc/besu/config/besu-dev.toml
```

## License

Licensed under either of

* Apache License, Version 2.0, ([LICENSE-APACHE](LICENSE-APACHE-2.0) or <http://www.apache.org/licenses/LICENSE-2.0>)
* MIT license ([LICENSE-MIT](LICENSE-MIT) or <http://opensource.org/licenses/MIT>)

at your option.
`SPDX-License-Identifier: (Apache-2.0 OR MIT)`

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in the work by you, as 
defined in the Apache-2.0 license, shall be dual licensed as above, without any additional terms or conditions.