SUMMARY = "Fundamental algorithms for scientific computing (prebuilt wheel)"
HOMEPAGE = "https://scipy.org"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://scipy-${PV}.dist-info/LICENSE.txt;md5=288894cdd684361c638d61efdb5bf18d"

# Prebuilt cp314 aarch64 wheel: scipy from source needs BLAS/LAPACK/Fortran +
# meson and has no OE recipe. Bumped past the cogip pin (1.6.x has no cp314
# wheel) -- the detector's sklearn DBSCAN pulls scipy.
WHEEL = "scipy-${PV}-cp314-cp314-manylinux_2_27_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/ef/f2/7cdb8eb308a1a6ae1e19f945913c82c23c0c442a462a46480ce487fdc0ac/${WHEEL}"
SRC_URI[sha256sum] = "adb2642e060a6549c343603a3851ba76ef0b74cc8c079a9a58121c7ec9fe2350"

inherit python_wheel

RDEPENDS:${PN} += "python3-numpy"
