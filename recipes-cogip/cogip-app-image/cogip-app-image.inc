# Pinned identity of the pre-built Cogip container image.
#
# Tracked in git for provenance: it records exactly which image is
# embedded in the kiosk. Updated automatically by `make app-image`
# (which rebuilds the tarball into DL_DIR and rewrites this checksum).
# The .tar.zst itself is never committed -- it lives in DL_DIR
# (~/.yocto/downloads) locally, and on a meta-cogip-app release later.
COGIP_APP_IMAGE_SHA256 = "c58a64778c7e7bbc3538718680cedc0360061f815c33c8d284d6a911ce5eab24"
