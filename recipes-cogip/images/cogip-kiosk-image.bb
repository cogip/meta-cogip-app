require cogip-kiosk-image.inc

# Native variant (default): the Cogip tools run directly on the system
# Python 3.14 (cogip-tools + its full dependency closure installed in
# site-packages), managed by systemd -- no Docker, no container image, no
# /opt/.venv seed. The Docker variant is kept as cogip-kiosk-docker-image.

# 2-partition layout (boot / root): no Docker store, so no /data partition.
WKS_FILE = "cogip-sdimage.wks"

# The native application stack. cogip-services (native units) pulls in
# cogip-tools -> packagegroup-cogip-python (the full runtime closure) +
# packagegroup-cogip-native-deps (libcamera/pykms/systemd/prctl/libserial) +
# cogip-environment (per-role config). One entry is enough.
IMAGE_INSTALL += "${@bb.utils.contains_any('COGIP_APP', '1 yes true', ' cogip-services ', '', d)}"

# Disk size: the rootfs holds Python 3.14 + the scientific/runtime closure
# (numpy, opencv, scipy, scikit-learn, matplotlib, ...) installed natively.
# No container tarball and no venv seed, but the site-packages are sizable.
IMAGE_ROOTFS_EXTRA_SPACE = "2097152"
