package net.bms.foodbalance

import net.fabricmc.api.ModInitializer

class FoodBalanceFabricEntrypoint : ModInitializer {
    override fun onInitialize() {
        FoodBalance.init()
    }
}
