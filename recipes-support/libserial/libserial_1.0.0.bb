SUMMARY = "C++ serial port library (crayzeewulf LibSerial)"
DESCRIPTION = "LibSerial provides C++ classes for accessing serial ports on \
POSIX systems. Required by the cogip-tools lidar drivers (lidar_ld19, \
ydlidar_g2 under cogip.cpp), which link it via pkg-config (libserial). On the \
Docker target it comes from Debian's libserial-dev; the native rootfs needs \
this recipe to ship libserial.so."
HOMEPAGE = "https://github.com/crayzeewulf/libserial"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=fdcb31da81c085fd445062a1eb929089"

# ABI must match the cogip.cpp wheel, which links Debian 13's libserial-dev
# (1.0.0+git20250423-6). Match Debian exactly: pin its base commit
# (git20250423 = a471ae8) and apply Debian's sole patch, which reverts the
# 2024-07 `exclusive` parameter (commit 283a0fe, an ABI break). The result is
# the 2-arg SerialPort::Open(string const&, ios_base::openmode const&) the
# wheel imports; plain master (3-arg) does not export it -> undefined symbol.
SRC_URI = " \
    git://github.com/crayzeewulf/libserial.git;branch=master;protocol=https \
    file://revert-283a0fe-exclusive.patch \
"
SRCREV = "a471ae8dee54e8770e4d47e60fba4acf29d8e7ad"

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
