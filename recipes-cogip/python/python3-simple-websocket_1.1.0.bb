SUMMARY = "Simple WebSocket server and client for Python"
HOMEPAGE = "https://pypi.org/project/simple-websocket/"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://simple_websocket-1.1.0.dist-info/LICENSE;md5=9d272c9fe2437531b5bbecf4fcc82e24"

WHEEL = "simple_websocket-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/52/59/0782e51887ac6b07ffd1570e0364cf901ebc36345fea669969d2084baebb/${WHEEL}"
SRC_URI[sha256sum] = "4af6069630a38ed6c561010f0e11a5bc0d4ca569b36306eb257cd9a192497c8c"

inherit python_wheel
