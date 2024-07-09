#!/bin/bash
# Copyright 2024, Usman Saleem.
# SPDX-License-Identifier: (Apache-2.0 OR MIT)

# Set default values for PIN and SO_PIN
DEFAULT_PIN="test123"
DEFAULT_SO_PIN="sotest123"

# Path to the PIN file
PIN_FILE="/etc/besu/config/pkcs11-hsm-password.txt"

# Read PIN from file if it exists, otherwise use environment variable or default value
if [ -f "$PIN_FILE" ]; then
    PIN=$(cat "$PIN_FILE")
else
    PIN="${PIN:-$DEFAULT_PIN}"
fi

# Use environment variables if set, otherwise use default values
SO_PIN="${SO_PIN:-$DEFAULT_SO_PIN}"

# Set up cleanup trap
trap 'rm -f /tmp/ec-secp256k1-*.pem' EXIT

# Check if SoftHSM module exists
SOFTHSM_MODULE="/usr/lib/softhsm/libsofthsm2.so"
if [ ! -f "$SOFTHSM_MODULE" ]; then
    echo "SoftHSM module not found: $SOFTHSM_MODULE"
    exit 1
fi

# Check if token already exists
if ! softhsm2-util --show-slots | grep -q "testtoken"; then
    echo "Initializing SoftHSM token ..."
    if ! softhsm2-util --init-token --slot 0 --label "testtoken" --pin "$PIN" --so-pin "$SO_PIN"; then
        echo "Failed to initialize token"
        exit 1
    fi

    echo "Generating SECP256K1 private key using openssl ..."
    # Generating temporary SECP256K1 private key (-noout=not encoded)
    if ! openssl ecparam -name secp256k1 -genkey -noout -out /tmp/ec-secp256k1-priv-key.pem; then
        echo "Failed to generate private key"
        exit 1
    fi

    # Generate public key from private key
    if ! openssl ec -in /tmp/ec-secp256k1-priv-key.pem -pubout -out /tmp/ec-secp256k1-pub-key.pem; then
        echo "Failed to generate public key"
        exit 1
    fi

    # Generate a self-signed certificate
    if ! openssl req -new -x509 -key /tmp/ec-secp256k1-priv-key.pem -out /tmp/ec-secp256k1-cert.pem -days 365 -subj '/CN=example.com'; then
        echo "Failed to generate self-signed certificate"
        exit 1
    fi

    echo "Importing openssl secp256k1 key into softhsm id: 1, label: testkey ..."
    # Importing private key and cert in softhsm. Note we have to specify --usage-derive for ECDH key agreement to work
    if ! pkcs11-tool --module "$SOFTHSM_MODULE" --login --pin "$PIN" \
    --write-object /tmp/ec-secp256k1-priv-key.pem --type privkey --usage-derive --id 1 --label "testkey" \
    --token-label "testtoken"; then
        echo "Failed to import private key"
        exit 1
    fi

    if ! pkcs11-tool --module "$SOFTHSM_MODULE" --login --pin "$PIN" \
    --write-object /tmp/ec-secp256k1-pub-key.pem --type pubkey --usage-derive --id 1 --label "testkey" \
    --token-label "testtoken"; then
        echo "Failed to import public key"
        exit 1
    fi

    if ! pkcs11-tool --module "$SOFTHSM_MODULE" --login --pin "$PIN" \
    --write-object /tmp/ec-secp256k1-cert.pem --type cert --id 1 --label "testkey" \
    --token-label "testtoken"; then
        echo "Failed to import certificate"
        exit 1
    fi

    echo "Token and keys initialized successfully."
else
    echo "Token already exists. Skipping initialization."
fi

# Launch Besu with the provided arguments
exec besu "$@"