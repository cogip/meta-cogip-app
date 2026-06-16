SUMMARY = "Extremely lightweight compatibility layer between dataframe libraries"
HOMEPAGE = "https://pypi.org/project/narwhals/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://narwhals-2.22.1.dist-info/licenses/LICENSE.md;md5=635dc68cb70b18de784113020c6d814e"

WHEEL = "narwhals-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/48/ca/36339329c4604adbcc99c899b7eb1ce1a555c499b6a6860757dc9bfed36d/${WHEEL}"
SRC_URI[sha256sum] = "60567d774edf77db53906f89d9fbd164e66e56d66d388e1e6990f17ac33cfb53"

inherit python_wheel
