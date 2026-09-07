package net.bms.foodbalance.client

import net.bms.foodbalance.BalanceConfig
import net.bms.foodbalance.EffectRules
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class DrinkConfigScreen(
    private val parent: Screen,
    private val type: Type,
    private val page: Int = 0
) : Screen(Component.literal(type.title)) {
    private val drinks: List<EffectRules.Drink>
        get() = if (type == Type.VINERY) EffectRules.VINERY_DRINKS else EffectRules.BREWERY_DRINKS

    override fun init() {
        val perPage = entriesPerPage()
        val pages = maxOf(1, (drinks.size + perPage - 1) / perPage)
        val currentPage = page.coerceIn(0, pages - 1)
        val entries = drinks.drop(currentPage * perPage).take(perPage)
        val buttonWidth = minOf(320, width - 40)

        entries.forEachIndexed { index, drink ->
            val y = 42 + index * 32
            val effectWidth = if (type == Type.VINERY) buttonWidth - 104 else buttonWidth
            val left = width / 2 - buttonWidth / 2

            lateinit var button: Button
            button = Button.builder(drinkLabel(drink)) {
                setDisabled(drink.itemId, !isDisabled(drink.itemId))
                button.message = drinkLabel(drink)
                BalanceConfig.save()
            }.bounds(left, y, effectWidth, 20).build()
            addRenderableWidget(button)

            if (type == Type.VINERY) {
                addRenderableWidget(
                    Button.builder(ageLabel(drink.itemId)) {
                        minecraft?.setScreen(WineAgingConfigScreen(this, drink))
                    }.bounds(left + effectWidth + 4, y, 100, 20).build()
                )
            }
        }

        val navigationY = height - 56
        val halfWidth = 98

        if (currentPage > 0) {
            addRenderableWidget(
                Button.builder(Component.literal("Previous")) {
                    minecraft?.setScreen(DrinkConfigScreen(parent, type, currentPage - 1))
                }.bounds(width / 2 - 100, navigationY, halfWidth, 20).build()
            )
        }

        if (currentPage < pages - 1) {
            addRenderableWidget(
                Button.builder(Component.literal("Next")) {
                    minecraft?.setScreen(DrinkConfigScreen(parent, type, currentPage + 1))
                }.bounds(width / 2 + 2, navigationY, halfWidth, 20).build()
            )
        }

        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) {
                BalanceConfig.save()
                minecraft?.setScreen(parent)
            }.bounds(width / 2 - 100, height - 32, 200, 20).build()
        )
    }

    private fun entriesPerPage(): Int {
        return ((height - 105) / 32).coerceIn(1, 6)
    }

    private fun isDisabled(itemId: String): Boolean {
        return if (type == Type.VINERY) {
            BalanceConfig.isVineryDrinkDisabled(itemId)
        } else {
            BalanceConfig.isBreweryDrinkDisabled(itemId)
        }
    }

    private fun setDisabled(itemId: String, disabled: Boolean) {
        if (type == Type.VINERY) {
            BalanceConfig.setVineryDrinkDisabled(itemId, disabled)
        } else {
            BalanceConfig.setBreweryDrinkDisabled(itemId, disabled)
        }
    }

    private fun drinkLabel(drink: EffectRules.Drink): Component {
        return Component.literal("${drink.name}: ${if (isDisabled(drink.itemId)) "Disabled" else "Enabled"}")
    }

    private fun ageLabel(itemId: String): Component {
        val cap = BalanceConfig.getVineryAgeCap(itemId)
        return Component.literal(
            when (cap) {
                null -> "Age: Default"
                0 -> "Age: Off"
                else -> "Age: $cap"
            }
        )
    }

    override fun onClose() {
        BalanceConfig.save()
        minecraft?.setScreen(parent)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics, mouseX, mouseY, partialTick)

        val perPage = entriesPerPage()
        val pages = maxOf(1, (drinks.size + perPage - 1) / perPage)
        val currentPage = page.coerceIn(0, pages - 1)
        val entries = drinks.drop(currentPage * perPage).take(perPage)

        super.render(graphics, mouseX, mouseY, partialTick)

        graphics.drawCenteredString(font, Component.literal("${type.title} (${currentPage + 1}/$pages)"), width / 2, 12, 0xFFFFFFFF.toInt())
        graphics.drawCenteredString(
            font,
            Component.literal(if (type == Type.VINERY) "Configure each drink's effect and maximum age" else "Toggle each drink's special effect")
                .withStyle(ChatFormatting.GRAY),
            width / 2,
            24,
            0xFFA0A0A0.toInt()
        )

        entries.forEachIndexed { index, drink ->
            graphics.drawCenteredString(
                font,
                Component.literal(drink.effectName).withStyle(ChatFormatting.DARK_GRAY),
                width / 2,
                63 + index * 32,
                0xFF808080.toInt()
            )
        }

    }

    enum class Type(val title: String) {
        VINERY("Vinery Drinks"),
        BREWERY("Brewery Drinks")
    }
}
