# Fork launcher (zygote) experiment — PARKED

Experiment (2026-06-25) to cut the Cogip kiosk boot time by importing the heavy
Python base ONCE in a parent process and `os.fork()`-ing one child per tool, so
numpy / cogip.cpp / socketio / pydantic / fastapi are loaded once instead of
once per process. Validated live on the Pi4 (2GB), then **parked** in favour of
the plain multi-service systemd model. This branch preserves the artifacts.

## Result
- Phase 0 (launcher = planner+detector+copilot; server/dashboard stay services):
  boot 34s -> **29s (~5s, ~15%)**. The win is de-contending the import (1 cold
  import instead of 3 fighting for the SD + 4 cores).
- Phase 5 (server+dashboard folded into the launcher too): **30s = neutral**.
  Folding them just moved them behind the parent's serial base import; the
  visible page (dashboard) is no earlier.

## Why it was parked
The ~5s gain did not justify the operational cost:
- No native `systemctl restart cogip@<tool>` (tools are forked children, not
  units). A control command (`cogip-launcher-ctl restart <tool>`) must be built.
- Bigger blast radius (a parent crash takes everything down).
- Reimplements supervision (re-fork on death, sd_notify) that systemd gives free.

## Bottleneck (measured) — why nothing else helps
Cold base import 8.56s, warm 6.39s -> only **~2.17s is disk I/O**, **~6.4s is
CPU** (executing numpy / cogip.cpp nanobind / pydantic / fastapi module init).
Consequences, all explored and REJECTED:
- Faster storage (USB3 key/SSD): SD is 41 MB/s seq, ~1 MB/s random 4K; but the
  cold I/O penalty is only ~2.17s, and a flash key's poor random read wouldn't
  even recover that. Ceiling ~2s, and needs SCSI/USB_STORAGE re-enabled (trim
  cut them). Not worth it.
- RAMFS / vmtouch: site-packages is 683MB (258MB of .so) on a 1.8GB box ->
  OOM/pressure; and you must populate from SD at boot = the I/O moved, not
  removed (= the page-cache-prewarm already tested and reverted). Ceiling ~2s,
  CPU untouched.
- Replacing init (launcher as PID 1): userspace-before-tools is only ~3s,
  dominated by SD enumeration (1.4s, hardware) + mandatory device/fs init (udev,
  fsck, tmpfiles). Replacing systemd reimplements udev/wpa_supplicant/can0/
  ldconfig/journald/sshd, loses robustness, and saves ~0 (and never touches the
  6.4s CPU import).
The ONLY lever on the dominant 6.4s CPU import is doing it once = this launcher.

## Dev workflow IS preserved (key finding)
The parent imports only the COMPILED base (numpy, cogip.cpp.*, third-party) — it
holds NO editable tool Python (`cogip.tools.*` absent from the parent's
sys.modules; verified). Each child does its `import cogip.tools.<tool>` AFTER the
fork -> tool code is loaded FRESH from disk per (re-)fork. So a per-tool re-fork
(via the control command) restarts only that tool AND picks up its edited .py.
A whole-launcher restart is only needed when editing the compiled base (rebuilt
anyway).

## Fork-safety gotchas (learned the hard way)
- `planner.__main__` spawns 4 threads AT IMPORT -> the parent must import the
  base ONLY; tool modules are imported in the child (post-fork). Keeps the
  parent single-threaded (Python 3.14 warns on fork from a multi-threaded proc).
- planner uses `multiprocessing` (Manager + avoidance Process). Default start
  method on 3.14 (spawn/forkserver) RE-EXECUTES the launcher module -> set
  `multiprocessing.set_start_method("fork")` AND guard the launcher with
  `if __name__ == "__main__":` so the re-import does not re-run the fork loop.
- detector's socketio connect runs in a NON-DAEMON thread; `main()` returns
  immediately -> a bare `os._exit()` kills it before it connects. The child must
  join non-daemon threads before the hard exit (mimic normal interpreter
  shutdown). Without this the detector "exits status=0" and never connects.

## Artifacts
- `cogip-launcher.py` — the launcher (Phase 5 layout: server first, then
  dashboard, detector, planner, copilot). Deploy to `/opt/`.
- `cogip-launcher.service` — systemd unit (Type=simple). Deploy to
  `/etc/systemd/system/`.
- `mcu-logger-launcher.conf` — drop-in re-pointing cogip@mcu-logger from
  cogip@server to cogip-launcher. Deploy to
  `/etc/systemd/system/cogip@mcu-logger.service.d/launcher.conf`.

## Deploy (to revive)
```
# disable the services the launcher owns
systemctl disable cogip@server cogip@dashboard cogip@planner cogip@detector cogip@copilot
systemctl enable cogip-launcher
systemctl daemon-reload
```
For Phase-0 layout instead (recommended: smaller blast radius, native
systemctl on server/dashboard), keep server+dashboard as services and set
`TOOLS = ["detector", "planner", "copilot"]` in the launcher.

## If revived — remaining work
Phase 1 supervision (re-fork on child death) · Phase 2 control command
(`cogip-launcher-ctl restart <tool>` over a Unix socket) · Phase 3 sd_notify ·
Phase 4 a proper recipe in meta-cogip-app + drop the folded `cogip@` units.
