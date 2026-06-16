SUMMARY = "Settings management using Pydantic"
HOMEPAGE = "https://pypi.org/project/pydantic-settings/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://pydantic_settings-2.14.1.dist-info/licenses/LICENSE;md5=9adde1a30a7e74a03e57e456551c19ae"

WHEEL = "pydantic_settings-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/ae/8d/f1af3832f5e6eb13ba94ee809e72b8ecb5eef226d27ee0bef7d963d943c7/${WHEEL}"
SRC_URI[sha256sum] = "6e3c7edfd8277687cdc598f56e5cff0e9bfff0910a3749deaa8d4401c3a2b9de"

inherit python_wheel
