# Besu Plugin - PKCS11 SoftHSM

A [Besu plugin](https://besu.hyperledger.org/private-networks/reference/plugin-api-interfaces) that shows how to 
integrate with HSM with PKCS11 interface. SoftHSM is used as a test HSM.

## Build Instructions
```shell
./gradlew clean build
```

## Usage
Drop the `jar` in the `/plugins` folder under Besu installation. This plugin will expose following additional cli 
options:
-

## Linux SoftHSM Setup
Following steps are tested on Ubuntu 24.04 LTS. Install following packages.

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