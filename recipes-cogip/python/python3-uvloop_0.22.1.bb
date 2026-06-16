SUMMARY = "Fast implementation of asyncio event loop on top of libuv"
HOMEPAGE = "https://pypi.org/project/uvloop/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://uvloop-0.22.1.dist-info/licenses/LICENSE-APACHE;md5=bb92739ddad0a2811957bd98bdb90474"

WHEEL = "uvloop-${PV}-cp314-cp314-manylinux2014_aarch64.manylinux_2_17_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/a3/94/94af78c156f88da4b3a733773ad5ba0b164393e357cc4bd0ab2e2677a7d6/${WHEEL}"
SRC_URI[sha256sum] = "297c27d8003520596236bdb2335e6b3f649480bd09e00d1e3a99144b691d2a35"

inherit python_wheel
