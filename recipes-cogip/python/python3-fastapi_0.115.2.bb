SUMMARY = "FastAPI web framework (prebuilt wheel, cogip-pinned version)"
HOMEPAGE = "https://fastapi.tiangolo.com"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://fastapi-0.115.2.dist-info/licenses/LICENSE;md5=95792ff3fe8e11aa49ceb247e66e4810"

# Pinned to cogip-tools' version: the OE recipe ships a much newer fastapi
# (0.135) pulling starlette 1.0, which dropped the old TemplateResponse(name,
# context) signature the dashboard uses -> 500 at render. Keep cogip's tested
# version.
WHEEL = "fastapi-${PV}-py3-none-any.whl"
SRC_URI = "https://files.pythonhosted.org/packages/c9/14/bbe7776356ef01f830f8085ca3ac2aea59c73727b6ffaa757abeb7d2900b/${WHEEL}"
SRC_URI[sha256sum] = "61704c71286579cc5a598763905928f24ee98bfcc07aabe84cfefb98812bbc86"

inherit python_wheel

RDEPENDS:${PN} += "python3-starlette python3-pydantic python3-typing-extensions"
