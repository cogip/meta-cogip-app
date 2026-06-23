SUMMARY = "OpenCV + contrib Python bindings (prebuilt wheel, provides cv2/aruco)"
HOMEPAGE = "https://opencv.org"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://opencv_contrib_python-4.13.0.92.dist-info/LICENSE.txt;md5=c4a59ea6fdfe49caa8470881ba0a6ffa"

WHEEL = "opencv_contrib_python-${PV}-cp37-abi3-manylinux2014_aarch64.manylinux_2_17_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/ff/7a/fe87eaf109b454af4a2579f46958b3cafb0f804b9c788c108760723a9bb7/${WHEEL}"
SRC_URI[sha256sum] = "5f9cb522dd9e465dfca3536c15288f7936b9827432fb9c885eaf94dc5f88c2a3"

inherit python_wheel

RDEPENDS:${PN} += "python3-numpy"

# cogip-tools depends on opencv-python AND opencv-contrib-python; this single
# wheel is the superset (includes aruco), so it provides the python3-opencv
# package the closure asks for. PREFERRED_RPROVIDER (in cogip.conf) makes it
# win over the OE opencv C++ source recipe, which is then never built.
RPROVIDES:${PN} += "python3-opencv"
