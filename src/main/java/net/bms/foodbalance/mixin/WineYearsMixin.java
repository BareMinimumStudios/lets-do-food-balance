package net.bms.foodbalance.mixin;

import net.bms.foodbalance.VineryAging;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.satisfy.vinery.core.util.WineYears", remap = false)
abstract class WineYearsMixin {
    @Inject(method = "getWineAgeYears", at = @At("HEAD"), require = 0, remap = false)
    private static void normalizeYears(ItemStack stack, Level level, CallbackInfoReturnable<Integer> cir) {
        if (!level.isClientSide) VineryAging.normalize(stack);
    }

    @Inject(method = "getWineAgeDays", at = @At("HEAD"), require = 0, remap = false)
    private static void normalizeDays(ItemStack stack, Level level, CallbackInfoReturnable<Integer> cir) {
        if (!level.isClientSide) VineryAging.normalize(stack);
    }

    @Inject(method = "getWineAgeYears", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private static void ageYears(ItemStack stack, Level level, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(VineryAging.ageYears(stack, level, cir.getReturnValue()));
    }

    @Inject(method = "getWineAgeDays", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private static void ageDays(ItemStack stack, Level level, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(VineryAging.ageDays(stack, cir.getReturnValue()));
    }
}
