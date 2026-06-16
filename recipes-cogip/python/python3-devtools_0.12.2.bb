SUMMARY = "Python's missing debug print command, and more."
HOMEPAGE = "https://pypi.org/project/devtools/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://devtools-0.12.2.dist-info/licenses/LICENSE;md5=3c69d58fa5f4ff0b4244368d39546751"

WHEEL = "devtools-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/d1/ae/afb1487556e2dc827a17097aac8158a25b433a345386f0e249f6d2694ccb/${WHEEL}"
SRC_URI[sha256sum] = "c366e3de1df4cdd635f1ad8cbcd3af01a384d7abda71900e68d43b04eb6aaca7"

inherit python_wheel
