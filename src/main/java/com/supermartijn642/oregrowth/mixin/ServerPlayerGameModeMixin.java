package com.supermartijn642.oregrowth.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 21/07/2025 by SuperMartijn642
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    private ServerLevel level;
    @Final
    @Shadow
    protected ServerPlayer player;

    @ModifyExpressionValue(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"
        )
    )
    private boolean correctToolCheck(boolean original, BlockPos pos){
        BlockState state = this.level.getBlockState(pos);
        Block block = state.getBlock();
        if(OreGrowth.isOreGrowthBlock(block)){
            pos = pos.relative(state.getValue(BlockStateProperties.FACING));
            return this.player.hasCorrectToolForDrops(this.level.getBlockState(pos));
        }
        return original;
    }

    @Inject(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
            ordinal = 0,
            shift = At.Shift.BEFORE
        )
    )
    private void breakCrystalsCreativeMode(BlockPos pos, CallbackInfoReturnable<Boolean> ci, @Local BlockState state){
        if(OreGrowth.isOreGrowthBlock(state.getBlock()))
            return;
        //noinspection DataFlowIssue
        ServerPlayerGameMode self = (ServerPlayerGameMode)(Object)this;
        Boolean canHarvest = null;
        for(Direction side : Direction.values()){
            BlockPos neighborPos = pos.relative(side);
            BlockState neighbor = this.level.getBlockState(neighborPos);
            if(!OreGrowth.isOreGrowthBlock(neighbor.getBlock()) || neighbor.getValue(OreGrowthBlock.FACE) != side.getOpposite())
                continue;
            boolean changed = this.level.removeBlock(neighborPos, false);
            if(!changed)
                continue;
            if(self.isCreative())
                continue;
            if(canHarvest == null)
                canHarvest = this.player.hasCorrectToolForDrops(state);
            if(canHarvest)
                neighbor.getBlock().playerDestroy(this.level, this.player, neighborPos, neighbor, null, this.player.getMainHandItem());
        }
    }
}
