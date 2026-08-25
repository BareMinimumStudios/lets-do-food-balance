package net.bms.foodbalance.mixin;

import net.bms.foodbalance.BalanceConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.cartoonishvillain.incapacitated.events.AbstractedIncapacitation", remap = false)
abstract class IncapacitatedMixin {
    @Inject(method = "eat", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void eat(LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        if (!BalanceConfig.disableIncapacitatedGoldenFoods()) return;

        if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.GOLDEN_CARROT) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            ci.cancel();
        }
    }
}
