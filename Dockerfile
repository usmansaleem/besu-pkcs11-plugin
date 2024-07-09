# syntax=docker/dockerfile:1
# Copyright 2024, Usman Saleem.
# SPDX-License-Identifier: (Apache-2.0 OR MIT)

# Start from the latest Hyperledger Besu image
FROM hyperledger/besu:latest

# Switch to root to install packages
USER 0

# Install additional packages for SoftHSM2 and OpenSC
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openssl \
    libssl3 \
    softhsm2 \
    opensc \
    gnutls-bin && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create a directory for SoftHSM2 tokens. This can be overridden using a volume mount to persist.
RUN mkdir -p /var/lib/tokens && chmod 755 /var/lib/tokens && chown besu:besu /var/lib/tokens

# Switch back to the besu user
USER besu

# Update workdir to Besu home directory
WORKDIR /opt/besu

# Set environment variables for SoftHSM2 configuration
ENV SOFTHSM2_CONF=/opt/besu/softhsm2.conf

# Copy the PKCS11 plugin JAR to the plugins directory
COPY --chown=besu:besu ./build/libs/besu-pkcs11-plugin-*.jar ./plugins/

# Copy the initialization script
COPY --chown=besu:besu --chmod=755 ./docker/scripts/entrypoint.sh ./entrypoint.sh

# Create a custom SoftHSM2 configuration file in besu home directory
RUN echo "directories.tokendir = /var/lib/tokens" > ./softhsm2.conf

# Set the entrypoint to our new script
ENTRYPOINT ["/opt/besu/entrypoint.sh"]