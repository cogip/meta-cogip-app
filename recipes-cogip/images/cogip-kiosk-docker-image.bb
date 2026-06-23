require cogip-kiosk-image.inc

# Docker variant: the Cogip tools run as containers (docker compose), the
# image is pre-loaded into the Docker store on /data. Kept alongside the
# native cogip-kiosk-image for fallback / comparison.

# 3-partition layout (boot / root / data); /data holds the Docker storage,
# raw-copied from the pre-loaded cogip-data.ext4 (built by `make app-data`)
# so first boot skips `docker load`. A bare kiosk (COGIP_APP=0) keeps an
# empty /data.
WKS_FILE = "${@'cogip-sdimage-app.wks' if d.getVar('COGIP_APP') in ('1', 'yes', 'true') else 'cogip-sdimage.wks'}"

# Docker runtime + the containerized application stack. The pre-loaded image
# tarball (cogip-app-image) is staged by `make app-image` first (the Makefile
# build target enforces this when COGIP_APP=1).
IMAGE_INSTALL += "${@bb.utils.contains_any('COGIP_APP', '1 yes true', ' \
    docker-moby \
    docker-compose \
    cogip-data-mount \
    cogip-docker-conf \
    cogip-environment \
    cogip-app-venv \
    cogip-services-docker \
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

# Disk size: the rootfs holds the embedded container image tarball (~1 GB)
# AND, at first boot, the seeded editable venv /opt/.venv (~1 GB). The loaded
# docker graph and mutable data live on the separate /data partition.
IMAGE_ROOTFS_SIZE       = "524288"
IMAGE_ROOTFS_EXTRA_SPACE = "3145728"
