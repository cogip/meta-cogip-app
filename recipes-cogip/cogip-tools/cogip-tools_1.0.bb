SUMMARY = "COGIP robotics tools (the cogip Python package)"
DESCRIPTION = "The cogip-tools package itself: the Python sources plus the \
compiled cogip.cpp extension (abi3 wheel), installed into the target's \
Python 3.14 site-packages. Runs natively on /usr/bin/python3 with the deps \
from packagegroup-cogip-python and the system bindings -- no venv, no Docker. \
The .py sources land in site-packages and stay editable on the rootfs."
LICENSE = "CLOSED"

# The cross-built abi3 wheel (cp313-abi3 imports on 3.14), produced by
# cogip-tools' build_wheel step and staged into DL_DIR by the Makefile.
WHEEL = "cogip_tools-1.0.0-cp313-abi3-linux_aarch64.whl"
FILESEXTRAPATHS:prepend := "${DL_DIR}:"
SRC_URI = "file://${WHEEL}"

inherit python_wheel

# The wheel ships entry_points but, unzipped (not pip-installed), the console
# scripts are not generated. Recreate /usr/bin/cogip-* from entry_points.txt
# (what pip would do), so systemd can launch the tools natively.
do_install:append() {
    install -d ${D}${bindir}
    epfile=$(ls ${S}/cogip_tools-*.dist-info/entry_points.txt)
    sed -n '/^\[console_scripts\]/,/^\[/p' "$epfile" | grep '=' | while read line; do
        name=$(echo "$line" | cut -d= -f1 | tr -d ' ')
        spec=$(echo "$line" | cut -d= -f2 | tr -d ' ')
        module=$(echo "$spec" | cut -d: -f1)
        func=$(echo "$spec" | cut -d: -f2)
        # The `if __name__ == "__main__"` guard is REQUIRED: Python 3.14
        # defaults multiprocessing to a non-fork start method, so the spawned
        # children re-import this script as __main__; without the guard main()
        # re-runs and re-spawns -> RuntimeError (planner avoidance subprocess).
        printf '#!/usr/bin/python3\nimport sys\nfrom %s import %s\nif __name__ == "__main__":\n    sys.exit(%s())\n' \
            "$module" "$func" "$func" > ${D}${bindir}/$name
        chmod 0755 ${D}${bindir}/$name
    done
}

# cogip.cpp's lidar drivers link libserial; the tools need the full Python
# closure and the non-pip system bindings.
RDEPENDS:${PN} += " \
    packagegroup-cogip-python \
    packagegroup-cogip-native-deps \
    libserial \
"
