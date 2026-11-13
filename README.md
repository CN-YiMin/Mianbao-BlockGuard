# Mianbao BlockGuard / 面包军火库·地形保护

A small NeoForge 1.21.1 addon that removes **block destruction** from
[Mianbao's NewModernWarfare](https://www.curseforge.com/minecraft/mc-mods/mianbaos-modernwarfare)
(`mianbaos_modernwarfare`) while leaving the rest of the mod usable.

It is built for **`mianbaos_modernwarfare-2.7.0.jar`** (the version in the
`你好，新蒸程！V1.8` pack).

## What it stops

| Source | What happens |
| --- | --- |
| Missiles, bombs, rockets, shells, mines, APS, drone/aircraft deaths, ... | The explosion still plays, still hurts entities, but its block list is emptied (or trimmed to the whitelist) before `finalizeExplosion`, so no blocks, drops or explosion-fire are produced. |
| Procedures that call `Level.destroyBlock` directly (crater loops, flamethrower, poisoned leaves, scorched wood, ...) | The direct call returns `false` before the block is removed. |
| Nuclear shock waves (`NuclearWaveScheduler`) | The wave is never enqueued, so there is no per-tick wave work and no explosion at all. |
| Nuclear crater/scorch tasks (`NuclearTerrainScheduler`, `ManagedNuclearSequenceScheduler`) | The tasks are never enqueued. |
| Bulk scorched-terrain replacement (`Explosionblastwork2`, `Explosionscorched`) | The terrain pass is cancelled; the explosion itself is already handled above. |
| Incendiary post-processing that replaces terrain with scorched variants, water or air (`Firebomb_explode_block_work`, `Gasbombexplodeblockwork`, `Fireball_tanshe_explode`) | Any change to an existing non-Mianbao block is blocked; the burning charge/projectile can still remove itself so its block entity does not tick forever. |
| Fire placed by a weapon (incendiary bombs, flamethrower, white phosphorus, ...) | Fire block placement by the mod is cancelled. Fire from other sources is untouched. |

Entity damage is preserved by default. Set `keepEntityDamage = false` if you
want the explosion cancelled entirely (the weapon then does no damage at all).

## Install

1. Drop `mianbao_blockguard-1.0.0.jar` into the instance's `mods/` folder.
2. Keep `mianbaos_modernwarfare-2.7.0.jar` and GeckoLib installed.
3. Start the game once. The config is created at
   `config/mianbao_blockguard-common.toml`.

The addon is server-side authoritative; clients do not need it for terrain
protection, but the pack should ship the same jar on both sides so the mod list
matches.

## Config

```toml
[general]
enabled = true
keepEntityDamage = true
protectExplosions = true
protectDirectBreaks = true
protectNuclearTerrain = true
protectScorchedTerrain = true
protectFire = true

[whitelist]
# Empty = protect every block.
# IDs and tags are both accepted.
blocks = []
```

`blocks` is a **whitelist of blocks that are still allowed to be destroyed**.
Everything not listed is protected. Examples:

```toml
[whitelist]
blocks = [
  "minecraft:dirt",
  "minecraft:sand",
  "#minecraft:leaves",
]
```

An empty list means **no block may be destroyed** by Mianbao's weapons.

The file is watched by NeoForge; saving it reloads the filter. A restart is the
safest option after changing it.

## Why there is no per-tick cost

The addon never subscribes to a tick event and never scans entities, blocks or
chunks. It only listens where destruction actually happens:

* `Explosion.explode()` — once per explosion, at `RETURN`, after the block list
  is known but before `finalizeExplosion` applies it. This is **O(blocks in that
  explosion)**, not O(ticks) or O(loaded blocks). It also covers explosions the
  mod creates directly with `new Explosion(...)`, including nuclear waves.
* `Level.destroyBlock(...)` — only for direct block-removal calls. Ordinary
  block updates and player mining do not use this path.
* `Level.setBlock(...)` — one `instanceof BaseFireBlock` test per block update;
  everything that is not fire exits immediately. Fire is the only block type
  that can reach the stack walk, and `protectFire = false` removes even that
  branch's work (the mixin still runs, but exits after two cached boolean
  reads). This is the only hook on the ordinary block-update path; disable it
  if you want zero block-update overhead and are willing to let incendiary
  weapons start fires.
* The mod's nuclear queue methods — one check per nuke, at enqueue time.

Attribution is a stack walk on those discrete events: the addon looks for the
first frame that is not vanilla/NeoForge/Mixin and checks whether it is
`net.mcreator.myfirstmod`. A stack walk costs a few microseconds and runs a few
times per second at most, not once per tick or once per block.

## Compatibility / limits

* Targets `mianbaos_modernwarfare` **2.7.0**. The mod-specific mixins name a few
  procedures; if a future version renames them, the mixin will fail loudly and
  the addon will need an update. The vanilla `Explosion` / `Level` hooks are
  version-independent within 1.21.1.
* Nuclear terrain tasks are cancelled wholesale; the block whitelist is not
  applied per-block inside a nuke wave. This is intentional — a nuke either
  carves terrain or it does not.
* Fire that is already burning (from before the addon was installed, or from
  another mod/player) still spreads normally.
* Only `mianbaos_modernwarfare` is affected. Other mods' explosions, Create
  cannons, TNT and players are untouched.

## Build

```powershell
cd mianbao-blockguard
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.10'
.\gradlew.bat build
```

The jar is written to `build/libs/mianbao_blockguard-1.0.0.jar`.

A dev server can be started with the target mod staged in `libs`:

```powershell
.\gradlew.bat runServer -PwithTarget
```

(The dev-server task needs `net.neoforged:DevLaunch:1.0.2` from
`maven.neoforged.net`, so it only works when that host is reachable.)
