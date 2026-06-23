SUMMARY = "NumPy: array computing (prebuilt wheel)"
HOMEPAGE = "https://numpy.org"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://numpy-2.5.0.dist-info/licenses/LICENSE.txt;md5=488a04026beeb967ad506b6c702389ee"

WHEEL = "numpy-${PV}-cp314-cp314-manylinux_2_27_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/6a/d9/4a4a628c812750363786afc3d33492709a5cd64b215469c16b0f6c7bb811/${WHEEL}"
SRC_URI[sha256sum] = "1a7569a7b53c77716f036bb28cb1c91f166a26ec7d9502cd1e4bdfe502fdec22"

inherit python_wheel
