#!/usr/bin/env python3
"""Cogip launcher (Phase 5): import the heavy Python base ONCE in this parent,
then os.fork() one child per tool. Children inherit the imported modules via
copy-on-write, so numpy/cogip.cpp/socketio/pydantic/fastapi are loaded once for
all tools instead of once per process. server is forked first (it owns the
shared-memory segment + the socketio hub the others connect to)."""
import os
import sys
import time
import signal
import threading
import multiprocessing as mp

os.environ.setdefault("OPENBLAS_NUM_THREADS", "1")
os.environ.setdefault("OMP_NUM_THREADS", "1")
os.environ.setdefault("NUMEXPR_NUM_THREADS", "1")

# Fork start method so the planner's Manager/avoidance Process inherit memory
# (no re-import of this launcher) instead of spawn re-executing it.
try:
    mp.set_start_method("fork", force=True)
except RuntimeError:
    pass

# --- import-safe shared base, ONCE in the parent (NO tool modules: planner's
# module spawns threads at import -> import tool modules in the child) ---------
t0 = time.monotonic()
import numpy  # noqa: F401
import socketio  # noqa: F401
import pydantic  # noqa: F401
import can  # noqa: F401
import fastapi  # noqa: F401
import uvicorn  # noqa: F401
import watchfiles  # noqa: F401
import cogip.cpp.libraries.shared_memory  # noqa: F401
import cogip.cpp.libraries.models  # noqa: F401
import cogip.cpp.libraries.obstacles  # noqa: F401
import cogip.cpp.libraries.avoidance  # noqa: F401
T_IMPORT = time.monotonic() - t0

# server FIRST: it creates the SharedMemory(owner) + socketio hub the rest need.
TOOLS = ["server", "dashboard", "detector", "planner", "copilot"]


def child_run(name):
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    signal.signal(signal.SIGTERM, signal.SIG_DFL)
    sys.argv = [f"cogip-{name}"]
    import importlib
    mod = importlib.import_module(f"cogip.tools.{name}.__main__")
    code = 0
    try:
        mod.main()
    except SystemExit as e:
        code = e.code if isinstance(e.code, int) else (0 if e.code is None else 1)
    except BaseException as e:  # noqa: BLE001
        sys.stderr.write(f"[{name}] crashed: {e!r}\n")
        sys.stderr.flush()
        code = 1
    # main() may return while non-daemon threads keep the tool alive (detector's
    # socketio connect runs in a non-daemon thread). Mimic normal interpreter
    # shutdown: wait for non-daemon threads before the hard _exit.
    mt = threading.main_thread()
    for t in threading.enumerate():
        if t is not mt and not t.daemon:
            try:
                t.join()
            except RuntimeError:
                pass
    os._exit(code)


def main_launcher():
    n = threading.active_count()
    sys.stderr.write(f"[launcher] base imported in {T_IMPORT:.2f}s; parent threads={n}\n")
    if n != 1:
        sys.stderr.write(f"[launcher] WARNING parent not single-threaded ({n})\n")
    sys.stderr.flush()
    children = {}
    for name in TOOLS:
        sys.stderr.flush()
        sys.stdout.flush()
        pid = os.fork()
        if pid == 0:
            child_run(name)  # never returns
        children[pid] = name
        sys.stderr.write(f"[launcher] forked {name} pid={pid}\n")
        sys.stderr.flush()

    def _term(signum, frame):
        for pid in list(children):
            try:
                os.kill(pid, signal.SIGTERM)
            except ProcessLookupError:
                pass

    signal.signal(signal.SIGTERM, _term)
    signal.signal(signal.SIGINT, _term)
    while children:
        try:
            pid, status = os.waitpid(-1, 0)
        except ChildProcessError:
            break
        except InterruptedError:
            continue
        nm = children.pop(pid, "?")
        sys.stderr.write(f"[launcher] child {nm} (pid={pid}) exited status={status}\n")
        sys.stderr.flush()


if __name__ == "__main__":
    main_launcher()
