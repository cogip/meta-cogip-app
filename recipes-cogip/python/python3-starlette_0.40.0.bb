SUMMARY = "Starlette ASGI framework (prebuilt wheel, fastapi 0.115-compatible)"
HOMEPAGE = "https://www.starlette.io"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://starlette-0.40.0.dist-info/licenses/LICENSE.md;md5=11e8c8dbfd5fa373c703de492140ff7a"

# Pinned <0.41 (fastapi 0.115.2 requires >=0.37.2,<0.41.0) and, crucially,
# still supports the legacy TemplateResponse(name, context) call the cogip
# dashboard uses. The OE starlette is 1.0, which removed it.
WHEEL = "starlette-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/0a/0f/64baf7a06492e8c12f5c4b49db286787a7255195df496fc21f5fd9eecffa/${WHEEL}"
SRC_URI[sha256sum] = "c494a22fae73805376ea6bf88439783ecfba9aac88a43911b48c653437e784c4"

inherit python_wheel

RDEPENDS:${PN} += "python3-anyio"
