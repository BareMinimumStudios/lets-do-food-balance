# Changelog

## 1.0.0

- Added configurable food effect balancing for Let's Do: Farm & Charm and Brewery.
- Added individual effect controls for Vinery and Brewery drinks.
- Added configurable Vinery wine aging caps.
- Added Vinery bottle stack size balancing.
- Added Incapacitated golden food compatibility.
- Added Mod Menu configuration and matching tooltip filtering.
- Fixed Mod Menu text rendering on 1.21.1 by using opaque ARGB colors and foreground render ordering.
- Added placed food compatibility.
- Added `/wine age` compatibility so debug aging works independently of world age while respecting configured wine caps.
- Added event-driven migration for existing Vinery bottles so stored aging and effect rule values follow the current Vinery configuration without per-tick inventory or chunk-container scans.
- Added atomic configuration saves and legacy wine-age offset migration to vanilla custom data.
