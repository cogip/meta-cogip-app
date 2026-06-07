SUMMARY = "Cogip application services (containerized, systemd-managed)"
DESCRIPTION = "Ships the production docker compose (one service per tool) \
plus a templated cogip@.service that drives each compose service in the \
foreground, so systemd owns restart/retry/ordering. The set of enabled \
tools is selected by the ROBOT_ID build variable (0 = beacon, 1-5 = robot)."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://compose.yml \
    file://environment \
    file://cogip@.service \
    file://cogip.target \
    file://dropins/bind-server.conf \
    file://dropins/bind-server-beacon.conf \
    file://dropins/shm-cleanup.conf \
    file://cogip-tmpfiles.conf \
"

S = "${WORKDIR}"

# Note: the relationship to cogip-app-load is a systemd runtime ordering
# (Requires= in cogip@.service), NOT a packaging dependency. The image
# recipe installs cogip-app-image / cogip-app-load only when the tarball
# is staged; pulling them via RDEPENDS here would make the build fail
# whenever the (conditional) cogip-app-image package is empty/absent.
RDEPENDS:${PN} = "docker-moby docker-compose"

inherit systemd allarch

# Role selection: 0 = beacon, 1-5 = robot. Comes from the build env
# (kas env block), like KIOSK_URL / WLAN_*.
ROBOT_ID ??= "0"

# Enable the right tool instances per role (+ the umbrella target).
SYSTEMD_SERVICE:${PN} = " \
    cogip.target \
    cogip@dashboard.service \
    ${@'cogip@server-beacon.service cogip@beaconcam.service' if d.getVar('ROBOT_ID') == '0' else 'cogip@server.service cogip@planner.service cogip@copilot.service cogip@detector.service cogip@mcu-logger.service cogip@robotcam.service'} \
"
SYSTEMD_AUTO_ENABLE = "enable"

# The instance set (and the dashboard/shm drop-ins) depend on ROBOT_ID.
do_install[vardeps] += "ROBOT_ID"

do_install() {
    # Config + compose
    install -d ${D}${sysconfdir}/cogip
    install -m 0644 ${WORKDIR}/compose.yml ${D}${sysconfdir}/cogip/compose.yml

    if [ "${ROBOT_ID}" = "0" ]; then
        role="beacon"
        server="server-beacon"
    else
        role="robot"
        server="server"
    fi
    sed -e "s/@ROBOT_ID@/${ROBOT_ID}/g" \
        -e "s/@COMPOSE_PROFILES@/${role}/g" \
        ${WORKDIR}/environment > ${D}${sysconfdir}/cogip/environment
    chmod 0644 ${D}${sysconfdir}/cogip/environment

    # Units
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/cogip@.service ${D}${systemd_system_unitdir}/cogip@.service
    install -m 0644 ${WORKDIR}/cogip.target  ${D}${systemd_system_unitdir}/cogip.target

    # Clear stale shared memory before the active server starts.
    install -d ${D}${systemd_system_unitdir}/cogip@${server}.service.d
    install -m 0644 ${WORKDIR}/dropins/shm-cleanup.conf \
        ${D}${systemd_system_unitdir}/cogip@${server}.service.d/shm-cleanup.conf

    # Dashboard follows the active server.
    install -d ${D}${systemd_system_unitdir}/cogip@dashboard.service.d
    if [ "${ROBOT_ID}" = "0" ]; then
        install -m 0644 ${WORKDIR}/dropins/bind-server-beacon.conf \
            ${D}${systemd_system_unitdir}/cogip@dashboard.service.d/bind.conf
        # beaconcam follows server-beacon
        install -d ${D}${systemd_system_unitdir}/cogip@beaconcam.service.d
        install -m 0644 ${WORKDIR}/dropins/bind-server-beacon.conf \
            ${D}${systemd_system_unitdir}/cogip@beaconcam.service.d/bind.conf
    else
        install -m 0644 ${WORKDIR}/dropins/bind-server.conf \
            ${D}${systemd_system_unitdir}/cogip@dashboard.service.d/bind.conf
        # robot tools follow server
        for t in planner copilot detector mcu-logger robotcam; do
            install -d ${D}${systemd_system_unitdir}/cogip@$t.service.d
            install -m 0644 ${WORKDIR}/dropins/bind-server.conf \
                ${D}${systemd_system_unitdir}/cogip@$t.service.d/bind.conf
        done
    fi

    # Shared socket dir (/run is tmpfs): created at boot by tmpfiles.d.
    install -d ${D}${sysconfdir}/tmpfiles.d
    install -m 0644 ${WORKDIR}/cogip-tmpfiles.conf \
        ${D}${sysconfdir}/tmpfiles.d/cogip.conf
}

FILES:${PN} = " \
    ${sysconfdir}/cogip \
    ${sysconfdir}/tmpfiles.d/cogip.conf \
    ${systemd_system_unitdir} \
"
