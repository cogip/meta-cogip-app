# Enable Cog's DRM platform so it can render straight to /dev/dri/card0
# without needing a Wayland compositor. This is the lightest kiosk
# setup for the Pi 4: WPE WebKit -> Mesa V3D -> DRM/KMS -> HDMI.
PACKAGECONFIG:append = " drm"
