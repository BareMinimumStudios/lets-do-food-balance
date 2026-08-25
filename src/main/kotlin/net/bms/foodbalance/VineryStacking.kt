package net.bms.foodbalance

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

object VineryStacking {
    private const val STACK_SIZE: Int = 4

    fun init() {
        DefaultItemComponentEvents.MODIFY.register { context ->
            for (drink in EffectRules.VINERY_DRINKS) {
                BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(drink.itemId)).ifPresent { item ->
                    context.modify(item) { builder ->
                        builder.set(DataComponents.MAX_STACK_SIZE, STACK_SIZE)
                    }
                }
            }
        }
    }
}
