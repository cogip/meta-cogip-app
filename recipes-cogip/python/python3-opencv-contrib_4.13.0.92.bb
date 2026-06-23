SUMMARY = "OpenCV + contrib Python bindings, headless (prebuilt wheel, cv2/aruco)"
HOMEPAGE = "https://opencv.org"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://opencv_contrib_python_headless-4.13.0.92.dist-info/LICENSE.txt;md5=c4a59ea6fdfe49caa8470881ba0a6ffa"

# Headless variant: cogip uses cv2 for image processing (aruco, drawing,
# imencode), never the highgui GUI on the robot. The non-headless wheel's cv2
# links libxcb/X11 (highgui), absent on this no-X kiosk image -> ImportError
# on the camera tools. The headless build drops the GUI deps entirely.
WHEEL = "opencv_contrib_python_headless-${PV}-cp37-abi3-manylinux2014_aarch64.manylinux_2_17_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/14/84/e6b3568f9147b4f114e881fb0e733fd97bdca15452feba78b510351584d1/${WHEEL}"
SRC_URI[sha256sum] = "449c1f00a685a3a7dff8d6fa93a70fbfe0de5537c24358ea03a1d996d12b33e8"

inherit python_wheel

RDEPENDS:${PN} += "python3-numpy"

# cogip-tools depends on opencv-python AND opencv-contrib-python; this single
# wheel is the superset (includes aruco), so it provides the python3-opencv
# package the closure asks for. PREFERRED_RPROVIDER (in cogip.conf) makes it
# win over the OE opencv C++ source recipe, which is then never built.
RPROVIDES:${PN} += "python3-opencv"
