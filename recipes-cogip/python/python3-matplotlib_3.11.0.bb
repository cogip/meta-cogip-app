SUMMARY = "Matplotlib plotting library (prebuilt wheel)"
HOMEPAGE = "https://matplotlib.org"
LICENSE = "PSF-2.0"
LIC_FILES_CHKSUM = "file://matplotlib-3.11.0.dist-info/LICENSE;md5=b3a68456c219986ef2a7f515e3118a30"

WHEEL = "matplotlib-${PV}-cp314-cp314-manylinux_2_26_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/b7/2d/4e1240ea82ee197dfb3851e71f71c87eeeb975f1753b56a0588e4e80739a/${WHEEL}"
SRC_URI[sha256sum] = "d9695457a467ff86d23f35037a43deb6f1134dd6d3e2ac8ce1e2087cff09ffb9"

inherit python_wheel

RDEPENDS:${PN} += "python3-numpy python3-contourpy python3-fonttools python3-kiwisolver python3-cycler python3-pyparsing python3-pillow python3-dateutil"
