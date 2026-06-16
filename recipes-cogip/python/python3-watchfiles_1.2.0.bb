SUMMARY = "Simple, modern and high performance file watching and code reload in p"
HOMEPAGE = "https://pypi.org/project/watchfiles/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://watchfiles-1.2.0.dist-info/licenses/LICENSE;md5=3c69d58fa5f4ff0b4244368d39546751"

WHEEL = "watchfiles-${PV}-cp314-cp314-manylinux_2_17_aarch64.manylinux2014_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/26/ca/1ad30103535cf0cecd7b993e8d50edc5351b1820e38f2d22e3df58962feb/${WHEEL}"
SRC_URI[sha256sum] = "7a7ce236284f002a156f70add88efe5c70879cccbb658be0822c54b1306fc09d"

inherit python_wheel
