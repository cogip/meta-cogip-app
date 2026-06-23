# Install a prebuilt Python wheel (manylinux aarch64) into the target
# site-packages.
#
# For deps whose from-source build is impractical under Yocto (scipy and the
# scientific stack: BLAS/LAPACK/Fortran/meson), ship the upstream manylinux
# wheel instead. manylinux wheels are self-contained (auditwheel bundles their
# native libs) and target a glibc baseline older than wrynose, so they load on
# the target. The wheel is built for a specific CPython ABI, so the recipe must
# match the image's python3 (cp314 on wrynose).
#
# Usage: set SRC_URI to the .whl and WHEEL to its filename, then
# `inherit python_wheel`. LIC_FILES_CHKSUM points inside the extracted
# dist-info (paths are relative to S).

inherit python3-dir python3native

DEPENDS += "unzip-native python3-native"

# Has compiled .so -> arch-specific, not allarch.
PACKAGE_ARCH = "${TUNE_PKGARCH}"

# Source is a single .whl copied into UNPACKDIR (not an archive bitbake
# extracts to ${BP}). Point S at UNPACKDIR -- which exists right after
# do_unpack -- so the default S-exists check does not warn, and the license
# check (LIC_FILES_CHKSUM relative to S) finds the extracted dist-info.
S = "${UNPACKDIR}"

# A .whl is a zip but bitbake does not auto-extract that suffix. Extract it
# ourselves (into S = UNPACKDIR), before the license check and packaging.
do_unpack_wheel() {
    unzip -q -o ${UNPACKDIR}/${WHEEL} -d ${S}
}
addtask unpack_wheel after do_unpack before do_populate_lic do_configure
do_unpack_wheel[depends] += "unzip-native:do_populate_sysroot"

do_install() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -rf ${S}/. ${D}${PYTHON_SITEPACKAGES_DIR}/
    # Drop the wheel itself (it sits in UNPACKDIR = S) from site-packages.
    rm -f ${D}${PYTHON_SITEPACKAGES_DIR}/${WHEEL}

    # Pre-compile to bytecode: unzipped wheels ship .py only, so the FIRST
    # import on the target compiles .pyc on the fly (slow cold start, e.g. the
    # cogip/server/dashboard boot). The native python3 is the same 3.14 as the
    # target, so its .pyc (cpython-314) are valid on-device. Strip the destdir
    # from the embedded paths (-s) so they read as /usr/lib/... at runtime.
    ${PYTHON} -m compileall -q -j 0 \
        -s ${D} -p / \
        ${D}${PYTHON_SITEPACKAGES_DIR} || true
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"

# The binaries are prebuilt by upstream (manylinux): bypass the QA and
# packaging steps that assume Yocto compiled and owns them.
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"
EXCLUDE_FROM_SHLIBS = "1"
SKIP_FILEDEPS = "1"
INSANE_SKIP:${PN} += "already-stripped ldflags rpaths useless-rpaths file-rdeps staticdev libdir arch"
