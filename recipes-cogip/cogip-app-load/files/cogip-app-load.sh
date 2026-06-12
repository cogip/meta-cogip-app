#!/bin/sh
# First-boot setup for the pre-shipped Cogip app:
#   1. Load the image tarball from the rootfs into the Docker store on the
#      data partition, unless that image is already in the store.
#   2. Seed the editable COGIP venv (/opt/.venv) onto the rootfs from that
#      image, so compose can bind-mount /opt/.venv into the tool
#      containers and the sources under
#      /opt/.venv/lib/pythonX.Y/site-packages/cogip stay editable on the
#      board (raspios-style).
#
# Both checks are cheap, so this is fast on every reboot. A reflash wipes
# /data (empty Docker store) and the rootfs (no /opt/.venv) -> both steps
# run fresh.

set -eu

TARBALL=/opt/cogip/cogip-app.image.tar.zst
IMAGE=cogip/cogip-tools:console
VENV=/opt/.venv

if [ ! -f "${TARBALL}" ]; then
    echo "cogip-app-load: ${TARBALL} not found, nothing to load" >&2
    exit 0
fi

# 1. Load the image unless it is already in the local store. Checking the
# store is instant; hashing the ~1 GB tarball every boot used to stall
# the boot for ~a minute and delay the whole stack.
if docker image inspect "${IMAGE}" >/dev/null 2>&1; then
    echo "cogip-app-load: image ${IMAGE} already loaded"
else
    echo "cogip-app-load: loading ${TARBALL} into Docker ..."
    zstd -dc "${TARBALL}" | docker load
fi

# 2. Seed /opt/.venv from the image, only when absent: on-board edits
# survive reboots; a reflash recreates the rootfs -> reseeded fresh.
if [ ! -x "${VENV}/bin/python" ]; then
    echo "cogip-app-load: seeding ${VENV} from ${IMAGE} ..."
    cid="$(docker create "${IMAGE}")"
    rm -rf "${VENV}"
    mkdir -p /opt
    docker cp "${cid}:/opt/.venv" "${VENV}"
    docker rm "${cid}" >/dev/null
    echo "cogip-app-load: ${VENV} seeded"
fi

echo "cogip-app-load: done"
