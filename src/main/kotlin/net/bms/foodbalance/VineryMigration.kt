package net.bms.foodbalance

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.Container

object VineryMigration {
    fun init() {
        if (!FabricLoader.getInstance().isModLoaded("vinery")) return

        ServerPlayerEvents.JOIN.register { player ->
            val inventoryChanged = normalize(player.inventory)
            val enderChestChanged = normalize(player.enderChestInventory)

            if (inventoryChanged) {
                player.inventory.setChanged()
                player.inventoryMenu.broadcastChanges()
            }

            if (enderChestChanged) {
                player.enderChestInventory.setChanged()
            }
        }
    }

    private fun normalize(container: Container): Boolean {
        var changed = false
        for (slot in 0 until container.containerSize) {
            val stack = container.getItem(slot)
            if (!stack.isEmpty && VineryAging.normalize(stack)) changed = true
        }
        return changed
    }
}
