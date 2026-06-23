SUMMARY = "Pillow imaging library (prebuilt wheel)"
HOMEPAGE = "https://python-pillow.org"
LICENSE = "MIT-CMU"
LIC_FILES_CHKSUM = "file://pillow-12.2.0.dist-info/licenses/LICENSE;md5=94edb85f19171bc4ac253a74208c0e39"

WHEEL = "pillow-${PV}-cp314-cp314-manylinux2014_aarch64.manylinux_2_17_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/70/62/98f6b7f0c88b9addd0e87c217ded307b36be024d4ff8869a812b241d1345/${WHEEL}"
SRC_URI[sha256sum] = "22db17c68434de69d8ecfc2fe821569195c0c373b25cccb9cbdacf2c6e53c601"

inherit python_wheel
