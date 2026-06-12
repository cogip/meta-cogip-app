SUMMARY = "First-boot loader for the pre-shipped Cogip Docker image"
DESCRIPTION = "Oneshot service that docker-loads the image tarball from \
the rootfs into the data-partition Docker store and seeds the editable \
venv /opt/.venv, idempotently. Also ships cogip-app-update, a CLI to \
reinstall a tools wheel into /opt/.venv without reflashing."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://cogip-app-load.sh \
    file://cogip-app-update.sh \
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
    install -m 0755 ${WORKDIR}/cogip-app-update.sh ${D}${bindir}/cogip-app-update

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/cogip-app-load.service \
                    ${D}${systemd_system_unitdir}/cogip-app-load.service
}

FILES:${PN} = " \
    ${bindir}/cogip-app-load \
    ${bindir}/cogip-app-update \
    ${systemd_system_unitdir}/cogip-app-load.service \
"
