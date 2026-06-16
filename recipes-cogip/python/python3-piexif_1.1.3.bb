SUMMARY = "To simplify exif manipulations with python. Writing, reading, and more"
HOMEPAGE = "https://pypi.org/project/piexif/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://piexif-1.1.3.dist-info/LICENSE.txt;md5=de3294fd570f87ece3b7ab7ccd61a108"

WHEEL = "piexif-${PV}-py2.py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/2c/d8/6f63147dd73373d051c5eb049ecd841207f898f50a5a1d4378594178f6cf/${WHEEL}"
SRC_URI[sha256sum] = "3bc435d171720150b81b15d27e05e54b8abbde7b4242cddd81ef160d283108b6"

inherit python_wheel
