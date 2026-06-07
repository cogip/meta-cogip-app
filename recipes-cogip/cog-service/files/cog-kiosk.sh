#!/bin/sh
# Cog kiosk launcher.
#
# Runs Cog with its DRM platform: WPE WebKit talks straight to
# /dev/dri/card0 via Mesa GBM. No Wayland compositor involved.

set -eu

KIOSK_URL="${KIOSK_URL:-@KIOSK_URL@}"

exec cog --platform=drm "${KIOSK_URL}"
