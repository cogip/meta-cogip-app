SUMMARY = "Pythonic bindings for FFmpeg's libraries."
HOMEPAGE = "https://pypi.org/project/av/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://av-17.1.0.dist-info/licenses/AUTHORS.py;md5=71ae0bf5042d0520427d742125a9957e"

WHEEL = "av-${PV}-cp311-abi3-manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/ad/13/64f6c466471cea225b8b2f4cdc51a571f8a286984b55a08d169b932fda5d/${WHEEL}"
SRC_URI[sha256sum] = "6a20658ec7d96a70e14b1196eff00b7cdd8831ac3b99868e16b8ba8b24090847"

inherit python_wheel
