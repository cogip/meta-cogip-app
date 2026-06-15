#!/bin/sh
# First-boot setup for the pre-shipped Cogip app: load the image tarball
# from the rootfs into the Docker store on the data partition, unless that
# image is already in the store.
#
# The editable venv (/opt/.venv) is no longer seeded here: it is shipped
# pre-extracted on the rootfs by the cogip-app-venv recipe (written by the
# flash, not copied by the Pi at first boot).
#
# The check is cheap, so this is fast on every reboot. A reflash wipes
# /data (empty Docker store) -> the load runs fresh.

set -eu

TARBALL=/opt/cogip/cogip-app.image.tar.zst
IMAGE=cogip/cogip-tools:console

if [ ! -f "${TARBALL}" ]; then
    echo "cogip-app-load: ${TARBALL} not found, nothing to load" >&2
    exit 0
fi

# Load the image unless it is already in the local store. Checking the
# store is instant; hashing the ~1 GB tarball every boot used to stall
# the boot for ~a minute and delay the whole stack.
if docker image inspect "${IMAGE}" >/dev/null 2>&1; then
    echo "cogip-app-load: image ${IMAGE} already loaded"
else
    echo "cogip-app-load: loading ${TARBALL} into Docker ..."
    zstd -dc "${TARBALL}" | docker load
fi

echo "cogip-app-load: done"
