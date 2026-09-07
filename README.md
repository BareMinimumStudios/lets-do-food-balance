# Let's Do Food Balance

Let's Do Food Balance is a Fabric 1.21.1 compatibility and balance mod for selected food and drink effects from Let's Do: Vinery, Let's Do: Brewery, and Let's Do: Farm & Charm.

It also includes optional compatibility for Incapacitated to prevent golden apples, golden carrots, and enchanted golden apples from changing the incapacitation state, bleedout timer, or self-revive behavior.

It is not a general-purpose food effect editor. Its built-in rules are specifically designed for the supported mods.

## Configuration

The configuration file is stored at `config/food_balance.json`.

When Mod Menu is installed, Let's Do Food Balance provides an in-game configuration screen.

Farmer's Blessing, Grandma's Blessing, and Stoutheart can be controlled independently. Vinery and Brewery each have their own drink configuration pages where every supported wine, cider, mead, beer, and whiskey can have its special effect enabled or disabled individually. Disabling a drink effect does not disable the item or change its normal consumption behavior.

Each Vinery drink can also have its own maximum age. `Vinery Default` leaves Vinery's normal aging behavior untouched and does not add an age ceiling, `0` disables aging for that drink, and any positive value caps its effective age at that many years. Vinery's own maximum effect level and maximum duration still apply independently.

Vinery's `/wine age <years>` debug command is supported. It can assign the requested raw age even when the world is younger than that value; the configured per-wine maximum still controls the wine's effective age.

Existing Vinery bottles are migrated to the current Vinery aging and effect settings without a global inventory tick or chunk-container scan. Player inventories and ender chests are checked once when a player joins, a container stack is checked only when a player actually interacts with that slot, and an individual bottle is checked again when Vinery evaluates or consumes it. Unopened loot containers are never touched by the migration. The brewed date is preserved while stored rule values are refreshed.

Brewery's Drunk effect can be controlled separately from each drink's special effect.

Disabled effect lines are hidden from item tooltips. Vinery aging information and Brewery quality information remain visible.

## Incapacitated

When Incapacitated golden-food handling is disabled, the following items keep their normal vanilla consumption behavior but no longer receive Incapacitated-specific behavior:

- Golden Apple
- Golden Carrot
- Enchanted Golden Apple

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Fabric Language Kotlin

Mod Menu and Incapacitated are optional.

Vinery wine, cider, mead, and special drink bottles stack to a maximum of 4.
