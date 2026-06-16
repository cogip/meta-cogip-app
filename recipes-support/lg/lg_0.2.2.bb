SUMMARY = "lg - Linux SBC GPIO/I2C/SPI/serial library (liblgpio)"
DESCRIPTION = "joan2937/lg: the C library behind the lgpio Python module, the \
GPIO backend gpiozero uses on the Raspberry Pi (planner LEDs / flag motor). \
The python3-lgpio sdist only ships the SWIG wrapper and links -llgpio, so the \
native rootfs needs this library (Debian provides it as liblgpio-dev)."
HOMEPAGE = "https://github.com/joan2937/lg"
LICENSE = "Unlicense"
LIC_FILES_CHKSUM = "file://UNLICENCE;md5=61287f92700ec1bdf13bc86d8228cd13"

SRC_URI = "git://github.com/joan2937/lg.git;branch=master;protocol=https"
SRCREV = "b959a17d723360e85648316757b02dbea9902feb"

# lg predates GCC 14 / C23 defaults: build as gnu17 so an empty () prototype
# still means "unspecified args" (C23 makes it (void), breaking lg's K&R-style
# function-pointer calls), and keep -Wincompatible-pointer-types a warning.
CFLAGS:append = " -std=gnu17 -Wno-error=incompatible-pointer-types"

# Makefile honours CC/CFLAGS/LDFLAGS. Build only the shared library (not the
# rgpiod daemon / rgs / docs, which we do not need and would drag in extras).
do_compile() {
    # The Makefile uses `CC ?= ...`, a no-op against make's built-in CC, so it
    # compiles with the host gcc unless CC is forced on the command line.
    # Target liblgpio.so produces liblgpio.so.1 + symlink. Neutralise the
    # Makefile's strip/size steps -- Yocto strips and packages itself.
    oe_runmake liblgpio.so CC="${CC}" STRIPLIB=true SIZE=true
}

do_install() {
    install -d ${D}${libdir} ${D}${includedir}
    install -m 0755 liblgpio.so.1 ${D}${libdir}/liblgpio.so.1
    ln -sf liblgpio.so.1 ${D}${libdir}/liblgpio.so
    install -m 0644 lgpio.h ${D}${includedir}/lgpio.h
}
