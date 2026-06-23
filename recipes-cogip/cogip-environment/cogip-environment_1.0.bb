SUMMARY = "Cogip per-role /etc/cogip/environment"
DESCRIPTION = "Generates /etc/cogip/environment with the full per-role runtime \
configuration (robot dimensions, detector/planner tuning, GPIO pins), \
resolved from the ROBOT_ID build variable exactly like raspios does \
(0 = beacon, 1 = robot, 2 = ninja, 3-9 = pami). The specs come from \
cogip-tools' raspios/config-common.env -- the single source of truth, \
staged into DL_DIR by the Makefile -- applied to its environment \
template, so the values are never duplicated in this layer. The file is \
consumed by the cogip-* containers (compose env_file) and by docker \
compose itself (COMPOSE_PROFILES), via cogip-services."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# config-common.env + the environment template are staged into DL_DIR by
# `make` (copied from COGIP_TOOLS_PATH/raspios), referenced as local
# file:// (DL_DIR added to the search path) -- no sha to pin, bitbake
# tracks their CONTENT so the env is regenerated when cogip-tools' config
# changes. Only the Docker app stack consumes this, so gate on COGIP_APP:
# a bare-kiosk (COGIP_APP=0) build then parses without the staged files.
FILESEXTRAPATHS:prepend := "${DL_DIR}:"
COGIP_APP ??= "1"
SRC_URI = "${@'file://cogip-config-common.env file://cogip-environment.template' if d.getVar('COGIP_APP') in ('1', 'yes', 'true') else ''}"

S = "${UNPACKDIR}"

inherit allarch

# Role -> hardware profile. Comes from the build env (kas env block), like
# KIOSK_URL / WLAN_*. Regenerate /etc/environment when it changes.
ROBOT_ID ??= "0"
do_install[vardeps] += "ROBOT_ID"

