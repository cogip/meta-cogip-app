SUMMARY = "Human friendly interface to linux subsystems using python"
HOMEPAGE = "https://pypi.org/project/linuxpy/"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://linuxpy-0.24.0.dist-info/licenses/LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464"

WHEEL = "linuxpy-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/8a/d8/6ce2855fda8123ff7d127821743de7aa1609cda025183b9c4046d5b36205/${WHEEL}"
SRC_URI[sha256sum] = "4e2ce8da45a7a7171bcdcf6b7ab53858d9a83108e24874bd9ddb50a73590a1c6"

inherit python_wheel
