SUMMARY = "Pre-extracted Cogip editable venv (/opt/.venv) on the rootfs"
DESCRIPTION = "Ships /opt/.venv (the cogip-tools wheel + its deps, built \
in the cogip-tools:console image) straight onto the rootfs at build time. \
compose bind-mounts it into the tool containers and the sources stay \
editable on the board. Replaces the first-boot 'docker cp' seed: the \
~700 MB venv is written by the flash, not copied by the Pi at first boot. \
The venv's interpreter (/opt/python) lives inside the container, which is \
where the venv actually runs."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# `make app-image` exports /opt/.venv from the console image into DL_DIR as
# cogip-venv.tar.zst, referenced as a local file:// (DL_DIR on the search
# path) -- content-tracked, no sha to pin. unpack=0: don't let bitbake try
# to auto-extract the .tar.zst; do_install does it. Gated on COGIP_APP like
# the rest of the Docker app stack.
FILESEXTRAPATHS:prepend := "${DL_DIR}:"
COGIP_APP ??= "1"
SRC_URI = "${@'file://cogip-venv.tar.zst;unpack=0' if d.getVar('COGIP_APP') in ('1', 'yes', 'true') else ''}"

S = "${UNPACKDIR}"

DEPENDS = "zstd-native"

# The venv carries aarch64 compiled extensions (numpy, opencv, cogip cpp),
# so this is a machine package, not allarch.
PACKAGE_ARCH = "${TUNE_PKGARCH}"

do_install() {
    [ -f "${UNPACKDIR}/cogip-venv.tar.zst" ] || return 0
    install -d ${D}/opt
    # tar holds top-level ".venv/" -> extract under /opt to get /opt/.venv.
    zstd -dc ${UNPACKDIR}/cogip-venv.tar.zst | tar -C ${D}/opt -xf -
}

# One opaque package: no -dev/-staticdev/-dbg split of the venv's .so and
# headers (they belong together, the venv is used as a whole).
PACKAGES = "${PN}"
FILES:${PN} = "/opt/.venv"

# This is a prebuilt third-party tree (manylinux wheels + Astral-built
# extensions), not something we compiled, and its .so run inside the
# container (which provides their libs + the /opt/python interpreter), not
# on the host. Its bundled libraries (e.g. cv2's Qt/xcb copies) are
# strictly private, so keep them out of the global shlib provider/needs
# map -- otherwise bitbake reports "multiple shlib providers" for the
# duplicated bundled sonames. Skip the QA that assumes we built it here.
EXCLUDE_FROM_SHLIBS = "1"
INSANE_SKIP:${PN} += "already-stripped ldflags file-rdeps textrel rpaths dev-so staticdev libdir"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
SKIP_FILEDEPS = "1"
