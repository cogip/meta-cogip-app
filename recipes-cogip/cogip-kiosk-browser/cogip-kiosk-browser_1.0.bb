SUMMARY = "Cogip kiosk launcher service"
DESCRIPTION = "Systemd unit that runs WPE WebKit's MiniBrowser on the WPE \
Platform DRM backend, full-screen on the HDMI VT, pointed at the local \
dashboard (port 8080 + ROBOT_ID). MiniBrowser is used instead of cog \
because cog 0.18.5's wpebackend-fdo DRM path SEGVs against WPE 2.52."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://cogip-kiosk-browser.service \
    file://cogip-kiosk-browser.sh \
"

S = "${UNPACKDIR}"

# MiniBrowser ships in wpewebkit (built with the minibrowser PACKAGECONFIG,
# see the wpewebkit bbappend). The rest are runtime deps that used to come
# transitively from cog (now removed), so pull them explicitly:
#  - xkeyboard-config: XKB rules (/usr/share/X11/xkb) for WPE Platform's
#    libxkbcommon -- without them MiniBrowser SEGVs ("Cannot load XKB
#    rules evdev").
#  - the mesa GL stack: libEGL + GLESv2 + GBM + the DRI gallium driver
#    (mesa-megadriver -> /usr/lib/dri, e.g. v3d). Without libEGL.so.1 /
#    the DRI driver, WPE Platform DRM cannot render and the screen stays
#    blank.
RDEPENDS:${PN} = " \
    wpewebkit \
    xkeyboard-config \
    libegl-mesa libgles2-mesa libgbm mesa-megadriver \
"

inherit systemd

SYSTEMD_SERVICE:${PN} = "cogip-kiosk-browser.service"
SYSTEMD_AUTO_ENABLE  = "enable"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/cogip-kiosk-browser.service \
                    ${D}${systemd_system_unitdir}/cogip-kiosk-browser.service

    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/cogip-kiosk-browser.sh \
                    ${D}${bindir}/cogip-kiosk-browser
}

# KIOSK_URL is no longer baked in: the launcher derives it from ROBOT_ID
# (8080 + ROBOT_ID) read from /etc/cogip/environment via the unit's
# EnvironmentFile, so the role drives the dashboard port.
#
# getty@tty1 (which would fight the kiosk for the VT) is kept off by the
# image console policy (cogip-kiosk-image), not here.

FILES:${PN} = " \
    ${systemd_system_unitdir}/cogip-kiosk-browser.service \
    ${bindir}/cogip-kiosk-browser \
"
