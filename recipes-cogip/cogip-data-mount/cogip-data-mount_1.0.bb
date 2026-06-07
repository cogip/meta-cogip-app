SUMMARY = "Mount the Cogip persistent data partition at /data"
DESCRIPTION = "Ships a systemd .mount unit for the wic-created 'data' \
ext4 partition. Holds Docker storage, on-device source and records."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://data.mount"

S = "${WORKDIR}"

RDEPENDS:${PN} = "systemd"

inherit systemd allarch

SYSTEMD_SERVICE:${PN} = "data.mount"
SYSTEMD_AUTO_ENABLE  = "enable"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/data.mount \
                    ${D}${systemd_system_unitdir}/data.mount

    # Mount point.
    install -d ${D}/data
}

FILES:${PN} = " \
    ${systemd_system_unitdir}/data.mount \
    /data \
"
