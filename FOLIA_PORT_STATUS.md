# Folia 1.21.11 port status

Target branch: `folia/1.21.11`. The NMS module `nms-v1_21_R7` targets
`1.21.11-R0.1-SNAPSHOT`. The common Bukkit implementation and version dispatch
remain in place for Paper and older supported versions.

## Changes in this pass

- Show/hide-all and quit/teleport/respawn handling no longer send player packets
  from async tasks. Quit cleanup runs in the quit event's player context.
- The shared ticker and world load/unload/reload paths now enter the global
  scheduler before enumerating players or resolving Bukkit worlds. Hologram
  viewer updates and hides are handed to each viewer's entity scheduler.
- Paper 1.21.11 interaction decoding now uses the packet's secondary-action
  flag and a cached player UUID, without reading player state on Netty. Only
  rendered hologram entity IDs are consumed; click events and actions run on
  the player's entity scheduler. Bulk clickable-entity updates use each
  viewer's scheduler, and the player service is seeded before existing online
  players receive packet listeners.
- The `folia-supported` declaration was removed. The remaining cross-region
  paths below need work before the plugin can safely advertise Folia support.
- Focused tests cover UUID-based player scheduling and deferring recognized
  hologram clicks to the player scheduler.

## Remaining ownership blockers

This is still not a complete cross-region guarantee. The legacy hologram API
stores shared mutable viewer/page state and many API methods can be called from
arbitrary plugin threads. Standalone `HologramLine` all-player operations and
public page mutations still need an explicit scheduler contract. Click events
and actions now run on the player's region; event cancellation suppresses the
action while the matched fake-entity packet remains consumed. External click
listeners therefore run on a Folia player thread and must follow that contract.

Reload currently resolves Bukkit worlds and loads configuration on the global
region. A large hologram set could stall that region; move pure file parsing off
the global scheduler before removing that limit.

## Verification (2026-10-18)

- [CI run 39091434090](https://github.com/CyoriaSMP-Team/DecentHolograms-Folia/actions/runs/39091434090)
  and [Folia development run 39091433902](https://github.com/CyoriaSMP-Team/DecentHolograms-Folia/actions/runs/39091433902)
  passed for PR head `f138baa3c7353a0371f8ab9a5bd1b310acc9b3bf`. The development
  artifact was built from merge commit `36643d142ab8c3541c3ce8e6fb2b7fb3e32002e4`.
  The unmodified plugin JAR SHA-256 was
  `4302e28e44aa80866c088a132e8437fdfc86aa47ea8e4589abe901800b059fa0`.
- The JAR loaded on isolated Folia 1.21.11-14 with Java 25 and four configured
  region tick threads. Two offline clients 427 blocks apart clicked the Region A
  and Region B holograms and received the matching messages. A second scratch-only
  fixture pass logged both player-command actions on a Folia region scheduler
  thread, with no plugin or scheduler exceptions. Both regions were assigned to
  worker `#1`, so this did not demonstrate execution on separate workers.
- Folia metadata was added only to the scratch copy
  (`96c93990d4a109442982b8ac978986a222be41765316228d13d3937d3040aaa9`) to allow
  that runtime smoke. The repository plugin metadata and unmodified CI artifact
  still have no `folia-supported` declaration.

The click-path smoke and CI pass, but the public hologram API still permits
arbitrary-thread calls into shared page, line, and viewer state; reload also parses
and resolves worlds on the global scheduler. Keep Folia disabled in plugin
metadata and do not publish a Folia-compatible release until those remaining
ownership paths have a scheduler contract and are exercised on Folia.
