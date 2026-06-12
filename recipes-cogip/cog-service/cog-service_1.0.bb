SUMMARY = "Cog kiosk launcher service"
DESCRIPTION = "Systemd unit that brings up cage (single-app Wayland \
compositor) and launches Cog pointed at KIOSK_URL."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://cog-kiosk.service \
    file://cog-kiosk.sh \
"

S = "${WORKDIR}"

RDEPENDS:${PN} = "cog wpewebkit"

# Injected at build time from KIOSK_URL (see kas-cogip.yml / cogip.conf).
KIOSK_URL ??= "http://localhost:8080"

inherit systemd

SYSTEMD_SERVICE:${PN} = "cog-kiosk.service"
SYSTEMD_AUTO_ENABLE  = "enable"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/cog-kiosk.service \
                    ${D}${systemd_system_unitdir}/cog-kiosk.service

    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/cog-kiosk.sh \
                    ${D}${bindir}/cog-kiosk

    # Bake the kiosk URL into the launcher so the unit stays simple
    # (no environment file plumbing). Edit /usr/bin/cog-kiosk on-target
    # to point at a different URL if needed.
    sed -i 's|@KIOSK_URL@|${KIOSK_URL}|g' ${D}${bindir}/cog-kiosk
}

# Note: getty@tty1 (which would fight Cog for the VT) is masked at the
# distro level, not here -- see meta-cogip-distro's systemd bbappend.
# Owning the console is a system policy, not the launcher's concern.

FILES:${PN} = " \
    ${systemd_system_unitdir}/cog-kiosk.service \
    ${bindir}/cog-kiosk \
"
