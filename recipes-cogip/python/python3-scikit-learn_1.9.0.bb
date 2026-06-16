SUMMARY = "A set of python modules for machine learning and data mining"
HOMEPAGE = "https://pypi.org/project/scikit-learn/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://scikit_learn-1.9.0.dist-info/licenses/COPYING;md5=f5c1641ba9c347cd4cc72cd16cdde1c8"

WHEEL = "scikit_learn-${PV}-cp314-cp314-manylinux_2_27_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/7d/79/f4a0c4fe9711154cddabf913471153af79056382ddc612cfe5ee0ff4b72e/${WHEEL}"
SRC_URI[sha256sum] = "5162ad10a418c8a282dde04c9aa06965de3e9a65f33c1440c0ae69bb1a09d913"

inherit python_wheel
