package com.supermartijn642.oregrowth.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Created 17/07/2026 by SuperMartijn642
 */
@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    @WrapWithCondition(
        method = "moveBlocks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/piston/PistonBaseBlock;dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"
        )
    )
    private boolean cancelDropsIfToolIsRequired(BlockState state, LevelAccessor level, BlockPos pos, BlockEntity entity){
        if(state.getBlock() != OreGrowth.ORE_GROWTH_BLOCK && state.getBlock() != OreGrowth.COMPLETE_ORE_GROWTH_BLOCK)
            return true;
        return !state.requiresCorrectToolForDrops();
    }
}
