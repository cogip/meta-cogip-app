SUMMARY = "Linux SBC GPIO module (lgpio)"
HOMEPAGE = "http://abyz.me.uk/lg/py_lgpio.html"
LICENSE = "Unlicense"
LIC_FILES_CHKSUM = "file://LICENSE;md5=61287f92700ec1bdf13bc86d8228cd13"

# C extension bundling the lg library; cross-compiled from the PyPI sdist
# (no aarch64 wheel published). gpiozero uses it as its Pi GPIO backend
# (planner LEDs / flag motor).
PYPI_PACKAGE = "lgpio"
SRC_URI[sha256sum] = "11372e653b200f76a0b3ef8a23a0735c85ec678a9f8550b9893151ed0f863fff"

# lgpio generates its Python bindings with SWIG, and links the lg C library
# (liblgpio), which the sdist does not bundle -- provided by the lg recipe.
DEPENDS += "swig-native lg"
RDEPENDS:${PN} += "lg"

inherit pypi setuptools3
