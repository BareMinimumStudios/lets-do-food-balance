package net.bms.foodbalance

import com.mojang.logging.LogUtils
import org.slf4j.Logger

object FoodBalance {
    const val MOD_ID: String = "food_balance"
    val LOGGER: Logger = LogUtils.getLogger()

    @JvmStatic
    fun init() {
        FoodBalanceComponents.init()
        BalanceConfig.load()
        VineryStacking.init()
        VineryMigration.init()
        LOGGER.info("loaded food balance configuration.")
    }
}
