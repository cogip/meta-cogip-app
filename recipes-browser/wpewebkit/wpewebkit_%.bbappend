# Cogip kiosk WebKit policy (application-level feature selection).
#
# The dashboard is plain HTML/JS and shows the camera as an MJPEG <img>
# (cameraModule.js): no HTML5 media at all. We want ZERO GStreamer in the
# build and image.
#
# Subtlety (WPE 2.52): USE_GSTREAMER is a standalone cmake option, ON by
# default (Source/cmake/GStreamerDefinitions.cmake), NOT derived from the
# media ENABLE_* flags. So disabling video/mediasource/... still leaves
# USE(GSTREAMER) on, and core code -- including the WebGL/ANGLE path
# (GraphicsContextGLANGLE) -- keeps `#include <gst/gst.h>`, breaking the
# build when no gstreamer is present.
#
# Expose USE_GSTREAMER as a proper PACKAGECONFIG and leave it OFF. Its
# disable branch also turns off the GStreamer-backed media features that
# default ON and have no meta-webkit toggle of their own (media-recorder,
# media-session, web-codecs) plus the gstreamer sub-options, so no media
# code is left without a backend.
PACKAGECONFIG[gstreamer] = "-DUSE_GSTREAMER=ON,-DUSE_GSTREAMER=OFF -DUSE_GSTREAMER_GL=OFF -DUSE_GSTREAMER_WEBRTC=OFF -DENABLE_MEDIA_RECORDER=OFF -DENABLE_MEDIA_SESSION=OFF -DENABLE_WEB_CODECS=OFF,gstreamer1.0 gstreamer1.0-plugins-base"

# Leave `gstreamer` OUT of PACKAGECONFIG (its disable branch applies), and
# drop the media features that DO have their own toggle (video /
# mediasource / mediastream / webaudio) + flite (speech-synthesis).
PACKAGECONFIG:remove = "video mediasource mediastream webaudio speech-synthesis"

# WPE 2.52 NetworkCacheDataGLib.cpp #includes <gio/gfiledescriptorbased.h>
# (gio-unix-2.0). That include path was only pulled in transitively via
# GStreamer's pkg-config, so with USE_GSTREAMER=OFF it goes missing even
# though the headers ARE in the sysroot. Add gio-unix-2.0 to the search
# path explicitly (it is a legitimate GLib dependency, not a GStreamer
# one -- GStreamer was just masking the gap).
CFLAGS:append = " -I${STAGING_INCDIR}/gio-unix-2.0"
CXXFLAGS:append = " -I${STAGING_INCDIR}/gio-unix-2.0"

# Note: host-build tuning (PARALLEL_MAKE / job limits for the heavy
# WebKit link) is intentionally NOT here -- it lives in the build repo
# (yocto-build kas local_conf_header), since it depends on the build
# host, not on the application.
