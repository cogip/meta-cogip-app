SUMMARY = "System dependencies for the native (no-Docker) Cogip tools venv"
DESCRIPTION = "Pulls onto the rootfs the libraries and Python modules the \
cogip-tools venv cannot obtain from pip: C extensions that build against \
system libraries and have no aarch64 wheel (systemd-python, python-prctl), \
the camera bindings that are not on PyPI (libcamera pycamera, kmsxx pykms), \
and the libserial C++ library the lidar drivers link. With these present, a \
Python 3.14 venv created --system-site-packages can import them, while the \
pip-installable deps (numpy, scipy, opencv, scikit-learn, fastapi, ...) are \
cross-installed into the venv itself."
LICENSE = "MIT"

inherit packagegroup

# Non-pip dependencies, provided by the system (Yocto), matching the wrynose
# system Python so the venv sees them via --system-site-packages.
#  - python3-systemd / python3-prctl: C extensions, sdist-only on PyPI (no
#    aarch64 wheel) -> must come from Yocto, not pip.
#  - libcamera-pycamera / kmsxx-python: libcamera + pykms bindings (picamera2
#    deps), not published on PyPI.
#  - libserial1: crayzeewulf LibSerial, linked by cogip.cpp's lidar drivers.
# NOTE: 'getch' (tiny C ext, sdist-only, no Yocto recipe yet) is still missing;
# add a recipe or drop the dep if it is unused on the headless robot.
RDEPENDS:${PN} = " \
    python3 \
    python3-systemd \
    python3-prctl \
    libcamera-pycamera \
    kmsxx-python \
    libserial \
"
