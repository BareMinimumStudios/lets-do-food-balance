package net.bms.foodbalance

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.effect.MobEffectInstance

object EffectRules {
    const val FARMERS_BLESSING: String = "farm_and_charm:farmers_blessing"
    const val GRANDMAS_BLESSING: String = "farm_and_charm:grandmas_blessing"
    const val STOUTHEART: String = "brewery:stoutheart"

    val FARMERS_BLESSING_ITEMS: Set<String> = linkedSetOf(
        "farm_and_charm:farmers_breakfast",
        "farm_and_charm:farmers_bread",
        "farm_and_charm:oatmeal_with_strawberries"
    )

    val GRANDMAS_BLESSING_ITEMS: Set<String> = linkedSetOf(
        "farm_and_charm:grandmothers_strawberry_cake"
    )

    val STOUTHEART_ITEMS: Set<String> = linkedSetOf(
        "brewery:sausage",
        "brewery:pretzel",
        "brewery:pork_knuckle",
        "brewery:fried_chicken",
        "brewery:half_chicken",
        "brewery:mashed_potatoes",
        "brewery:potato_salad",
        "brewery:dumplings"
    )

    data class Drink(
        val itemId: String,
        val effectId: String,
        val name: String,
        val effectName: String
    )

    const val BREWERY_DRUNK: String = "brewery:drunk"

    val VINERY_DRINKS: List<Drink> = listOf(
        Drink("vinery:apple_cider", "minecraft:strength", "Apple Cider", "Strength"),
        Drink("vinery:apple_wine", "minecraft:resistance", "Apple Wine", "Resistance"),
        Drink("vinery:mead", "minecraft:haste", "Mead", "Haste"),
        Drink("vinery:glowing_wine", "minecraft:glowing", "Glowing Wine", "Glowing"),
        Drink("vinery:solaris_wine", "minecraft:health_boost", "Solaris Wine", "Health Boost"),
        Drink("vinery:kelp_cider", "vinery:water_walker", "Kelp Cider", "Water Walker"),
        Drink("vinery:eiswein", "vinery:frosty_armor", "Eiswein", "Frosty Armor"),
        Drink("vinery:aegis_wine", "vinery:armor_effect", "Aegis Wine", "Armor"),
        Drink("vinery:villagers_fright", "minecraft:bad_omen", "Villagers' Fright", "Bad Omen"),
        Drink("vinery:clark_wine", "vinery:double_jump", "Clark Wine", "Double Jump"),
        Drink("vinery:jellie_wine", "vinery:jellie", "Jellie Wine", "Jellie"),
        Drink("vinery:noir_wine", "minecraft:jump_boost", "Noir Wine", "Jump Boost"),
        Drink("vinery:red_wine", "minecraft:slow_falling", "Red Wine", "Slow Falling"),
        Drink("vinery:strad_wine", "minecraft:night_vision", "Strad Wine", "Night Vision"),
        Drink("vinery:cherry_wine", "minecraft:invisibility", "Cherry Wine", "Invisibility"),
        Drink("vinery:cristel_wine", "minecraft:water_breathing", "Cristel Wine", "Water Breathing"),
        Drink("vinery:lilitu_wine", "vinery:party_effect", "Lilitu Wine", "Party"),
        Drink("vinery:jo_special_mixture", "vinery:climbing_effect", "Jo's Special Mixture", "Climbing"),
        Drink("vinery:bolvar_wine", "vinery:lava_walker", "Bolvar Wine", "Lava Walker"),
        Drink("vinery:magnetic_wine", "vinery:magnet", "Magnetic Wine", "Magnet"),
        Drink("vinery:stal_wine", "vinery:health_effect", "Stal Wine", "Health"),
        Drink("vinery:chenet_wine", "vinery:climbing_effect", "Chenet Wine", "Climbing"),
        Drink("vinery:bottle_mojang_noir", "vinery:experience_effect", "Mojang Noir", "Experience"),
        Drink("vinery:chorus_wine", "vinery:teleport", "Chorus Wine", "Teleport"),
        Drink("vinery:creepers_crush", "vinery:creeper_effect", "Creeper's Crush", "Creeper"),
        Drink("vinery:mellohi_wine", "minecraft:instant_health", "Mellohi Wine", "Instant Health")
    )

    val BREWERY_DRINKS: List<Drink> = listOf(
        Drink("brewery:beer_wheat", "brewery:snowwhite", "Wheat Beer", "Snow White"),
        Drink("brewery:beer_hops", "brewery:partystarter", "Hops Beer", "Party Starter"),
        Drink("brewery:beer_barley", "brewery:pintcharisma", "Barley Beer", "Pint Charisma"),
        Drink("brewery:beer_oat", "brewery:mining", "Oat Beer", "Mining"),
        Drink("brewery:beer_nettle", "brewery:pacify", "Nettle Beer", "Pacify"),
        Drink("brewery:beer_haley", "brewery:haley", "Haley Beer", "Haley"),
        Drink("brewery:whiskey_maggoallan", "brewery:healingtouch", "Maggoallan Whiskey", "Healing Touch"),
        Drink("brewery:whiskey_carrasconlabel", "brewery:renewingtouch", "Carrascon Label Whiskey", "Renewing Touch"),
        Drink("brewery:whiskey_lilitusinglemalt", "brewery:partystarter", "Lilitu Single Malt", "Party Starter"),
        Drink("brewery:whiskey_jojannik", "brewery:toxictouch", "Jojannik Whiskey", "Toxic Touch"),
        Drink("brewery:whiskey_cristelwalker", "brewery:protectivetouch", "Cristelwalker Whiskey", "Protective Touch"),
        Drink("brewery:whiskey_ak", "brewery:lightning_strike", "AK Whiskey", "Lightning Strike"),
        Drink("brewery:whiskey_highland_hearth", "brewery:repulsion", "Highland Hearth", "Repulsion"),
        Drink("brewery:whiskey_jamesons_malt", "brewery:explosion", "Jameson's Malt", "Explosion"),
        Drink("brewery:whiskey_smokey_reverie", "brewery:combustion", "Smokey Reverie", "Combustion")
    )

    private val vineryEffects: Map<String, String> = VINERY_DRINKS.associate { it.itemId to it.effectId }
    private val breweryEffects: Map<String, String> = BREWERY_DRINKS.associate { it.itemId to it.effectId }

    fun blocks(entry: ConsumptionContext.Entry?, effect: MobEffectInstance): Boolean {
        if (entry == null) return false

        val effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.effect.value()).toString()
        val config = BalanceConfig.value

        if (effectId in (config.rules[entry.itemId] ?: emptySet())) {
            return true
        }

        if (entry.itemId in config.disabledVinery && vineryEffects[entry.itemId] == effectId) {
            return true
        }

        if (entry.itemId in breweryEffects) {
            if (entry.itemId in config.disabledBrewery && breweryEffects[entry.itemId] == effectId) return true
            if (!config.keepBreweryDrunk && effectId == BREWERY_DRUNK) return true
        }

        return false
    }

    fun blockedTooltipEffects(itemId: String): Set<String> {
        val config = BalanceConfig.value
        val effects = linkedSetOf<String>()

        effects.addAll(config.rules[itemId] ?: emptySet())

        if (itemId in config.disabledVinery) {
            vineryEffects[itemId]?.let(effects::add)
        }

        if (itemId in config.disabledBrewery) {
            breweryEffects[itemId]?.let(effects::add)
        }

        return effects
    }
}
