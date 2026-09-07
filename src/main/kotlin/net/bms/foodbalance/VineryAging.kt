package net.bms.foodbalance

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import kotlin.math.max

object VineryAging {
    private const val OFFSET_KEY: String = "food_balance_wine_age_offset_days"
    private val wineYearId = ResourceLocation.fromNamespaceAndPath("vinery", "wine_year")
    private val vineryItems = EffectRules.VINERY_DRINKS.mapTo(hashSetOf()) { ResourceLocation.parse(it.itemId) }

    @Volatile
    private var wineYearAccess: WineYearAccess? = null

    private val settingsAccess: SettingsAccess? by lazy {
        try {
            val clazz = Class.forName("net.satisfy.vinery.platform.fabric.PlatformHelperImpl")
            SettingsAccess(
                daysPerYear = clazz.getMethod("getWineDaysPerYear"),
                yearsPerEffectLevel = clazz.getMethod("getWineYearsPerEffectLevel"),
                startDuration = clazz.getMethod("getWineStartDuration"),
                durationPerYear = clazz.getMethod("getWineDurationPerYear"),
                maxDuration = clazz.getMethod("getWineMaxDuration"),
                maxLevel = clazz.getMethod("getWineMaxLevel")
            )
        } catch (_: ReflectiveOperationException) {
            null
        }
    }

    @JvmStatic
    fun normalize(stack: ItemStack): Boolean {
        if (!isVineryDrink(stack)) return false

        var changed = sanitizeMetadata(stack)
        val type = wineYearType() ?: return changed
        val component = stack.get(type) ?: return changed
        val info = wineYear(component) ?: return changed
        val settings = currentSettings() ?: return changed

        if (
            info.daysPerYear == settings.daysPerYear &&
            info.yearsPerEffectLevel == settings.yearsPerEffectLevel &&
            info.startDuration == settings.startDuration &&
            info.durationPerYear == settings.durationPerYear &&
            info.maxDuration == settings.maxDuration &&
            info.maxLevel == settings.maxLevel
        ) return changed

        val replacement = createWineYear(component.javaClass, info.brewedDay, settings) ?: return changed
        stack.set(type, replacement)
        changed = true
        return changed
    }

