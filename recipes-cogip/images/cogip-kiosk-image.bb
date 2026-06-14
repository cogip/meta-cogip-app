SUMMARY = "Cogip kiosk image for Raspberry Pi 4 (Cog/WPE WebKit on Wayland)"
DESCRIPTION = "Minimal Wayland image that boots straight into Cog \
displaying a local web page. Replaces the Debian/Raspios-based kiosk."
LICENSE = "MIT"

inherit core-image

# This layer owns the image and its partitioning: a 3-partition layout
# (boot / root / data), the data partition holding Docker storage and
# mutable on-device data.
#
# With the Docker app stack (COGIP_APP), use the variant whose /data is
# raw-copied from cogip-data.ext4 (image pre-loaded into the Docker store,
# built by `make app-data`) so first boot skips `docker load`. A bare
# kiosk (COGIP_APP=0) keeps an empty /data.
WKS_FILE = "${@'cogip-sdimage-app.wks' if d.getVar('COGIP_APP') in ('1', 'yes', 'true') else 'cogip-sdimage.wks'}"

# Image features kept intentionally small to favour boot time. ssh-server
# is included for field debugging; drop it once the unit is stable.
# package-management deliberately left out: the kiosk image is immutable,
# upgrades happen by reflashing.
#
# Passwordless root login for field debugging (serial / HDMI VT / SSH).
# On wrynose the `debug-tweaks` bundle is gone -- use its granular
# replacements. REMOVE these for production.
IMAGE_FEATURES += " \
    ssh-server-openssh \
    allow-empty-password \
    allow-root-login \
    empty-root-password \
    post-install-logging \
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

# Docker runtime + the Cogip containerized application stack. Gated on
# COGIP_APP (default 1): set COGIP_APP=0 to build a bare kiosk image
# (Cog + networking only, no Docker, no cogip-tools container, no app
# tarball needed) -- handy for isolating Wi-Fi / display bring-up.
# The pre-loaded image tarball (cogip-app-image) is staged by
# `make app-image` first (the Makefile build target enforces this when
# COGIP_APP=1).
COGIP_APP ??= "1"
IMAGE_INSTALL += "${@bb.utils.contains_any('COGIP_APP', '1 yes true', ' \
    docker-moby \
    docker-compose \
    cogip-data-mount \
    cogip-docker-conf \
    cogip-environment \
    cogip-app-venv \
    cogip-services \
    cogip-app-image \
    cogip-app-load \
', '', d)}"

# When the app stack is built, the data partition is raw-copied from the
# pre-loaded cogip-data.ext4 (built by `make app-data`, sitting in DL_DIR).
# wic's rawcopy resolves `file=` from DEPLOY_DIR_IMAGE, so symlink the
# multi-GB blob there just before wic -- a symlink, NOT a copy, to avoid
# routing several GB through a recipe/sstate (which fills the disk).
cogip_stage_data_ext4() {
    case "${COGIP_APP}" in
        1|yes|true)
            if [ ! -f "${DL_DIR}/cogip-data.ext4" ]; then
                bbfatal "cogip-data.ext4 missing in DL_DIR -- run 'make app-data'"
            fi
            ln -sf "${DL_DIR}/cogip-data.ext4" "${DEPLOY_DIR_IMAGE}/cogip-data.ext4"
            ;;
    esac
}
do_image_wic[prefuncs] += "cogip_stage_data_ext4"

# Strip development tooling: kernel-dev, gdb, etc. Image is reflashed,
# not patched on-target.
IMAGE_LINGUAS = "en-us"

# Disk size: the rootfs holds the embedded container image tarball
# (~1 GB) AND, at first boot, the seeded editable venv /opt/.venv
# (cogip-app-load docker-cp's it out of the image, ~1 GB). The loaded
# docker graph and mutable data live on the separate /data partition.
# Reserve enough free space for both the tar and the runtime venv seed.
IMAGE_ROOTFS_SIZE       = "524288"
IMAGE_ROOTFS_EXTRA_SPACE = "3145728"
