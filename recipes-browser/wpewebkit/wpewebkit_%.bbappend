# Cogip kiosk WebKit policy (application-level feature selection).
#
# The dashboard is plain HTML/JS and shows the camera as an MJPEG <img>
# (cameraModule.js), so none of the HTML5 media path is used. Dropping
# these flags removes the whole GStreamer plugin stack + flite from the
# build and image. WebGL stays ON (GBM/EGL based, no GStreamer).
PACKAGECONFIG:remove = "video mediasource mediastream webaudio gst_gl speech-synthesis"

# WebCodecs has no PACKAGECONFIG flag and is ON by default, but its
# VideoFrame type comes from the (now disabled) video stack, so it fails
# to compile and WebKit does not auto-disable it. Force it off; the
# MJPEG-in-<img> dashboard does not use the WebCodecs API.
EXTRA_OECMAKE:append = " -DENABLE_WEB_CODECS=OFF"

# Note: host-build tuning (PARALLEL_MAKE / job limits for the heavy
# WebKit link) is intentionally NOT here -- it lives in the build repo
# (yocto-build kas local_conf_header), since it depends on the build
# host, not on the application.
