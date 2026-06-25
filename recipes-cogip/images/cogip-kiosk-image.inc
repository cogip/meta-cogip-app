SUMMARY = "Cogip kiosk image for Raspberry Pi 4 (WPE WebKit on DRM/KMS)"
DESCRIPTION = "Minimal image that boots straight into WPE WebKit's MiniBrowser \
displaying the local dashboard. Replaces the Debian/Raspios-based kiosk. \
Common base shared by the native (cogip-kiosk-image) and Docker \
(cogip-kiosk-docker-image) variants."
LICENSE = "MIT"

inherit core-image

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
# No kernel-modules: the mainline kernel (meta-cogip-bsp linux-stable) is
# monolithic, everything =y. No linux-firmware-rpidistro-bcm43455: the BCM43455
# wifi firmware is embedded in the kernel image (CONFIG_EXTRA_FIRMWARE). The
# raspberrypi machine RRECOMMENDS that firmware, so drop it explicitly to avoid
# a redundant on-disk copy.
BAD_RECOMMENDATIONS += "linux-firmware-rpidistro-bcm43455"
IMAGE_INSTALL += " \
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
    systemd-analyze \
"

# Role / app selection. COGIP_APP=0 builds a bare kiosk (Cog + networking
# only) -- handy for isolating Wi-Fi / display bring-up. The app stack
# itself is added by each variant (.bb).
COGIP_APP ??= "1"

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
# systemd-networkd-wait-online is masked too: every cogip tool pulls
# network-online.target, which blocks on wait-online until an interface is
# routable (~4-7s). Nothing here needs external network at boot (tools talk
# over localhost + CAN), so masking it makes network-online.target passive
# (reached immediately), trimming the critical path. Side effect:
# network-online becomes instant system-wide; revisit if a future service
# genuinely needs the external network up before it starts.
# avahi (mDNS/.local), ofono (3G/modem) and rpcbind (NFS portmapper) start at
# boot but the kiosk needs none of them (addressed by IP, no modem, no NFS).
# They cost ~1.5s of boot and pull the network. Mask the services AND their
# socket units (socket-activated, else the socket re-pulls the service).
systemd_handle_machine_id:append() {
    install -d ${IMAGE_ROOTFS}${sysconfdir}/systemd/system
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/getty@tty1.service
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/serial-getty@ttyS0.service
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/systemd-networkd-wait-online.service
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/avahi-daemon.service
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/avahi-daemon.socket
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/ofono.service
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/rpcbind.service
    ln -sf /dev/null ${IMAGE_ROOTFS}${sysconfdir}/systemd/system/rpcbind.socket
}

# Strip development tooling: image is reflashed, not patched on-target.
IMAGE_LINGUAS = "en-us"
