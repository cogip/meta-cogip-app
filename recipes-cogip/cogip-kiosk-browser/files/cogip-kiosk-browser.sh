#!/bin/sh
# Kiosk launcher: WPE WebKit MiniBrowser on the WPE Platform DRM backend.
#
# Not cog: cog 0.18.5's DRM platform renders through wpebackend-fdo, whose
# embedded-Wayland protocol dispatch SEGVs against WPE 2.52's WebProcess
# (NULL message handler). MiniBrowser ships inside WPE (so it is always
# version-matched) and, on the WPE Platform DRM backend, scans out to KMS
# directly -- no fdo, no crash. The display backend + DRM device + a
# runtime dir are set by the unit (WPE_DISPLAY / WPE_DRM_DEVICE /
# XDG_RUNTIME_DIR).
#
# URL: the dashboard listens on 8080 + ROBOT_ID (beacon 8080, robot 8081,
# ninja 8082, ...). ROBOT_ID comes from /etc/cogip/environment via the
# unit's EnvironmentFile; a KIOSK_URL in the environment overrides it.

set -eu

KIOSK_URL="${KIOSK_URL:-http://localhost:$((8080 + ${ROBOT_ID:-0}))}"

exec /usr/libexec/wpe-webkit-2.0/MiniBrowser "${KIOSK_URL}"
