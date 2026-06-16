SUMMARY = "Updated polling utility with many configurable options"
HOMEPAGE = "https://pypi.org/project/polling2/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://polling2-0.5.0.dist-info/LICENSE.md;md5=7ab8a3809e3df8bed4ed40738f5c1769"

WHEEL = "polling2-${PV}-py2.py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/a3/de/e5bf2556ebd6db12590788207575c7c75b1de62f5ddc8b4916b668e04e6b/${WHEEL}"
SRC_URI[sha256sum] = "ad86d56fbd7502f0856cac2d0109d595c18fa6c7fb12c88cee5e5d16c17286c1"

inherit python_wheel
