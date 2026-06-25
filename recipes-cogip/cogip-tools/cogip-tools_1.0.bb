SUMMARY = "COGIP robotics tools (the cogip Python package)"
DESCRIPTION = "The cogip-tools package itself: the Python sources plus the \
compiled cogip.cpp extension (abi3 wheel), installed into the target's \
Python 3.14 site-packages. Runs natively (no Docker). The .py sources land in \
site-packages and stay editable on the rootfs. A --system-site-packages venv \
at /opt/.venv (uv) sits on top so a dependency can be tried on the board with \
`uv pip install` without rebuilding the image; the services run the venv \
python so an added dep is picked up on restart."
LICENSE = "CLOSED"

# The cross-built abi3 wheel (cp313-abi3 imports on 3.14), produced by
# cogip-tools' build_wheel step and staged into DL_DIR by the Makefile.
WHEEL = "cogip_tools-1.0.0-cp313-abi3-linux_aarch64.whl"
FILESEXTRAPATHS:prepend := "${DL_DIR}:"
SRC_URI = "file://${WHEEL}"

inherit python_wheel

# Experimentation venv root.
VENV = "/opt/.venv"

# cogip + the heavy ABI-correct deps live in the SYSTEM site-packages (the
# wheel install above + packagegroup-cogip-python). We lay a
# --system-site-packages venv on top at /opt/.venv: it inherits everything
# from the system and only its own site-packages is writable, so Eric can
# `uv pip install <pkg>` on the board to try a dependency without rebuilding
# the image. The services launch with `uv run` against this venv (VIRTUAL_ENV
# = /opt/.venv), so an added dep is picked up on `systemctl restart
# cogip@<tool>`.
#
# The venv is laid down statically here (no python executed at build, so it is
# cross-safe): a --system-site-packages venv is just pyvenv.cfg + the
# interpreter symlinks + an empty writable site-packages. uv recognises it via
# pyvenv.cfg; the absolute venv python self-detects the venv at startup.
do_install:append() {
    install -d ${D}${VENV}/bin
    install -d ${D}${VENV}/lib/${PYTHON_DIR}/site-packages

    cat > ${D}${VENV}/pyvenv.cfg <<EOF
home = /usr/bin
include-system-site-packages = true
version = ${PYTHON_BASEVERSION}
EOF

    # Interpreter chain -> the base python on the rootfs.
    ln -sf /usr/bin/${PYTHON_DIR} ${D}${VENV}/bin/${PYTHON_DIR}
    ln -sf ${PYTHON_DIR}          ${D}${VENV}/bin/python3
    ln -sf python3                ${D}${VENV}/bin/python

    # Minimal POSIX-sh activate for interactive shells. Services use the
    # absolute venv python and uv finds pyvenv.cfg on its own, so neither
    # needs this -- it is only for `source /opt/.venv/bin/activate`.
    cat > ${D}${VENV}/bin/activate <<'EOF'
# cogip experimentation venv (sourced, not executed).
VIRTUAL_ENV="/opt/.venv"; export VIRTUAL_ENV
_OLD_VIRTUAL_PATH="$PATH"; export _OLD_VIRTUAL_PATH
PATH="$VIRTUAL_ENV/bin:$PATH"; export PATH
deactivate() {
    PATH="$_OLD_VIRTUAL_PATH"; export PATH
    unset VIRTUAL_ENV _OLD_VIRTUAL_PATH
    unset -f deactivate
  }
EOF

    # Console scripts (cogip-server, cogip-planner, ...) regenerated from the
    # wheel's entry_points -- the wheel is unzipped, not pip-installed, so they
    # do not exist. Shebang is the VENV python so each tool sees the system
    # packages AND any uv-installed experiment. The `if __name__ == "__main__"`
    # guard is REQUIRED: Python 3.14 defaults multiprocessing to a non-fork
    # start method, so the spawned children re-import this script as __main__;
    # without the guard main() re-runs and re-spawns -> RuntimeError (planner
    # avoidance subprocess).
    epfile=$(ls ${S}/cogip_tools-*.dist-info/entry_points.txt)
    sed -n '/^\[console_scripts\]/,/^\[/p' "$epfile" | grep '=' | while read line; do
        name=$(echo "$line" | cut -d= -f1 | tr -d ' ')
        spec=$(echo "$line" | cut -d= -f2 | tr -d ' ')
        module=$(echo "$spec" | cut -d: -f1)
        func=$(echo "$spec" | cut -d: -f2)
        printf '#!%s/bin/python\nimport sys\nfrom %s import %s\nif __name__ == "__main__":\n    sys.exit(%s())\n' \
            "${VENV}" "$module" "$func" "$func" > ${D}${VENV}/bin/$name
        chmod 0755 ${D}${VENV}/bin/$name
    done

    # Drop interactive shells into the venv (parity with raspios).
    install -d ${D}${sysconfdir}/profile.d
    printf '. %s/bin/activate\n' "${VENV}" > ${D}${sysconfdir}/profile.d/cogip-venv.sh
}

FILES:${PN} += "${VENV} ${sysconfdir}/profile.d/cogip-venv.sh"

# cogip.cpp's lidar drivers link libserial; the tools need the full Python
# closure and the non-pip system bindings. uv is the on-device package manager
# for the /opt/.venv experimentation venv.
RDEPENDS:${PN} += " \
    packagegroup-cogip-python \
    packagegroup-cogip-native-deps \
    libserial \
    uv \
"
