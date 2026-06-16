SUMMARY = "A simple interface to GPIO devices with Raspberry Pi"
HOMEPAGE = "https://pypi.org/project/gpiozero/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://gpiozero-2.0.1.dist-info/LICENSE.rst;md5=f7edfe7aeac02cb6c394726db07eb41c"

WHEEL = "gpiozero-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/02/eb/6518a1b00488d48995034226846653c382d676cf5f04be62b3c3fae2c6a1/${WHEEL}"
SRC_URI[sha256sum] = "8f621de357171d574c0b7ea0e358cb66e560818a47b0eeedf41ce1cdbd20c70b"

inherit python_wheel
