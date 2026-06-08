SUMMARY = "Pre-built Cogip container image embedded in the rootfs"
DESCRIPTION = "Installs the zstd-compressed 'docker save' tarball of the \
cogip/cogip-tools:console arm64 image into the rootfs. It is loaded into \
the data-partition Docker store at first boot by cogip-app-load. \
\
The tarball is produced by 'make app-image' (docker build of the \
cogip-tools Dockerfile + docker save) and dropped into DL_DIR; its \
checksum is pinned in cogip-app-image.inc for provenance. The .tar.zst \
is never committed. Built locally it is already in DL_DIR (no download); \
once published it is fetched from a meta-cogip-app release."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# Pinned sha256 of the image tarball (tracked, updated by `make
# app-image`). require (not parse-time read) so bitbake re-parses when
# the checksum changes.
require cogip-app-image.inc

# Remote artifact, forward-compatible with CI publishing to a
# meta-cogip-app release. Built locally the file is already in DL_DIR,
# so no download occurs; the pinned sha256 is verified either way.
# unpack=0: ship the tarball whole (docker-loaded at first boot), don't
# let bitbake auto-extract the .tar.zst.
SRC_URI = "https://github.com/cogip/meta-cogip-app/releases/download/app-image-latest/cogip-app.image.tar.zst;unpack=0"
SRC_URI[sha256sum] = "${COGIP_APP_IMAGE_SHA256}"

S = "${WORKDIR}"

inherit allarch

do_install() {
    install -d ${D}/opt/cogip
    src="${WORKDIR}/cogip-app.image.tar.zst"
    [ -f "$src" ] || src="${DL_DIR}/cogip-app.image.tar.zst"
    install -m 0644 "$src" ${D}/opt/cogip/cogip-app.image.tar.zst
}

FILES:${PN} = "/opt/cogip/cogip-app.image.tar.zst"

# Binary blob, no debug/strip work to do.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
