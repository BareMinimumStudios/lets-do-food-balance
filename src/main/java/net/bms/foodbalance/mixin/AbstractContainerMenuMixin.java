package net.bms.foodbalance.mixin;

import net.bms.foodbalance.VineryAging;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
abstract class AbstractContainerMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"))
    private void normalizeClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (player.level().isClientSide) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (slotId >= 0 && slotId < menu.slots.size()) {
            var slot = menu.getSlot(slotId);
            if (VineryAging.normalize(slot.getItem())) slot.setChanged();
        }

        VineryAging.normalize(menu.getCarried());
    }
}
