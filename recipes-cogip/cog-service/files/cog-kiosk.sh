#!/bin/sh
# Cog kiosk launcher.
#
# Runs Cog with its DRM platform: WPE WebKit talks straight to
# /dev/dri/card0 via Mesa GBM. No Wayland compositor involved.
#
# renderer=gles forces Cog's GPU (EGL/GLES) renderer. Its DRM platform
# otherwise defaults to the 'modeset' renderer, which scans out CPU dumb
# buffers -- and vc4 rejects those ("failed to create framebuffer:
# Invalid argument"), leaving the screen frozen on the boot logo while
# Cog runs blind. The gles renderer uses V3D + GBM and works.

set -eu

KIOSK_URL="${KIOSK_URL:-@KIOSK_URL@}"

exec cog --platform=drm --platform-params=renderer=gles "${KIOSK_URL}"
