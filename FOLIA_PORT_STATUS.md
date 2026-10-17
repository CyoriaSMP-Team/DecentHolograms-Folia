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
- The `folia-supported` declaration was removed. The remaining cross-region
  paths below need work before the plugin can safely advertise Folia support.
- Focused tests pin `S.forPlayer` and the repeating global scheduler facade to
  their platform scheduler routes.

## Remaining ownership blockers

This is a scoped safety patch, not a complete cross-region guarantee. The legacy
hologram API stores shared mutable viewer/page state and many API methods can be
called from arbitrary plugin threads. A full fix needs ownership-aware state
updates (or immutable snapshots) across `Hologram`, `HologramLine`, and page
mutations, plus per-player packet operations throughout those public APIs.

The packet listener is inserted into Netty's pipeline and calls `onClick`
synchronously so it can cancel the packet. `onClick` reads player location and
shared hologram state, so this path needs a Folia-safe interception design that
preserves packet cancellation. Standalone `HologramLine` all-player operations
and public API methods also need an explicit scheduler contract. Click callbacks
invoke arbitrary registered listeners/actions, so their thread contract needs
documentation and player-dependent portions need careful scheduling.

Reload currently resolves Bukkit worlds and loads configuration on the global
region. A large hologram set could stall that region; move pure file parsing off
the global scheduler before removing that limit.

The local Gradle test/build attempt did not finish: dependency metadata resolution
stalled after the build logic configured. Run the focused test and project checks
on the remote runner. Keep Folia disabled in plugin metadata until the blockers
above have been addressed and exercised on Folia.
