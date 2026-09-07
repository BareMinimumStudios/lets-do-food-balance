package net.bms.foodbalance.mixin;

import net.bms.foodbalance.VineryAging;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.satisfy.vinery.core.command.WineDebugCommands", remap = false)
abstract class WineDebugCommandsMixin {
    @Inject(method = "ageHeld", at = @At("HEAD"), require = 0, remap = false)
    private static void normalizeAge(CommandSourceStack source, int years, CallbackInfoReturnable<Integer> cir) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return;
        VineryAging.normalize(player.getMainHandItem());
    }

    @Inject(method = "infoHeld", at = @At("HEAD"), require = 0, remap = false)
    private static void normalizeInfo(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return;
        VineryAging.normalize(player.getMainHandItem());
    }

    @Inject(method = "ageHeld", at = @At("RETURN"), require = 0, remap = false)
    private static void ageHeld(CommandSourceStack source, int years, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() == 0) return;
        ServerPlayer player = source.getPlayer();
        if (player == null) return;
        VineryAging.applyCommandAge(player.getMainHandItem(), player.serverLevel(), years);
    }
}