    @JvmStatic
    fun ageYears(stack: ItemStack, level: Level, rawAge: Int): Int {
        val info = wineYear(stack) ?: return clamp(stack, rawAge)
        val currentDay = (level.gameTime / 24000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val rawDays = max(0, currentDay - info.brewedDay)
        val offset = ageOffset(stack)
        val years = ((rawDays.toLong() + offset.toLong()) / info.daysPerYear.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        return clamp(stack, years)
    }

    @JvmStatic
    fun ageDays(stack: ItemStack, rawDays: Int): Int {
        val info = wineYear(stack)
        val offset = ageOffset(stack)
        val total = (rawDays.toLong().coerceAtLeast(0L) + offset.toLong()).coerceAtMost(Int.MAX_VALUE.toLong())
        val cap = BalanceConfig.getVineryAgeCap(itemId(stack))
        if (cap == null || info == null) return total.toInt()
        val maxDays = cap.toLong() * info.daysPerYear.toLong()
        return total.coerceAtMost(maxDays).toInt()
    }

    @JvmStatic
    fun applyCommandAge(stack: ItemStack, level: Level, years: Int) {
        normalize(stack)
        val info = wineYear(stack) ?: return
        val currentDay = (level.gameTime / 24000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val rawDays = max(0, currentDay - info.brewedDay)
        val desiredDays = years.toLong().coerceAtLeast(0L) * info.daysPerYear.toLong()
        val offset = (desiredDays - rawDays.toLong()).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        setAgeOffset(stack, offset)
    }

    private fun isVineryDrink(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        return BuiltInRegistries.ITEM.getKey(stack.item) in vineryItems
    }

    private fun clamp(stack: ItemStack, age: Int): Int {
        return BalanceConfig.clampVineryAge(itemId(stack), age)
    }

    private fun itemId(stack: ItemStack): String {
        return BuiltInRegistries.ITEM.getKey(stack.item).toString()
    }

    private fun ageOffset(stack: ItemStack): Int {
        val custom = stack.get(DataComponents.CUSTOM_DATA)
        if (custom != null) {
            val tag = custom.copyTag()
            if (tag.contains(OFFSET_KEY)) return tag.getInt(OFFSET_KEY).coerceAtLeast(0)
        }

        return stack.get(FoodBalanceComponents.LEGACY_WINE_AGE_OFFSET_DAYS)?.coerceAtLeast(0) ?: 0
    }

    private fun sanitizeMetadata(stack: ItemStack): Boolean {
        var changed = false

        val custom = stack.get(DataComponents.CUSTOM_DATA)
        if (custom != null) {
            val tag = custom.copyTag()
            if (tag.contains(OFFSET_KEY) && tag.getInt(OFFSET_KEY) <= 0) {
                tag.remove(OFFSET_KEY)
                changed = true
            }

            if (tag.isEmpty) {
                stack.remove(DataComponents.CUSTOM_DATA)
                changed = true
            } else if (changed) {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
            }
        }

        val legacy = stack.get(FoodBalanceComponents.LEGACY_WINE_AGE_OFFSET_DAYS)
        if (legacy != null) {
            setAgeOffset(stack, legacy.coerceAtLeast(0))
            changed = true
        }

        return changed
    }

    private fun setAgeOffset(stack: ItemStack, value: Int) {
        val tag = stack.get(DataComponents.CUSTOM_DATA)?.copyTag() ?: CompoundTag()
        if (value <= 0) {
            tag.remove(OFFSET_KEY)
        } else {
            tag.putInt(OFFSET_KEY, value)
        }

        if (tag.isEmpty) {
            stack.remove(DataComponents.CUSTOM_DATA)
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))
        }

        stack.remove(FoodBalanceComponents.LEGACY_WINE_AGE_OFFSET_DAYS)
    }

    @Suppress("UNCHECKED_CAST")
    private fun wineYearType(): DataComponentType<Any>? {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.get(wineYearId) as? DataComponentType<Any>
    }

    private fun wineYear(stack: ItemStack): WineYearInfo? {
        val type = wineYearType() ?: return null
        return wineYear(stack.get(type) ?: return null)
    }

    private fun wineYear(component: Any): WineYearInfo? {
        return try {
            val access = access(component.javaClass) ?: return null
            WineYearInfo(
                brewedDay = access.brewedDay(component),
                daysPerYear = access.daysPerYear(component).coerceAtLeast(1),
                yearsPerEffectLevel = access.yearsPerEffectLevel(component).coerceAtLeast(1),
                startDuration = access.startDuration(component).coerceAtLeast(0),
                durationPerYear = access.durationPerYear(component).coerceAtLeast(0),
                maxDuration = access.maxDuration(component).coerceAtLeast(0),
                maxLevel = access.maxLevel(component).coerceAtLeast(0)
            )
        } catch (_: ReflectiveOperationException) {
            null
        } catch (_: ClassCastException) {
            null
        }
    }

    private fun access(clazz: Class<*>): WineYearAccess? {
        val cached = wineYearAccess
        if (cached != null && cached.clazz == clazz) return cached

        return try {
            WineYearAccess(
                clazz = clazz,
                brewedDayMethod = clazz.getMethod("brewedDay"),
                daysPerYearMethod = clazz.getMethod("daysPerYear"),
                yearsPerEffectLevelMethod = clazz.getMethod("yearsPerEffectLevel"),
                startDurationMethod = clazz.getMethod("startDuration"),
                durationPerYearMethod = clazz.getMethod("durationPerYear"),
                maxDurationMethod = clazz.getMethod("maxDuration"),
                maxLevelMethod = clazz.getMethod("maxLevel"),
                constructor = clazz.getConstructor(
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
            ).also { wineYearAccess = it }
        } catch (_: ReflectiveOperationException) {
            null
        }
    }

    private fun currentSettings(): WineSettings? {
        val access = settingsAccess ?: return null
        return try {
            WineSettings(
                daysPerYear = access.value(access.daysPerYear).coerceAtLeast(1),
                yearsPerEffectLevel = access.value(access.yearsPerEffectLevel).coerceAtLeast(1),
                startDuration = access.value(access.startDuration).coerceAtLeast(0),
                durationPerYear = access.value(access.durationPerYear).coerceAtLeast(0),
                maxDuration = access.value(access.maxDuration).coerceAtLeast(0),
                maxLevel = access.value(access.maxLevel).coerceAtLeast(0)
            )
        } catch (_: ReflectiveOperationException) {
            null
        } catch (_: ClassCastException) {
            null
        }
    }

    private fun createWineYear(clazz: Class<*>, brewedDay: Int, settings: WineSettings): Any? {
        return try {
            val access = access(clazz) ?: return null
            access.constructor.newInstance(
                brewedDay,
                settings.daysPerYear,
                settings.yearsPerEffectLevel,
                settings.startDuration,
                settings.durationPerYear,
                settings.maxDuration,
                settings.maxLevel
            )
        } catch (_: ReflectiveOperationException) {
            null
        }
    }

    private data class WineYearAccess(
        val clazz: Class<*>,
        val brewedDayMethod: Method,
        val daysPerYearMethod: Method,
        val yearsPerEffectLevelMethod: Method,
        val startDurationMethod: Method,
        val durationPerYearMethod: Method,
        val maxDurationMethod: Method,
        val maxLevelMethod: Method,
        val constructor: Constructor<*>
    ) {
        fun brewedDay(value: Any): Int = number(brewedDayMethod, value)
        fun daysPerYear(value: Any): Int = number(daysPerYearMethod, value)
        fun yearsPerEffectLevel(value: Any): Int = number(yearsPerEffectLevelMethod, value)
        fun startDuration(value: Any): Int = number(startDurationMethod, value)
        fun durationPerYear(value: Any): Int = number(durationPerYearMethod, value)
        fun maxDuration(value: Any): Int = number(maxDurationMethod, value)
        fun maxLevel(value: Any): Int = number(maxLevelMethod, value)

        private fun number(method: Method, value: Any): Int {
            return (method.invoke(value) as Number).toInt()
        }
    }

    private data class SettingsAccess(
        val daysPerYear: Method,
        val yearsPerEffectLevel: Method,
        val startDuration: Method,
        val durationPerYear: Method,
        val maxDuration: Method,
        val maxLevel: Method
    ) {
        fun value(method: Method): Int = (method.invoke(null) as Number).toInt()
    }

    private data class WineYearInfo(
        val brewedDay: Int,
        val daysPerYear: Int,
        val yearsPerEffectLevel: Int,
        val startDuration: Int,
        val durationPerYear: Int,
        val maxDuration: Int,
        val maxLevel: Int
    )

    private data class WineSettings(
        val daysPerYear: Int,
        val yearsPerEffectLevel: Int,
        val startDuration: Int,
        val durationPerYear: Int,
        val maxDuration: Int,
        val maxLevel: Int
    )
}
