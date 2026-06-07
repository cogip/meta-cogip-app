#!/bin/sh
# Load the pre-shipped Cogip Docker image into the Docker store on the
# data partition, once, at first boot (or again after the rootfs image
# tar changes). Idempotent via a checksum stamp on /data.

set -eu

TARBALL=/opt/cogip/cogip-app.image.tar.zst
STAMP=/data/.cogip-app-loaded.sha256

if [ ! -f "${TARBALL}" ]; then
    echo "cogip-app-load: ${TARBALL} not found, nothing to load" >&2
    exit 0
fi

current="$(sha256sum "${TARBALL}" | cut -d' ' -f1)"

if [ -f "${STAMP}" ] && [ "$(cat "${STAMP}")" = "${current}" ]; then
    echo "cogip-app-load: image already loaded (${current})"
    exit 0
fi

echo "cogip-app-load: loading ${TARBALL} into Docker ..."
zstd -dc "${TARBALL}" | docker load

echo "${current}" > "${STAMP}"
echo "cogip-app-load: done"
