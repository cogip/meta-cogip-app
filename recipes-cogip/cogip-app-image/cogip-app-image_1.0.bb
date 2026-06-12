SUMMARY = "Pre-built Cogip container image embedded in the rootfs"
DESCRIPTION = "Installs the zstd-compressed 'docker save' tarball of the \
cogip/cogip-tools:console arm64 image into the rootfs. It is loaded into \
the data-partition Docker store at first boot by cogip-app-load. \
\
The tarball is produced by 'make app-image' (docker build of the \
cogip-tools Dockerfile + docker save) straight into DL_DIR, and picked \
up here as a LOCAL file. The .tar.zst is never committed."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# `make app-image` drops the tarball into DL_DIR. Reference it as a local
# file:// (DL_DIR added to the file search path): a local fetch needs no
# sha256sum -- so there is nothing to pin or keep in sync -- and bitbake
# tracks the file's CONTENT, so a freshly built tarball is rebuilt into
# the image automatically. unpack=0: ship the .tar.zst whole (docker-load
# happens at first boot), don't let bitbake auto-extract it.
FILESEXTRAPATHS:prepend := "${DL_DIR}:"
SRC_URI = "file://cogip-app.image.tar.zst;unpack=0"

S = "${UNPACKDIR}"

inherit allarch

do_install() {
    install -d ${D}/opt/cogip
    src="${UNPACKDIR}/cogip-app.image.tar.zst"
    [ -f "$src" ] || src="${DL_DIR}/cogip-app.image.tar.zst"
    install -m 0644 "$src" ${D}/opt/cogip/cogip-app.image.tar.zst
}

FILES:${PN} = "/opt/cogip/cogip-app.image.tar.zst"

# Binary blob, no debug/strip work to do.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
