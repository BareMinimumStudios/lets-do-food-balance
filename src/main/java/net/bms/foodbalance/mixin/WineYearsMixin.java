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
    @Inject(method = "getWineAgeYears", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private static void clampAge(ItemStack stack, Level level, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(VineryAging.clamp(stack, cir.getReturnValue()));
    }
}
