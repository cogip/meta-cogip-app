SUMMARY = "First-boot loader for the pre-shipped Cogip Docker image"
DESCRIPTION = "Oneshot service that docker-loads the image tarball from \
the rootfs into the data-partition Docker store, idempotently."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://cogip-app-load.sh \
    file://cogip-app-load.service \
"

S = "${WORKDIR}"

RDEPENDS:${PN} = "docker-moby zstd coreutils cogip-app-image"

inherit systemd allarch

SYSTEMD_SERVICE:${PN} = "cogip-app-load.service"
SYSTEMD_AUTO_ENABLE  = "enable"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/cogip-app-load.sh ${D}${bindir}/cogip-app-load

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/cogip-app-load.service \
                    ${D}${systemd_system_unitdir}/cogip-app-load.service
}

FILES:${PN} = " \
    ${bindir}/cogip-app-load \
    ${systemd_system_unitdir}/cogip-app-load.service \
"
