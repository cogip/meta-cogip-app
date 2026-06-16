SUMMARY = "threadpoolctl"
HOMEPAGE = "https://pypi.org/project/threadpoolctl/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://threadpoolctl-3.6.0.dist-info/licenses/LICENSE;md5=8f2439cfddfbeebdb5cac3ae4ae80eaf"

WHEEL = "threadpoolctl-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/32/d5/f9a850d79b0851d1d4ef6456097579a9005b31fea68726a4ae5f2d82ddd9/${WHEEL}"
SRC_URI[sha256sum] = "43a0b8fd5a2928500110039e43a5eed8480b918967083ea48dc3ab9f13c4a7fb"

inherit python_wheel
