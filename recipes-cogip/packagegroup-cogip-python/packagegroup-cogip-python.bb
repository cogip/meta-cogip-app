SUMMARY = "All Python runtime dependencies of the cogip-tools (native venv-free)"
DESCRIPTION = "Pulls the full cogip-tools Python runtime closure onto the \
target's Python 3.14 site-packages: deps cross-compiled by OE, the prebuilt \
wheels (python_wheel recipes) and lgpio. Combined with packagegroup-cogip- \
native-deps (libcamera/pykms/systemd/prctl/libserial) this lets the cogip \
tools run natively, no Docker. The cogip-tools sources themselves are added \
separately (editable on the rootfs)."
LICENSE = "MIT"

inherit packagegroup

RDEPENDS:${PN} = " \
    python3 \
    python3-aiohappyeyeballs \
    python3-aiohttp \
    python3-aioserial \
    python3-aiosignal \
    python3-annotated-types \
    python3-anyio \
    python3-asttokens \
    python3-attrs \
    python3-av \
    python3-bidict \
    python3-can \
    python3-cbor2 \
    python3-certifi \
    python3-charset-normalizer \
    python3-click \
    python3-colorzero \
    python3-contourpy \
    python3-cycler \
    python3-cython \
    python3-dateutil \
    python3-devtools \
    python3-dotenv \
    python3-engineio \
    python3-executing \
    python3-fastapi \
    python3-fonttools \
    python3-frozenlist \
    python3-gpiozero \
    python3-h11 \
    python3-httpcore \
    python3-httptools \
    python3-httpx \
    python3-idna \
    python3-jinja2 \
    python3-joblib \
    python3-jsonschema \
    python3-jsonschema-specifications \
    python3-kiwisolver \
    python3-lgpio \
    python3-libarchive-c \
    python3-linuxpy \
    python3-luma-core \
    python3-luma-oled \
    python3-markdown-it-py \
    python3-markupsafe \
    python3-matplotlib \
    python3-mdurl \
    python3-more-itertools \
    python3-multidict \
    python3-nanobind \
    python3-narwhals \
    python3-numpy \
    python3-opencv \
    python3-packaging \
    python3-picamera2 \
    python3-piexif \
    python3-pillow \
    python3-polling2 \
    python3-prctl \
    python3-propcache \
    python3-protobuf \
    python3-pydantic \
    python3-pydantic-core \
    python3-pydantic-settings \
    python3-pygments \
    python3-pyparsing \
    python3-pyserial \
    python3-pyyaml \
    python3-referencing \
    python3-requests \
    python3-rich \
    python3-rpds-py \
    python3-scikit-learn \
    python3-scipy \
    python3-sentry-sdk \
    python3-shellingham \
    python3-simple-websocket \
    python3-simplejpeg \
    python3-six \
    python3-smbus2 \
    python3-sniffio \
    python3-socketio \
    python3-starlette \
    python3-systemd \
    python3-threadpoolctl \
    python3-toposort \
    python3-tqdm \
    python3-typer \
    python3-typing-extensions \
    python3-urllib3 \
    python3-uvicorn \
    python3-uvloop \
    python3-watchfiles \
    python3-websocket-client \
    python3-websockets \
    python3-wrapt \
    python3-wsproto \
    python3-yarl \
"
