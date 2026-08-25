package net.bms.foodbalance.mixin;

import net.bms.foodbalance.ConsumptionContext;
import net.bms.foodbalance.EffectRules;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
    private void addEffect(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        if (EffectRules.INSTANCE.blocks(ConsumptionContext.INSTANCE.getCurrent(), effect)) {
            cir.setReturnValue(false);
        }
    }
}
