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

# The kiosk renderer is WPE WebKit's MiniBrowser on the WPE Platform DRM
# backend (see cogip-kiosk-browser / the wpewebkit bbappend): WPE talks
# straight to the vc4-kms DRM node, no compositor. cog itself is NOT
# installed -- its wpebackend-fdo DRM path SEGVs against WPE 2.52.
IMAGE_INSTALL += " \
    kernel-modules \
    linux-firmware-rpidistro-bcm43455 \
    wireless-regdb-static \
    rfkill \
    wpewebkit \
    mesa \
    libdrm \
    libinput \
    fontconfig \
    ttf-dejavu-sans \
    cogip-kiosk-browser \
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

# Console policy: keep getty@tty1 off so the kiosk owns the HDMI VT / DRM
# master. A "disable" preset is ignored -- OE runs `preset-all` with
# --preset-mode=enable-only, so disables are no-ops -- and getty@tty1 is
# enabled by a static wants symlink anyway. Only a mask (-> /dev/null)
# wins, but masking before preset-all makes it fail ("Unit ... is
# masked"). So mask it at the END of systemd_handle_machine_id, i.e. right
# AFTER its two preset-all calls.
#
# serial-getty@ttyS0 is masked for the same reason but a different goal:
# it is enabled by a static getty.target wants symlink, yet /dev/ttyS0
# (the mini-UART) never appears -- the console is ttyAMA0 and the cmdline
# sets 8250.nr_uarts=1. So dev-ttyS0.device hits its 90s device timeout
# and gates multi-user.target, pushing full boot from ~22s to ~97s. Mask
# it so the device is never waited on.
# systemd-networkd-wait-online is masked too: every cogip tool and docker
# itself pull network-online.target, which blocks on wait-online until an
# interface is routable (~4-7s). Nothing here needs external network at
# boot -- all tools run with network_mode: host (localhost + CAN) and the
# Docker image is already local -- so masking it makes network-online.target
# passive (reached immediately), trimming the critical path. Side effect:
# network-online becomes instant system-wide; revisit if a future service
# genuinely needs the external network up before it starts.
systemd_handle_machine_id:append() {
    install -d ${IMAGE_ROOTFS}${sysconfdir}/systemd/system
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/getty@tty1.service
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/serial-getty@ttyS0.service
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/systemd-networkd-wait-online.service
}

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
