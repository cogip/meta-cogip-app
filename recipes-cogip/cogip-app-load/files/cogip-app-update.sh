#!/bin/sh
# Reinstall a COGIP tools wheel into the on-board editable venv
# (/opt/.venv) WITHOUT reflashing -- for quick iteration over SSH:
#
#     scp cogip_tools-<version>.whl root@<pi>:/tmp/
#     ssh root@<pi> cogip-app-update /tmp/cogip_tools-<version>.whl
#
# The venv interpreter (/opt/.venv/bin/python) links to /opt/python, which
# only exists inside the cogip image, so the install runs in a throwaway
# container with /opt/.venv and the wheel bind-mounted. The running tool
# containers also bind-mount /opt/.venv, so restarting them picks up the
# new code.

set -eu

IMAGE=cogip/cogip-tools:console
VENV=/opt/.venv

WHEEL="${1:-}"
if [ -z "${WHEEL}" ] || [ ! -f "${WHEEL}" ]; then
    echo "usage: cogip-app-update /path/to/cogip_tools-<version>.whl" >&2
    exit 1
fi
WHEEL_ABS="$(readlink -f "${WHEEL}")"
WHEEL_NAME="$(basename "${WHEEL_ABS}")"

if [ ! -x "${VENV}/bin/python" ]; then
    echo "cogip-app-update: ${VENV} not seeded yet (boot once / run cogip-app-load)" >&2
    exit 1
fi

echo "cogip-app-update: reinstalling ${WHEEL_NAME} into ${VENV} ..."
docker run --rm \
    -v "${VENV}:${VENV}" \
    -v "${WHEEL_ABS}:/tmp/${WHEEL_NAME}:ro" \
    "${IMAGE}" \
    uv pip install --python "${VENV}/bin/python" --no-deps --reinstall "/tmp/${WHEEL_NAME}"

echo "cogip-app-update: restarting cogip tools ..."
units="$(systemctl list-units --plain --no-legend 'cogip@*.service' 2>/dev/null | awk '{print $1}')"
if [ -n "${units}" ]; then
    # shellcheck disable=SC2086
    systemctl restart ${units}
else
    echo "cogip-app-update: no cogip@ services loaded, nothing to restart" >&2
fi

echo "cogip-app-update: done"
