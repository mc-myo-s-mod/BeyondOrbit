# Beyond Orbit: Core

Beyond Orbit: Core is a NeoForge 1.21.1 mod that turns celestial bodies into data-driven, server-side resource targets. The current playable loop is intentionally small but complete: craft a satellite, deploy it through an uplink, let the server tick mine resources, then collect the buffer.

## Player Loop

1. Craft an `Orbital Data Core` from copper, iron, and redstone.
2. Use the core to craft a `Basic Satellite`.
3. Use the core again to craft a `Satellite Uplink` block.
4. Place the `Satellite Uplink` in the world.
5. Let the `Satellite Uplink` charge passively, then right-click it while holding a `Basic Satellite` to deploy a location-bound mining satellite.
6. Empty-hand right-click opens the Uplink status screen; sneak + empty-hand right-click collects accumulated resources.
7. For a heavier launch-pad mission, craft a `Rocket Frame`, `Orbital Mining Module`, and `Launch Pad`, then use a `Basic Satellite` on the pad.

## Commands

`/beyondorbit` or `/beyondorbit guide` is available as an in-game quick start.

Admin/debug commands:

```mcfunction
/beyondorbit
/beyondorbit guide
/beyondorbit status
/beyondorbit config
/beyondorbit planets
/beyondorbit bodies
/beyondorbit satellites
/beyondorbit celestial list
/beyondorbit celestial detail <body>
/beyondorbit celestial resources <body>
/beyondorbit celestial remaining <body>
/beyondorbit celestial extract <body> <rolls>
/beyondorbit celestial reset <body>
/beyondorbit satellite list
/beyondorbit satellite startMining <satellite> <body> <rolls> <intervalTicks>
/beyondorbit satellite status <satellite>
/beyondorbit satellite tick <ticks>
/beyondorbit satellite stop <satellite>
/beyondorbit satellite clearBuffer <satellite>
/beyondorbit satellite collect <satellite>
```

Viewing commands such as `planets`, `satellites`, `status`, `config`, and `celestial detail/resources` are available without operator permission. Mutating/debug commands such as `extract`, `reset`, `startMining`, `tick`, `stop`, and `clearBuffer` require permission level 2.

## Datapack Celestial Bodies

Celestial body definitions are loaded from:

```text
data/<namespace>/beyondorbit/celestial_bodies/*.json
```

The bundled examples include:

```text
beyondorbit:crimson_asteroid
beyondorbit:aurelia
```

Finite resources are stored per world in `BeyondOrbitSavedData`; satellite mission state and accumulated reward buffers are stored there too.

## Progression

The mod includes recipe-unlock advancements and a small Beyond Orbit advancement chain:

```text
Beyond Orbit
→ First Satellite
→ Orbital Uplink
→ Launch Infrastructure
```

These are intentionally lightweight. They teach the loop without forcing a quest-book dependency, while the Uplink screen exposes machine status in-game.

## Server Config

Common config sections:

```toml
[resources]
infiniteCelestialResources = false
forceFiniteResources = false
forceInfiniteResources = false
resourceAmountMultiplier = 1.0
resourceYieldMultiplier = 1.0
maxExtractionRollsPerOperation = 64

[satellites]
uplinkRollsPerExtraction = 16
uplinkTicksPerExtraction = 200
uplinkEnergyCapacity = 10000
uplinkDeployEnergyCost = 1000
uplinkEnergyGeneratedPerTick = 10
launchPadRollsPerExtraction = 32
launchPadTicksPerExtraction = 160
```

## Verification

The current implementation is covered by GameTests for:

- finite celestial reserve depletion
- satellite mission ticking and accumulation
- playable content registration and mining tags
- Satellite Uplink deploy/mine/collect interaction

Useful verification commands:

```text
./gradlew compileJava processResources --console=plain
./gradlew runGameTestServer --console=plain
./gradlew runServer --console=plain
```
