package net.bms.foodbalance.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback

class FoodBalanceClientEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            TooltipRules.apply(stack, lines)
        }
    }
}