do_install() {
    [ -f "${UNPACKDIR}/cogip-environment.template" ] || return 0

    rid="${ROBOT_ID}"

    # Role specs: single source of truth from cogip-tools.
    . "${UNPACKDIR}/cogip-config-common.env"

    # Type resolution, mirroring raspios stage2_create_image.sh: which set
    # of CUSTOM_* values fills the template. Beacon (0) leaves them empty
    # -- the planner/detector tools don't run on it -- as raspios does.
    case "$rid" in
        0)  profile="beacon" ;;
        1)  profile="robot"
            rwidth="$ROBOT_WIDTH";                  rlength="$ROBOT_LENGTH"
            dport="$ROBOT_DETECTOR_LIDAR_PORT"
            dmin="$ROBOT_DETECTOR_MIN_DISTANCE";    dmax="$ROBOT_DETECTOR_MAX_DISTANCE"
            dint="$ROBOT_DETECTOR_MIN_INTENSITY";   drefresh="$ROBOT_DETECTOR_REFRESH_INTERVAL"
            ddelay="$ROBOT_DETECTOR_SENSOR_DELAY"
            dsamples="$ROBOT_DETECTOR_CLUSTER_MIN_SAMPLES"; deps="$ROBOT_DETECTOR_CLUSTER_EPS"
            bbmargin="$ROBOT_PLANNER_OBSTACLE_BB_MARGIN" ;;
        2)  profile="robot"
            rwidth="$NINJA_WIDTH";                  rlength="$NINJA_LENGTH"
            dport="$NINJA_DETECTOR_LIDAR_PORT"
            dmin="$NINJA_DETECTOR_MIN_DISTANCE";    dmax="$NINJA_DETECTOR_MAX_DISTANCE"
            dint="$NINJA_DETECTOR_MIN_INTENSITY";   drefresh="$NINJA_DETECTOR_REFRESH_INTERVAL"
            ddelay="$NINJA_DETECTOR_SENSOR_DELAY"
            dsamples="$NINJA_DETECTOR_CLUSTER_MIN_SAMPLES"; deps="$NINJA_DETECTOR_CLUSTER_EPS"
            bbmargin="$NINJA_PLANNER_OBSTACLE_BB_MARGIN" ;;
        [3-9])  profile="robot"
            rwidth="$PAMI_WIDTH";                   rlength="$PAMI_LENGTH"
            dport="$PAMI_DETECTOR_LIDAR_PORT"
            dmin="$PAMI_DETECTOR_MIN_DISTANCE";     dmax="$PAMI_DETECTOR_MAX_DISTANCE"
            dint="$PAMI_DETECTOR_MIN_INTENSITY";    drefresh="$PAMI_DETECTOR_REFRESH_INTERVAL"
            ddelay="$PAMI_DETECTOR_SENSOR_DELAY"
            dsamples="$PAMI_DETECTOR_CLUSTER_MIN_SAMPLES"; deps="$PAMI_DETECTOR_CLUSTER_EPS"
            bbmargin="$PAMI_PLANNER_OBSTACLE_BB_MARGIN" ;;
        *)  bbfatal "cogip-environment: ROBOT_ID '$rid' invalid (expected 0-9)" ;;
    esac

    install -d ${D}${sysconfdir}/cogip

    # Render the template: fill the CUSTOM_* placeholders and the
    # socket-io port suffix (localhost:809<id>).
    sed -e "s/CUSTOM_ROBOT_WIDTH/$rwidth/" \
        -e "s/CUSTOM_ROBOT_LENGTH/$rlength/" \
        -e "s|CUSTOM_DETECTOR_LIDAR_PORT|$dport|" \
        -e "s/CUSTOM_DETECTOR_MIN_DISTANCE/$dmin/" \
        -e "s/CUSTOM_DETECTOR_MAX_DISTANCE/$dmax/" \
        -e "s/CUSTOM_DETECTOR_MIN_INTENSITY/$dint/" \
        -e "s/CUSTOM_DETECTOR_REFRESH_INTERVAL/$drefresh/" \
        -e "s/CUSTOM_DETECTOR_SENSOR_DELAY/$ddelay/" \
        -e "s/CUSTOM_DETECTOR_CLUSTER_MIN_SAMPLES/$dsamples/" \
        -e "s/CUSTOM_DETECTOR_CLUSTER_EPS/$deps/" \
        -e "s/CUSTOM_PLANNER_OBSTACLE_BB_MARGIN/$bbmargin/" \
        -e "s/809ROBOT_ID/809$rid/" \
        "${UNPACKDIR}/cogip-environment.template" > ${D}${sysconfdir}/cogip/environment

    # Uncomment + fill the type-specific GPIO pins (raspios stage2).
    case "$rid" in
        1)  sed -i \
              -e "s|# PLANNER_STARTER_PIN=|PLANNER_STARTER_PIN=$ROBOT_STARTER_PIN|" \
              -e "s|# PLANNER_SCSERVOS_PORT=|PLANNER_SCSERVOS_PORT=$ROBOT_SCSERVOS_PORT|" \
              -e "s|# PLANNER_SCSERVOS_BAUD_RATE=|PLANNER_SCSERVOS_BAUD_RATE=$ROBOT_SCSERVOS_BAUD_RATE|" \
              ${D}${sysconfdir}/cogip/environment ;;
        2)  sed -i \
              -e "s/# PLANNER_LED_RED_PIN=/PLANNER_LED_RED_PIN=$NINJA_LED_RED_PIN/" \
              -e "s/# PLANNER_LED_GREEN_PIN=/PLANNER_LED_GREEN_PIN=$NINJA_LED_GREEN_PIN/" \
              -e "s/# PLANNER_LED_BLUE_PIN=/PLANNER_LED_BLUE_PIN=$NINJA_LED_BLUE_PIN/" \
              -e "s/# PLANNER_FLAG_MOTOR_PIN=/PLANNER_FLAG_MOTOR_PIN=$NINJA_FLAG_MOTOR_PIN/" \
              -e "s/# PLANNER_OLED_BUS=/PLANNER_OLED_BUS=$NINJA_OLED_BUS/" \
              -e "s/# PLANNER_OLED_ADDRESS=/PLANNER_OLED_ADDRESS=$NINJA_OLED_ADDRESS/" \
              -e "s|# PLANNER_SCSERVOS_PORT=|PLANNER_SCSERVOS_PORT=$ROBOT_SCSERVOS_PORT|" \
              -e "s|# PLANNER_SCSERVOS_BAUD_RATE=|PLANNER_SCSERVOS_BAUD_RATE=$ROBOT_SCSERVOS_BAUD_RATE|" \
              -e "s|# *PLANNER_STRATEGY=.*|PLANNER_STRATEGY=Ninja|" \
              -e "s|# *PLANNER_START_POSITION=.*|PLANNER_START_POSITION=NINJA|" \
              ${D}${sysconfdir}/cogip/environment ;;
        [3-9])  sed -i \
              -e "s/# PLANNER_LED_RED_PIN=/PLANNER_LED_RED_PIN=$PAMI_LED_RED_PIN/" \
              -e "s/# PLANNER_LED_GREEN_PIN=/PLANNER_LED_GREEN_PIN=$PAMI_LED_GREEN_PIN/" \
              -e "s/# PLANNER_LED_BLUE_PIN=/PLANNER_LED_BLUE_PIN=$PAMI_LED_BLUE_PIN/" \
              -e "s/# PLANNER_FLAG_MOTOR_PIN=/PLANNER_FLAG_MOTOR_PIN=$PAMI_FLAG_MOTOR_PIN/" \
              -e "s/# PLANNER_OLED_BUS=/PLANNER_OLED_BUS=$PAMI_OLED_BUS/" \
              -e "s/# PLANNER_OLED_ADDRESS=/PLANNER_OLED_ADDRESS=$PAMI_OLED_ADDRESS/" \
              ${D}${sysconfdir}/cogip/environment ;;
    esac

    # Orchestration vars: COMPOSE_PROFILES selects the compose profile and
    # ROBOT_ID is read by the tools. Previously emitted by cogip-services;
    # this recipe now owns the whole file.
    printf 'ROBOT_ID=%s\nCOMPOSE_PROFILES=%s\n' "$rid" "$profile" \
        >> ${D}${sysconfdir}/cogip/environment
}

FILES:${PN} = "${sysconfdir}/cogip/environment"
