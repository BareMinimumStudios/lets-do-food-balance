package net.bms.foodbalance.mixin;

import net.bms.foodbalance.ConsumptionContext;
import net.bms.foodbalance.VineryAging;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void begin(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (!level.isClientSide) VineryAging.normalize((ItemStack) (Object) this);
        ConsumptionContext.INSTANCE.push((ItemStack) (Object) this);
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void end(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        ConsumptionContext.INSTANCE.pop();
    }
}
