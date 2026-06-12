#!/bin/sh
# First-boot setup for the pre-shipped Cogip app:
#   1. Load the image tarball from the rootfs into the Docker store on the
#      data partition (idempotent via a checksum stamp on /data).
#   2. Seed the editable COGIP venv (/opt/.venv) onto the rootfs from that
#      image, so compose can bind-mount /opt/.venv into the tool
#      containers and the sources under
#      /opt/.venv/lib/pythonX.Y/site-packages/cogip stay editable on the
#      board (raspios-style).

set -eu

TARBALL=/opt/cogip/cogip-app.image.tar.zst
STAMP=/data/.cogip-app-loaded.sha256
IMAGE=cogip/cogip-tools:console
VENV=/opt/.venv

if [ ! -f "${TARBALL}" ]; then
    echo "cogip-app-load: ${TARBALL} not found, nothing to load" >&2
    exit 0
fi

# 1. Load the image unless the stamp already matches this tarball.
current="$(sha256sum "${TARBALL}" | cut -d' ' -f1)"
if [ -f "${STAMP}" ] && [ "$(cat "${STAMP}")" = "${current}" ]; then
    echo "cogip-app-load: image already loaded (${current})"
else
    echo "cogip-app-load: loading ${TARBALL} into Docker ..."
    zstd -dc "${TARBALL}" | docker load
    echo "${current}" > "${STAMP}"
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
