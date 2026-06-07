SUMMARY = "Pre-built Cogip Docker image tarball embedded in the rootfs"
DESCRIPTION = "Installs the zstd-compressed 'docker save' tarball of the \
cogip/cogip-tools:console arm64 image into the rootfs. It is loaded into \
the data-partition Docker store at first boot by cogip-app-load. \
\
The tarball is large and gitignored; produce it before building with \
'make app-image' in yocto/ (docker build + docker save | zstd into \
meta-cogip/files-prebuilt/)."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# The tarball lives outside the recipe dir, in the (gitignored) layer
# staging area populated by `make app-image`. It MUST exist before
# building the image (the Makefile `build` target guards this). bitbake
# tracks the file's checksum in the fetch signature, so refreshing the
# tarball (a new `make app-image`) correctly re-triggers do_rootfs.
FILESEXTRAPATHS:prepend := "${COGIP_FILES_PREBUILT}:"

# unpack=0: keep the tarball as-is. Without it bitbake recognises the
# .tar.zst extension and auto-extracts it, so the file would not be in
# ${WORKDIR} for do_install (and we want to ship the tar, not its
# contents -- it is docker-loaded whole at first boot).
SRC_URI = "file://cogip-app.image.tar.zst;unpack=0"

S = "${WORKDIR}"

inherit allarch

do_install() {
    install -d ${D}/opt/cogip
    install -m 0644 ${WORKDIR}/cogip-app.image.tar.zst \
                    ${D}/opt/cogip/cogip-app.image.tar.zst
}

FILES:${PN} = "/opt/cogip/cogip-app.image.tar.zst"

# Binary blob, no debug/strip work to do.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
