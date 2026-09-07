package net.bms.foodbalance

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import java.util.ArrayDeque

object ConsumptionContext {
    private val entries: ThreadLocal<ArrayDeque<Entry>?> = ThreadLocal()

    val current: Entry?
        get() = entries.get()?.peekLast()

    fun push(stack: ItemStack) {
        val values = entries.get() ?: ArrayDeque<Entry>().also(entries::set)
        values.addLast(Entry(BuiltInRegistries.ITEM.getKey(stack.item).toString()))
    }

    fun pop() {
        val values = entries.get() ?: return
        if (values.isNotEmpty()) values.removeLast()
        if (values.isEmpty()) entries.remove()
    }

    data class Entry(val itemId: String)
}
