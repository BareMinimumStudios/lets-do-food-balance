package net.bms.foodbalance.mixin;

import net.bms.foodbalance.ConsumptionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.satisfy.farm_and_charm.core.block.FoodBlock", remap = false)
abstract class FoodBlockMixin {
    @Inject(method = "tryEat", at = @At("HEAD"), require = 0, remap = false)
    private void begin(LevelAccessor level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<InteractionResult> cir) {
        Block block = (Block) (Object) this;
        ConsumptionContext.INSTANCE.push(new ItemStack(block.asItem()));
    }

    @Inject(method = "tryEat", at = @At("RETURN"), require = 0, remap = false)
    private void end(LevelAccessor level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<InteractionResult> cir) {
        ConsumptionContext.INSTANCE.pop();
    }
}
