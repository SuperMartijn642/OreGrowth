package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Shadow
    protected ClientLevel level;

    @ModifyVariable(
        method = "destroy",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            shift = At.Shift.AFTER
        ),
        ordinal = 0
    )
    private BlockState destroy(BlockState state, BlockPos pos){
        if(state.is(OreGrowth.ORE_GROWTH_BLOCK)){
            Direction facing = state.getValue(OreGrowthBlock.FACE);
            BlockState base = this.level.getBlockState(pos.relative(facing));
            if(!base.isAir())
                return base;
        }
        return state;
    }

    @ModifyVariable(
        method = "crack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/particle/TerrainParticle;<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
            shift = At.Shift.BEFORE
        ),
        ordinal = 0
    )
    private BlockState crack(BlockState state, BlockPos pos){
        if(state.is(OreGrowth.ORE_GROWTH_BLOCK)){
            Direction facing = state.getValue(OreGrowthBlock.FACE);
            BlockState base = this.level.getBlockState(pos.relative(facing));
            if(!base.isAir())
                return base;
        }
        return state;
    }
}
