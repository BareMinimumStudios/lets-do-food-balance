package net.bms.foodbalance.client

import net.bms.foodbalance.BalanceConfig
import net.bms.foodbalance.EffectRules
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class WineAgingConfigScreen(
    private val parent: Screen,
    private val drink: EffectRules.Drink
) : Screen(Component.literal("${drink.name} Aging")) {
    private var cap: Int?
        get() = BalanceConfig.getVineryAgeCap(drink.itemId)
        set(value) {
            BalanceConfig.setVineryAgeCap(drink.itemId, value)
            BalanceConfig.save()
        }

    override fun init() {
        val center = width / 2
        val y = height / 2 - 28

        addRenderableWidget(
            Button.builder(Component.literal("-5")) { change(-5) }
                .bounds(center - 122, y, 46, 20)
                .build()
        )
        addRenderableWidget(
            Button.builder(Component.literal("-1")) { change(-1) }
                .bounds(center - 72, y, 46, 20)
                .build()
        )
        addRenderableWidget(
            Button.builder(Component.literal("+1")) { change(1) }
                .bounds(center + 26, y, 46, 20)
                .build()
        )
        addRenderableWidget(
            Button.builder(Component.literal("+5")) { change(5) }
                .bounds(center + 76, y, 46, 20)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.literal("Use Vinery Default")) { cap = null }
                .bounds(center - 122, y + 28, 120, 20)
                .build()
        )
        addRenderableWidget(
            Button.builder(Component.literal("Disable Aging")) { cap = 0 }
                .bounds(center + 2, y + 28, 120, 20)
                .build()
        )

        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) {
                minecraft?.setScreen(parent)
            }.bounds(center - 100, height - 32, 200, 20).build()
        )
    }

    private fun change(amount: Int) {
        val current = cap ?: 0
        cap = (current + amount).coerceIn(0, 9999)
    }

    private fun capText(): String {
        return when (val value = cap) {
            null -> "Vinery Default (uncapped)"
            0 -> "Aging Disabled"
            else -> "$value years"
        }
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
    }

    override fun render(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(graphics, mouseX, mouseY, partialTick)
        super.render(graphics, mouseX, mouseY, partialTick)

        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFFFF.toInt())
        graphics.drawCenteredString(
            font,
            Component.literal("Maximum Age: ${capText()}"),
            width / 2,
            height / 2 - 54,
            0xFFFFFFFF.toInt()
        )
        graphics.drawCenteredString(
            font,
            Component.literal("Vinery's normal effect limits still apply").withStyle(ChatFormatting.GRAY),
            width / 2,
            height / 2 + 28,
            0xFFA0A0A0.toInt()
        )
    }
}
