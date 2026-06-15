SUMMARY = "Cogip Docker daemon configuration"
DESCRIPTION = "Points the Docker data-root at the persistent /data \
partition and orders docker.service after data.mount."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://daemon.json \
    file://data-mount.conf \
"

S = "${UNPACKDIR}"

RDEPENDS:${PN} = "docker-moby"

inherit allarch

do_install() {
    install -d ${D}${sysconfdir}/docker
    install -m 0644 ${UNPACKDIR}/daemon.json \
                    ${D}${sysconfdir}/docker/daemon.json

    # Drop-in ordering docker.service after the data partition mount.
    install -d ${D}${systemd_system_unitdir}/docker.service.d
    install -m 0644 ${UNPACKDIR}/data-mount.conf \
                    ${D}${systemd_system_unitdir}/docker.service.d/data-mount.conf
}

FILES:${PN} = " \
    ${sysconfdir}/docker/daemon.json \
    ${systemd_system_unitdir}/docker.service.d/data-mount.conf \
"
