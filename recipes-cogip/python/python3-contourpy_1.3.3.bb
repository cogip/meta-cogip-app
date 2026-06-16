SUMMARY = "Python library for calculating contours of 2D quadrilateral grids"
HOMEPAGE = "https://pypi.org/project/contourpy/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://contourpy-1.3.3.dist-info/LICENSE;md5=0186404b1452548f04e644440ce58e3c"

WHEEL = "contourpy-${PV}-cp314-cp314-manylinux_2_26_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/b1/71/f93e1e9471d189f79d0ce2497007731c1e6bf9ef6d1d61b911430c3db4e5/${WHEEL}"
SRC_URI[sha256sum] = "22e9b1bd7a9b1d652cd77388465dc358dafcd2e217d35552424aa4f996f524f5"

inherit python_wheel
