package net.bms.foodbalance

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path

object BalanceConfig {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val path: Path = FabricLoader.getInstance().configDir.resolve("food_balance.json")

    var value: Config = Config()
        private set

    fun load() {
        value = if (Files.exists(path)) {
            try {
                Files.newBufferedReader(path).use { reader ->
                    val json = JsonParser.parseReader(reader).asJsonObject
                    val config = gson.fromJson(json, Config::class.java) ?: Config()
                    migrate(json, config)
                    config
                }
            } catch (exception: Exception) {
                FoodBalance.LOGGER.error("failed to read {}.", path, exception)
                Config()
            }
        } else {
            Config()
        }

        value.normalize()
        save()
        FoodBalance.LOGGER.info("loaded configuration from {}.", path.toAbsolutePath())
    }

    fun save() {
        try {
            Files.createDirectories(path.parent)
            Files.newBufferedWriter(path).use { writer ->
                gson.toJson(value, writer)
            }
        } catch (exception: Exception) {
            FoodBalance.LOGGER.error("failed to write {}.", path, exception)
        }
    }


    private fun migrate(json: JsonObject, config: Config) {
        if (!json.has("disabledVineryDrinks") && json.has("disableVineryEffects")) {
            val disabled = json.get("disableVineryEffects").asBoolean
            config.disabledVineryDrinks = if (disabled) {
                legacyItems(json, "vineryItems", EffectRules.VINERY_DRINKS.map { it.itemId })
            } else {
                linkedSetOf()
            }
        }

        if (!json.has("disabledBreweryDrinks") && json.has("disableBreweryEffects")) {
            val disabled = json.get("disableBreweryEffects").asBoolean
            config.disabledBreweryDrinks = if (disabled) {
                legacyItems(json, "breweryItems", EffectRules.BREWERY_DRINKS.map { it.itemId })
            } else {
                linkedSetOf()
            }
        }
    }

    private fun legacyItems(json: JsonObject, key: String, defaults: List<String>): MutableSet<String> {
        if (!json.has(key) || !json.get(key).isJsonArray) return defaults.toCollection(linkedSetOf())
        return json.getAsJsonArray(key).mapNotNullTo(linkedSetOf()) { element ->
            element.takeIf { it.isJsonPrimitive }?.asString
        }
    }

    @JvmStatic
    fun disableIncapacitatedGoldenFoods(): Boolean {
        return value.disableIncapacitatedGoldenFoods
    }

    fun isFoodEffectBlocked(items: Set<String>, effect: String): Boolean {
        return items.all { item -> effect in (value.rules[item] ?: emptySet()) }
    }

    fun setFoodEffectBlocked(items: Set<String>, effect: String, blocked: Boolean) {
        val rules = value.foodEffects ?: linkedMapOf<String, MutableSet<String>>().also { value.foodEffects = it }

        for (item in items) {
            if (blocked) {
                rules.getOrPut(item, ::linkedSetOf).add(effect)
            } else {
                rules[item]?.remove(effect)
                if (rules[item].isNullOrEmpty()) rules.remove(item)
            }
        }
    }

    fun isVineryDrinkDisabled(item: String): Boolean {
        return item in value.disabledVinery
    }

    fun setVineryDrinkDisabled(item: String, disabled: Boolean) {
        val items = value.disabledVineryDrinks ?: linkedSetOf<String>().also { value.disabledVineryDrinks = it }
        if (disabled) items.add(item) else items.remove(item)
    }

    fun getVineryAgeCap(item: String): Int? {
        return value.ageCaps[item]
    }

    fun setVineryAgeCap(item: String, years: Int?) {
        val caps = value.vineryAgeCaps ?: linkedMapOf<String, Int>().also { value.vineryAgeCaps = it }
        if (years == null) {
            caps.remove(item)
        } else {
            caps[item] = years.coerceAtLeast(0)
        }
    }

    @JvmStatic
    fun clampVineryAge(item: String, age: Int): Int {
        val cap = value.ageCaps[item] ?: return age
        return age.coerceAtMost(cap)
    }

    fun isBreweryDrinkDisabled(item: String): Boolean {
        return item in value.disabledBrewery
    }

    fun setBreweryDrinkDisabled(item: String, disabled: Boolean) {
        val items = value.disabledBreweryDrinks ?: linkedSetOf<String>().also { value.disabledBreweryDrinks = it }
        if (disabled) items.add(item) else items.remove(item)
    }

    class Config {
        var foodEffects: MutableMap<String, MutableSet<String>>? = defaultFoodEffects()
        var disabledVineryDrinks: MutableSet<String>? = EffectRules.VINERY_DRINKS.mapTo(linkedSetOf()) { it.itemId }
        var disabledBreweryDrinks: MutableSet<String>? = EffectRules.BREWERY_DRINKS.mapTo(linkedSetOf()) { it.itemId }
        var vineryAgeCaps: MutableMap<String, Int>? = linkedMapOf()
        var keepBreweryDrunk: Boolean = true
        var disableIncapacitatedGoldenFoods: Boolean = true

        val rules: Map<String, Set<String>>
            get() = foodEffects ?: emptyMap()

        val disabledVinery: Set<String>
            get() = disabledVineryDrinks ?: emptySet()

        val disabledBrewery: Set<String>
            get() = disabledBreweryDrinks ?: emptySet()

        val ageCaps: Map<String, Int>
            get() = vineryAgeCaps ?: emptyMap()

        fun normalize() {
            foodEffects = (foodEffects ?: linkedMapOf())
                .filterKeys { it.isNotBlank() }
                .mapValuesTo(linkedMapOf()) { (_, effects) -> effects.filterTo(linkedSetOf()) { it.isNotBlank() } }
            disabledVineryDrinks = (disabledVineryDrinks ?: linkedSetOf()).filterTo(linkedSetOf()) { it.isNotBlank() }
            disabledBreweryDrinks = (disabledBreweryDrinks ?: linkedSetOf()).filterTo(linkedSetOf()) { it.isNotBlank() }
            vineryAgeCaps = (vineryAgeCaps ?: linkedMapOf())
                .filterKeys { it.isNotBlank() }
                .mapValuesTo(linkedMapOf()) { (_, years) -> years.coerceAtLeast(0) }
        }

        companion object {
            private fun defaultFoodEffects(): MutableMap<String, MutableSet<String>> = linkedMapOf(
                "farm_and_charm:farmers_breakfast" to linkedSetOf("farm_and_charm:farmers_blessing"),
                "farm_and_charm:farmers_bread" to linkedSetOf("farm_and_charm:farmers_blessing"),
                "farm_and_charm:oatmeal_with_strawberries" to linkedSetOf("farm_and_charm:farmers_blessing"),
                "farm_and_charm:grandmothers_strawberry_cake" to linkedSetOf("farm_and_charm:grandmas_blessing"),
                "brewery:sausage" to linkedSetOf("brewery:stoutheart"),
                "brewery:pretzel" to linkedSetOf("brewery:stoutheart"),
                "brewery:pork_knuckle" to linkedSetOf("brewery:stoutheart"),
                "brewery:fried_chicken" to linkedSetOf("brewery:stoutheart"),
                "brewery:half_chicken" to linkedSetOf("brewery:stoutheart"),
                "brewery:mashed_potatoes" to linkedSetOf("brewery:stoutheart"),
                "brewery:potato_salad" to linkedSetOf("brewery:stoutheart"),
                "brewery:dumplings" to linkedSetOf("brewery:stoutheart")
            )
        }
    }
}
