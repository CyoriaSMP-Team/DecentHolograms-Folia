# Folia 1.21.11 port status

Target branch: `folia/1.21.11`. The NMS module `nms-v1_21_R7` targets
`1.21.11-R0.1-SNAPSHOT`. The common Bukkit implementation and version dispatch
remain in place for Paper and older supported versions.

## Changes in this pass

- Visibility refreshes initiated by the asynchronous ticker now schedule player
  work through the platform's entity scheduler abstraction.
- Show/hide-all and quit/teleport/respawn handling no longer send player packets
  from async tasks. Quit cleanup runs in the quit event's player context.
- The `folia-supported` declaration was removed. The remaining cross-region
  paths below need work before the plugin can safely advertise Folia support.
- Added a focused test pinning `S.forPlayer` to `PlatformScheduler.runForPlayer`.

## Remaining ownership blockers

This is a scoped safety patch, not a complete cross-region guarantee. The legacy
hologram API stores shared mutable viewer/page state and many API methods can be
called from arbitrary plugin threads. A full fix needs ownership-aware state
updates (or immutable snapshots) across `Hologram`, `HologramLine`, and page
mutations, plus per-player packet operations throughout those public APIs.

World load/unload handlers and hologram load/save paths still perform Bukkit
world/location and shared hologram operations from async work. Several commands
and action handlers dispatch server-wide commands through the global scheduler;
console command safety depends on each command's own world/entity behavior.
Click callbacks also invoke arbitrary registered listeners/actions, so their
thread contract cannot be made safe without a documented API contract and
careful scheduling of player-dependent portions.

No local Gradle build or tests were run, per request; CI should run the focused
test and project checks on the remote runner. Keep Folia disabled in plugin
metadata until the blockers above have been addressed and exercised on Folia.
