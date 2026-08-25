package net.bms.foodbalance.client

import net.bms.foodbalance.EffectRules
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

object TooltipRules {
    fun apply(stack: ItemStack, tooltip: MutableList<Component>) {
        val itemId = BuiltInRegistries.ITEM.getKey(stack.item).toString()
        val blocked = EffectRules.blockedTooltipEffects(itemId)

        val effectNames = blocked.mapNotNull { id ->
            val effectId = ResourceLocation.tryParse(id) ?: return@mapNotNull null
            BuiltInRegistries.MOB_EFFECT.get(effectId)?.displayName?.string
        }

        var removed = false
        val iterator = tooltip.listIterator()
        while (iterator.hasNext()) {
            val line = iterator.next().string
            if (effectNames.any { name -> line == name || line.startsWith("$name ") || line.startsWith("$name (") }) {
                iterator.remove()
                removed = true
            }
        }

        val food = stack.get(DataComponents.FOOD)
        if (food != null && food.effects().isNotEmpty()) {
            val effectIds = food.effects().map { possible ->
                BuiltInRegistries.MOB_EFFECT.getKey(possible.effect().effect.value()).toString()
            }

            if (effectIds.all(blocked::contains)) {
                removeAttributeSection(tooltip)
            }
        }

        if (removed && tooltip.size > 1 && tooltip[1].string.isEmpty()) {
            tooltip.removeAt(1)
        }

        removeCompletedAgingLine(itemId, tooltip)
    }

    private fun removeCompletedAgingLine(itemId: String, tooltip: MutableList<Component>) {
        val cap = net.bms.foodbalance.BalanceConfig.getVineryAgeCap(itemId) ?: return
        val ageLine = Component.translatable("tooltip.vinery.age", cap).string
        if (tooltip.none { it.string == ageLine }) return

        tooltip.removeIf { line -> matchesTranslation(line.string, "tooltip.vinery.next_upgrade") }
    }

    private fun matchesTranslation(line: String, key: String): Boolean {
        val marker = "__food_balance_value__"
        val translated = Component.translatable(key, marker).string
        val markerIndex = translated.indexOf(marker)
        if (markerIndex < 0) return false

        val prefix = translated.substring(0, markerIndex)
        val suffix = translated.substring(markerIndex + marker.length)
        return line.startsWith(prefix) && line.endsWith(suffix)
    }

    private fun removeAttributeSection(tooltip: MutableList<Component>) {
        val header = Component.translatable("potion.whenDrank").string
        val start = tooltip.indexOfFirst { it.string == header }
        if (start < 0) return

        var end = start + 1
        while (end < tooltip.size && tooltip[end].string.isNotEmpty()) {
            end++
        }

        for (index in end - 1 downTo start) {
            tooltip.removeAt(index)
        }

        if (start > 0 && tooltip[start - 1].string.isEmpty()) {
            tooltip.removeAt(start - 1)
        }
    }
}
