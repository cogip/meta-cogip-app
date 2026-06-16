SUMMARY = "A simple package for fast JPEG encoding and decoding."
HOMEPAGE = "https://pypi.org/project/simplejpeg/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://simplejpeg-1.9.0.dist-info/licenses/LICENSE;md5=e4dfcdee2922505bffec7961bdc8ecec"

WHEEL = "simplejpeg-${PV}-cp314-cp314-manylinux2014_aarch64.manylinux_2_17_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/d4/32/fe632d5709e4a278a73f99539a94fdecf9d48969b8b3b94ba9940d8fcb9d/${WHEEL}"
SRC_URI[sha256sum] = "2192faf8efa84de5965da7336cf4c358c395f06a67ad87b85d513eea52d860c7"

inherit python_wheel
