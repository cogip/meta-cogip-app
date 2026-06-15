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

# Wait for the dashboard to start serving before launching MiniBrowser.
# The dashboard is a container that only begins listening ~30 s after
# boot, and MiniBrowser does not retry a refused connection -- it would
# sit forever on a "Connection refused" error page. Poll the URL (busybox
# wget; -T 2 keeps a refused connection fast) until it answers, capped at
# ~2 min so the kiosk still comes up if the dashboard never does.
i=0
until wget -q -T 2 -O /dev/null "${KIOSK_URL}" 2>/dev/null; do
    i=$((i + 1))
    if [ "$i" -ge 120 ]; then
        echo "cogip-kiosk-browser: ${KIOSK_URL} still down after ${i}s, launching anyway" >&2
        break
    fi
    sleep 1
done

exec /usr/libexec/wpe-webkit-2.0/MiniBrowser "${KIOSK_URL}"
