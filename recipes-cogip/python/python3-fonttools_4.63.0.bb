SUMMARY = "Tools to manipulate font files"
HOMEPAGE = "https://pypi.org/project/fonttools/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://fonttools-4.63.0.dist-info/licenses/LICENSE;md5=211c9e4671bde3881351f22a2901f692"

WHEEL = "fonttools-${PV}-cp314-cp314-manylinux2014_aarch64.manylinux_2_17_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/dd/87/64cfa18a7a1621d17b7f4502b2b0ed8a135a90c3db51ea590ee99043e76b/${WHEEL}"
SRC_URI[sha256sum] = "6b2248c5decb223562f7902ff6325077a073f608ee8e33e88ad88db734eb9f49"

inherit python_wheel
