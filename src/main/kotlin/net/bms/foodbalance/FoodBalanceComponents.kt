package net.bms.foodbalance

import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

object FoodBalanceComponents {
    @JvmField
    val LEGACY_WINE_AGE_OFFSET_DAYS: DataComponentType<Int> = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        ResourceLocation.fromNamespaceAndPath(FoodBalance.MOD_ID, "wine_age_offset_days"),
        DataComponentType.builder<Int>().persistent(Codec.INT).build()
    )

    fun init() = Unit
}
