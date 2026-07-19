package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {

    @Unique
    private boolean isRandomlyTicking;

    @Inject(
        method = "initCache",
        at = @At("TAIL")
    )
    private void initCache(CallbackInfo ci){
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        this.isRandomlyTicking = OreGrowthRecipeManager.get(false).getRecipeFor(state.getBlock()) != null;
    }

    @Inject(
        method = "isRandomlyTicking",
        at = @At("HEAD"),
        cancellable = true
    )
    private void isRandomlyTicking(CallbackInfoReturnable<Boolean> ci){
        if(this.isRandomlyTicking)
            ci.setReturnValue(true);
    }

    @Inject(
        method = "randomTick",
        at = @At("HEAD")
    )
    private void randomTick(ServerLevel level, BlockPos pos, Random random, CallbackInfo ci){
        if(!this.isRandomlyTicking)
            return;
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        OreGrowthRecipe recipe = OreGrowthRecipeManager.get(level.isClientSide).getRecipeFor(state.getBlock());
        if(recipe != null)
            OreGrowthBlock.trySpawnOreGrowth(state, recipe, level, pos, random);
    }

    @Inject(
        method = "is(Lnet/minecraft/tags/TagKey;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    public void is(TagKey<Block> tag, CallbackInfoReturnable<Boolean> ci){
        // Intercept checks for mining tags
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        Block block = state.getBlock();
        if(OreGrowth.isOreGrowthBlock(block) && ((OreGrowthBlock)block).is(state, tag))
            ci.setReturnValue(true);
    }

    @Inject(
        method = "is(Lnet/minecraft/core/HolderSet;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    public void is(HolderSet<Block> tag, CallbackInfoReturnable<Boolean> ci){
        // Intercept checks for mining tags
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        Block block = state.getBlock();
        if(OreGrowth.isOreGrowthBlock(block) && tag instanceof HolderSet.Named && ((OreGrowthBlock)block).is(state, ((HolderSet.Named<Block>)tag).key()))
            ci.setReturnValue(true);
    }

    @Inject(
        method = "requiresCorrectToolForDrops()Z",
        at = @At("HEAD"),
        cancellable = true
    )
    public void requiresCorrectToolForDrops(CallbackInfoReturnable<Boolean> ci){
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        Block block = state.getBlock();
        if(OreGrowth.isOreGrowthBlock(block))
            ci.setReturnValue(((OreGrowthBlock)block).requiresCorrectToolForDrops(state));
    }
}
