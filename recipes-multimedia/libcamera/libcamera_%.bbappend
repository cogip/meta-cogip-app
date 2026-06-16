# Native deployment: build libcamera's Python bindings (the
# libcamera-pycamera package) so picamera2 can drive the camera without the
# Docker base image. picamera2 itself is a pure-python wheel (pip), but its
# libcamera/pykms bindings are not on PyPI and must come from the system.
# Stacks on meta-raspberrypi's libcamera bbappend (RPi pipeline support).
PACKAGECONFIG:append = " pycamera"
