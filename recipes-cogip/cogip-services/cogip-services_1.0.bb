SUMMARY = "Cogip application services (native, systemd-managed)"
DESCRIPTION = "Ships a templated cogip@.service that runs each tool natively \
with `uv run` against the /opt/.venv venv (the cogip-%i console script), so \
systemd owns restart/retry/ordering. No Docker. The set of enabled tools is \
selected by the ROBOT_ID build variable (0 = beacon, 1-5 = robot)."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://cogip@.service \
    file://cogip.target \
    file://dropins/bind-server.conf \
    file://dropins/bind-server-beacon.conf \
    file://dropins/shm-cleanup.conf \
    file://cogip-tmpfiles.conf \
"

S = "${UNPACKDIR}"

# Native runtime: the tools (cogip-tools + closure + system bindings) and the
# per-role environment. The units read /etc/cogip/environment (per-role
# config, owned by cogip-environment).
RDEPENDS:${PN} = "cogip-tools cogip-environment"

inherit systemd allarch

# Role selection: 0 = beacon, 1-5 = robot. Comes from the build env
# (kas env block), like KIOSK_URL / WLAN_*.
ROBOT_ID ??= "0"

# Enable the right tool instances per role (+ the umbrella target).
# Robot-role tools. robotcam is only enabled on camera-equipped robots; the
# ninja (ROBOT_ID=2) has no camera, so leaving it on just wastes ~5s of boot
# and CPU (it crash-loops on the missing /dev/video0, competing with the other
# tools' Python cold start).
ROBOT_SERVICES = "cogip@server.service cogip@planner.service cogip@copilot.service cogip@detector.service cogip@mcu-logger.service"
ROBOT_SERVICES:append = "${@'' if d.getVar('ROBOT_ID') == '2' else ' cogip@robotcam.service'}"

SYSTEMD_SERVICE:${PN} = " \
    cogip.target \
    cogip@dashboard.service \
    ${@'cogip@server-beacon.service cogip@beaconcam.service' if d.getVar('ROBOT_ID') == '0' else d.getVar('ROBOT_SERVICES')} \
"
SYSTEMD_AUTO_ENABLE = "enable"

# The instance set (and the dashboard/shm drop-ins) depend on ROBOT_ID.
do_install[vardeps] += "ROBOT_ID"

do_install() {
    # The active server's instance name depends on the role; the role env
    # itself (/etc/cogip/environment) is produced by the cogip-environment recipe.
    if [ "${ROBOT_ID}" = "0" ]; then
        server="server-beacon"
    else
        server="server"
    fi

    # Units
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/cogip@.service ${D}${systemd_system_unitdir}/cogip@.service
    install -m 0644 ${UNPACKDIR}/cogip.target  ${D}${systemd_system_unitdir}/cogip.target

    # Clear stale shared memory before the active server starts.
    install -d ${D}${systemd_system_unitdir}/cogip@${server}.service.d
    install -m 0644 ${UNPACKDIR}/dropins/shm-cleanup.conf \
        ${D}${systemd_system_unitdir}/cogip@${server}.service.d/shm-cleanup.conf

    # Dashboard (and the other tools) follow the active server lifecycle.
    install -d ${D}${systemd_system_unitdir}/cogip@dashboard.service.d
    if [ "${ROBOT_ID}" = "0" ]; then
        install -m 0644 ${UNPACKDIR}/dropins/bind-server-beacon.conf \
            ${D}${systemd_system_unitdir}/cogip@dashboard.service.d/bind.conf
        install -d ${D}${systemd_system_unitdir}/cogip@beaconcam.service.d
        install -m 0644 ${UNPACKDIR}/dropins/bind-server-beacon.conf \
            ${D}${systemd_system_unitdir}/cogip@beaconcam.service.d/bind.conf
    else
        install -m 0644 ${UNPACKDIR}/dropins/bind-server.conf \
            ${D}${systemd_system_unitdir}/cogip@dashboard.service.d/bind.conf
        for t in planner copilot detector mcu-logger robotcam; do
            install -d ${D}${systemd_system_unitdir}/cogip@$t.service.d
            install -m 0644 ${UNPACKDIR}/dropins/bind-server.conf \
                ${D}${systemd_system_unitdir}/cogip@$t.service.d/bind.conf
        done
    fi

    # Shared socket dir (/run is tmpfs): created at boot by tmpfiles.d.
    install -d ${D}${sysconfdir}/tmpfiles.d
    install -m 0644 ${UNPACKDIR}/cogip-tmpfiles.conf \
        ${D}${sysconfdir}/tmpfiles.d/cogip.conf
}

FILES:${PN} = " \
    ${sysconfdir}/tmpfiles.d/cogip.conf \
    ${systemd_system_unitdir} \
"
