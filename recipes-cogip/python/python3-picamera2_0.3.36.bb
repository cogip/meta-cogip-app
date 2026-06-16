SUMMARY = "The libcamera-based Python interface to Raspberry Pi cameras, based on"
HOMEPAGE = "https://pypi.org/project/picamera2/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://picamera2-0.3.36.dist-info/licenses/LICENSE;md5=6541a38108b5accb25bd55a14e76086d"

WHEEL = "picamera2-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/3b/e9/484a810cfd4564df7fbf632925971a2d12be8867866aa539f3136ec1cea4/${WHEEL}"
SRC_URI[sha256sum] = "99c2b97a65e5739ce68743b79e627b1bbb9024d91bc5e3915b98cfd0dcbec1a0"

inherit python_wheel
