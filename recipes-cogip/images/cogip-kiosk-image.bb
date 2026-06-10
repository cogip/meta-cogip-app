SUMMARY = "Cogip kiosk image for Raspberry Pi 4 (Cog/WPE WebKit on Wayland)"
DESCRIPTION = "Minimal Wayland image that boots straight into Cog \
displaying a local web page. Replaces the Debian/Raspios-based kiosk."
LICENSE = "MIT"

inherit core-image

# This layer owns the image and its partitioning: a 3-partition layout
# (boot / root / data), the data partition holding Docker storage and
# mutable on-device data. See wic/cogip-sdimage.wks.
WKS_FILE = "cogip-sdimage.wks"

# Image features kept intentionally small to favour boot time. ssh-server
# is included for field debugging; drop it once the unit is stable.
# package-management deliberately left out: the kiosk image is immutable,
# upgrades happen by reflashing.
#
# debug-tweaks: empty root password so you can log in on the serial
# console / an HDMI VT (Ctrl+Alt+F2) / over SSH to debug. REMOVE for
# production -- it allows passwordless root login.
IMAGE_FEATURES += " \
    ssh-server-openssh \
    debug-tweaks \
"

# Cog runs in its DRM platform (see meta-cogip/.../cog_%.bbappend) so
# there's no Wayland compositor in the image: WPE WebKit talks directly
# to /dev/dri/card0 via Mesa GBM. Minimal, fast, hardware-accelerated.
IMAGE_INSTALL += " \
    kernel-modules \
    linux-firmware-rpidistro-bcm43455 \
    wireless-regdb-static \
    rfkill \
    cog \
    wpewebkit \
    mesa \
    libdrm \
    libinput \
    fontconfig \
    ttf-dejavu-sans \
    cog-service \
    cogip-net \
    wpa-supplicant \
    systemd \
"

# Docker runtime + the Cogip containerized application stack. The
# pre-loaded image tarball (cogip-app-image) and its first-boot loader
# (cogip-app-load) are always included; the tarball must be staged by
# `make app-image` first (the Makefile build target enforces this).
IMAGE_INSTALL += " \
    docker-moby \
    docker-compose \
    cogip-data-mount \
    cogip-docker-conf \
    cogip-services \
    cogip-app-image \
    cogip-app-load \
"

# Strip development tooling: kernel-dev, gdb, etc. Image is reflashed,
# not patched on-target.
IMAGE_LINGUAS = "en-us"

# Disk size: rootfs stays lean (system + the embedded container image
# tarball ~1 GB). The loaded docker graph and mutable data live on the
# separate /data partition, not here. The container tar dominates, so
# allow ~1.5 GB of extra space on top of the base rootfs.
IMAGE_ROOTFS_SIZE       = "524288"
IMAGE_ROOTFS_EXTRA_SPACE = "1572864"
