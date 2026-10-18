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

Local `:plugin:compileJava`, `:plugin:compileTestJava`, and
`:nms:nms-paper_v1_21_R7:compileJava` pass with JDK 21. The full `:plugin:test`
task cannot finish offline because older NMS modules need uncached Spigot
snapshots (`1.20.6` and `1.21.1`). Run full CI and the Folia runtime smoke before
considering a release. Keep Folia disabled in plugin metadata until the blockers
above have been addressed and exercised on Folia.
