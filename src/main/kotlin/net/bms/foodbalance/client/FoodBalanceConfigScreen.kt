package net.bms.foodbalance.client

import net.bms.foodbalance.BalanceConfig
import net.bms.foodbalance.EffectRules
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class FoodBalanceConfigScreen(private val parent: Screen) : Screen(Component.literal("Let's Do Food Balance")) {
    override fun init() {
        var y = height / 2 - 82

        addToggle(y, "Farmer's Blessing", {
            BalanceConfig.isFoodEffectBlocked(EffectRules.FARMERS_BLESSING_ITEMS, EffectRules.FARMERS_BLESSING)
        }) { disabled ->
            BalanceConfig.setFoodEffectBlocked(EffectRules.FARMERS_BLESSING_ITEMS, EffectRules.FARMERS_BLESSING, disabled)
        }
        y += 24

        addToggle(y, "Grandma's Blessing", {
            BalanceConfig.isFoodEffectBlocked(EffectRules.GRANDMAS_BLESSING_ITEMS, EffectRules.GRANDMAS_BLESSING)
        }) { disabled ->
            BalanceConfig.setFoodEffectBlocked(EffectRules.GRANDMAS_BLESSING_ITEMS, EffectRules.GRANDMAS_BLESSING, disabled)
        }
        y += 24

        addToggle(y, "Stoutheart", {
            BalanceConfig.isFoodEffectBlocked(EffectRules.STOUTHEART_ITEMS, EffectRules.STOUTHEART)
        }) { disabled ->
            BalanceConfig.setFoodEffectBlocked(EffectRules.STOUTHEART_ITEMS, EffectRules.STOUTHEART, disabled)
        }
        y += 24

        addRenderableWidget(
            Button.builder(Component.literal("Vinery Drinks...")) {
                minecraft?.setScreen(DrinkConfigScreen(this, DrinkConfigScreen.Type.VINERY))
            }.bounds(width / 2 - 100, y, 200, 20).build()
        )
        y += 24

        addRenderableWidget(
            Button.builder(Component.literal("Brewery Drinks...")) {
                minecraft?.setScreen(DrinkConfigScreen(this, DrinkConfigScreen.Type.BREWERY))
            }.bounds(width / 2 - 100, y, 200, 20).build()
        )
        y += 24

        addEnabledToggle(y, "Brewery Drunk", { BalanceConfig.value.keepBreweryDrunk }) { enabled ->
            BalanceConfig.value.keepBreweryDrunk = enabled
        }
        y += 24

        addToggle(y, "Incapacitated Golden Foods", { BalanceConfig.value.disableIncapacitatedGoldenFoods }) { disabled ->
            BalanceConfig.value.disableIncapacitatedGoldenFoods = disabled
        }

        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) { closeScreen() }
                .bounds(width / 2 - 100, height - 32, 200, 20)
                .build()
        )
    }

    private fun addToggle(y: Int, name: String, get: () -> Boolean, set: (Boolean) -> Unit) {
        lateinit var button: Button
        button = Button.builder(disabledLabel(name, get())) {
            set(!get())
            button.message = disabledLabel(name, get())
            BalanceConfig.save()
        }.bounds(width / 2 - 100, y, 200, 20).build()
        addRenderableWidget(button)
    }

    private fun addEnabledToggle(y: Int, name: String, get: () -> Boolean, set: (Boolean) -> Unit) {
        lateinit var button: Button
        button = Button.builder(enabledLabel(name, get())) {
            set(!get())
            button.message = enabledLabel(name, get())
            BalanceConfig.save()
        }.bounds(width / 2 - 100, y, 200, 20).build()
        addRenderableWidget(button)
    }

    private fun disabledLabel(name: String, value: Boolean): Component {
        return Component.literal("$name: ${if (value) "Disabled" else "Enabled"}")
    }

    private fun enabledLabel(name: String, value: Boolean): Component {
        return Component.literal("$name: ${if (value) "Enabled" else "Disabled"}")
    }

    private fun closeScreen() {
        BalanceConfig.save()
        minecraft?.setScreen(parent)
    }

    override fun onClose() {
        closeScreen()
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics, mouseX, mouseY, partialTick)
        super.render(graphics, mouseX, mouseY, partialTick)
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFFFF.toInt())
    }
}
