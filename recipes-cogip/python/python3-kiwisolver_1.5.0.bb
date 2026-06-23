SUMMARY = "Kiwisolver Cassowary constraint solver (prebuilt wheel)"
HOMEPAGE = "https://github.com/nucleic/kiwi"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://kiwisolver-1.5.0.dist-info/licenses/LICENSE;md5=7a2c756dc2da8fbde2a254cae9a9320e"

WHEEL = "kiwisolver-${PV}-cp314-cp314-manylinux_2_24_aarch64.manylinux_2_28_aarch64.whl"
SRC_URI = "https://files.pythonhosted.org/packages/6b/f0/f768ae564a710135630672981231320bc403cf9152b5596ec5289de0f106/${WHEEL}"
SRC_URI[sha256sum] = "4e7f886f47ab881692f278ae901039a234e4025a68e6dfab514263a0b1c4ae05"

inherit python_wheel
