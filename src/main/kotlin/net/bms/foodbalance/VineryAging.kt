package net.bms.foodbalance

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack

object VineryAging {
    @JvmStatic
    fun clamp(stack: ItemStack, age: Int): Int {
        val itemId = BuiltInRegistries.ITEM.getKey(stack.item).toString()
        return BalanceConfig.clampVineryAge(itemId, age)
    }
}
