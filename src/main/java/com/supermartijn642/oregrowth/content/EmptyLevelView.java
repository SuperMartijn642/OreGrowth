package com.supermartijn642.oregrowth.content;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

/**
 * Created 06/07/2025 by SuperMartijn642
 */
public class EmptyLevelView implements BlockAndTintGetter {

    public static final EmptyLevelView INSTANCE = new EmptyLevelView();

    private static final LevelLightEngine LIGHT_ENGINE = new LevelLightEngine(
        new LightChunkGetter() {
            @Override
            public @Nullable LightChunk getChunkForLighting(int i, int j){
                return null;
            }

            @Override
            public BlockGetter getLevel(){
                return INSTANCE;
            }
        },
        false,
        false
    );

    @Override
    public LevelLightEngine getLightEngine(){
        return LIGHT_ENGINE;
    }

    @Override
    public CardinalLighting cardinalLighting(){
        return CardinalLighting.DEFAULT;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver colorResolver){
        return 0;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos){
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos){
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos){
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight(){
        return 0;
    }

    @Override
    public int getMinY(){
        return 0;
    }
}
