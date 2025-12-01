# Drenched Wings

A Minecraft Fabric mod that adds liquid-based elytra restrictions, environmental nerfs, and cooldown mechanics, fully
controlled through server game rules.

Inspired by **cubfan135’s “[Minecraft’s Elytra Problem](https://www.youtube.com/watch?v=fBbtP9GZITM)”** — implements his
proposed liquid-based Elytra limitations as a configurable, server-side Fabric mod.

## Core Mechanics

**Environmental Nerfs**

- **20-second cooldown** for elytra in rain, water, or lava.
- **Firework boosts disabled** during rain.

**Splash-Water Cooldown**

- Hitting a player with a **splash water bottle** disables elytra for **up to 20-seconds**, scaled by proximity.

**Server-Side Control**

- All effects configurable via server game rules.
- Fully **vanilla-compliant** using Minecraft’s `ItemCooldowns` system.

## Game Rules

- `doElytraNerf` – Global toggle for all elytra nerfs (default: true)
- `disableElytraInRain` – Disables elytra in rain (default: true)
- `disableElytraInWater` – Disables elytra in water (default: true)
- `disableElytraInLava` – Disables elytra in lava (default: true)
- `disableRocketsInRain` – Prevent firework rockets boost in rain (default: true)
- `applyElytraCooldownFromSplashWater` – Apply cooldown when hit by splash water bottle (default: true)

## LICENSE

This project is licensed under the [MIT License](LICENSE).
