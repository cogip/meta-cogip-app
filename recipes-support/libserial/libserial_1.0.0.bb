SUMMARY = "C++ serial port library (crayzeewulf LibSerial)"
DESCRIPTION = "LibSerial provides C++ classes for accessing serial ports on \
POSIX systems. Required by the cogip-tools lidar drivers (lidar_ld19, \
ydlidar_g2 under cogip.cpp), which link it via pkg-config (libserial). On the \
Docker target it comes from Debian's libserial-dev; the native rootfs needs \
this recipe to ship libserial.so."
HOMEPAGE = "https://github.com/crayzeewulf/libserial"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=fdcb31da81c085fd445062a1eb929089"

# No upstream release tags; pin a master commit. project() declares 1.0.0.
SRC_URI = "git://github.com/crayzeewulf/libserial.git;branch=master;protocol=https"
SRCREV = "50e0f443666d48d7c7e181dc73a6b35700517fae"

inherit cmake

# Ship only the C++ shared library + headers + pkg-config file. The defaults
# build extras we do not need and which would drag in build deps: unit tests
# (Boost + gtest), example programs, Doxygen docs, and the Python SIP bindings
# (cogip.cpp links the C++ library directly, not the SIP module).
EXTRA_OECMAKE = " \
    -DLIBSERIAL_ENABLE_TESTING=OFF \
    -DLIBSERIAL_BUILD_EXAMPLES=OFF \
    -DLIBSERIAL_BUILD_DOCS=OFF \
    -DLIBSERIAL_PYTHON_ENABLE=OFF \
    -DINSTALL_STATIC=OFF \
"
