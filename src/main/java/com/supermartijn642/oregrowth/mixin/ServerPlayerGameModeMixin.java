package com.supermartijn642.oregrowth.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 19/07/2026 by SuperMartijn642
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    private ServerLevel level;
    @Final
    @Shadow
    private ServerPlayer player;

    @Shadow
    private boolean removeBlock(BlockPos pos, BlockState state, boolean canHarvest){
        throw new AssertionError();
    }

    @Inject(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;removeBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z",
            ordinal = 0,
            shift = At.Shift.BEFORE
        )
    )
    private void breakCrystalsCreativeMode(BlockPos pos, CallbackInfoReturnable<Boolean> ci, @Local(ordinal = 1) BlockState state){
        if(OreGrowth.isOreGrowthBlock(state.getBlock()))
            return;
        for(Direction side : Direction.values()){
            BlockPos neighborPos = pos.relative(side);
            BlockState neighbor = this.level.getBlockState(neighborPos);
            if(!OreGrowth.isOreGrowthBlock(neighbor.getBlock()) || neighbor.getValue(OreGrowthBlock.FACE) != side.getOpposite())
                continue;
            this.removeBlock(neighborPos, neighbor, false);
        }
    }

    @Inject(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayerGameMode;removeBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)Z",
            ordinal = 1,
            shift = At.Shift.BEFORE
        )
    )
    private void breakCrystals(BlockPos pos, CallbackInfoReturnable<Boolean> ci,
                                           @Local(ordinal = 1) BlockState state, @Local(ordinal = 0) boolean canHarvest, @Local(ordinal = 1) ItemStack heldItem){
        if(OreGrowth.isOreGrowthBlock(state.getBlock()))
            return;
        for(Direction side : Direction.values()){
            BlockPos neighborPos = pos.relative(side);
            BlockState neighbor = this.level.getBlockState(neighborPos);
            if(!OreGrowth.isOreGrowthBlock(neighbor.getBlock()) || neighbor.getValue(OreGrowthBlock.FACE) != side.getOpposite())
                continue;
            boolean crystalRemoved = this.removeBlock(neighborPos, neighbor, canHarvest);
            if(crystalRemoved && canHarvest)
                neighbor.getBlock().playerDestroy(this.level, this.player, neighborPos, neighbor, null, heldItem);
        }
    }
}
