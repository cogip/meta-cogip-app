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
#
# Time-to-page: rather than block here until the dashboard answers and
# only THEN launch the browser, we launch MiniBrowser immediately on a
# local loading page that polls the dashboard in JS and redirects when it
# is up. This overlaps WPE's cold start (WebProcess spawn + GL/DRM init,
# several seconds) with the dashboard container's heavy Python import,
# instead of running them back-to-back -- the dashboard page then loads
# into an already-warm WebProcess.

set -eu

KIOSK_URL="${KIOSK_URL:-http://localhost:$((8080 + ${ROBOT_ID:-0}))}"

RUNTIME="${XDG_RUNTIME_DIR:-/run/cog}"
LOADING="${RUNTIME}/loading.html"

# Loading splash: poll the dashboard (no-cors fetch -- a refused connection
# rejects, any HTTP response resolves) and redirect as soon as it answers.
cat > "${LOADING}" <<EOF
<!doctype html>
<html lang="fr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>COGIP</title>
<style>
  html, body { margin: 0; height: 100%; background: #1e1e1e; color: #d0d0d0;
    font-family: sans-serif; }
  .box { position: absolute; top: 50%; left: 50%;
    transform: translate(-50%, -50%); text-align: center; }
  h1 { letter-spacing: .3em; margin: 0 0 .6em; font-weight: 300; }
  .spin { width: 38px; height: 38px; margin: 1.2em auto 0;
    border: 4px solid #333; border-top-color: #1e88e5; border-radius: 50%;
    animation: r 1s linear infinite; }
  @keyframes r { to { transform: rotate(360deg); } }
</style>
</head>
<body>
  <div class="box">
    <h1>COGIP</h1>
    <div>Connexion au dashboard...</div>
    <div class="spin"></div>
  </div>
  <script>
    var url = "${KIOSK_URL}";
    function check() {
      fetch(url, { mode: "no-cors", cache: "no-store" })
        .then(function () { window.location.href = url; })
        .catch(function () { setTimeout(check, 500); });
    }
    check();
  </script>
</body>
</html>
EOF

exec /usr/libexec/wpe-webkit-2.0/MiniBrowser "file://${LOADING}"
