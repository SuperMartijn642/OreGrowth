package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import com.supermartijn642.oregrowth.extensions.OreGrowthBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin implements OreGrowthBlockState {

    @Unique
    private boolean hasOreGrowthRecipe;
    @Unique
    private boolean recipeCached;

    @Override
    public void oreGrowthInvalidate(){
        this.recipeCached = false;
    }

    @Inject(
        method = "isRandomlyTicking",
        at = @At("HEAD"),
        cancellable = true
    )
    private void isRandomlyTicking(CallbackInfoReturnable<Boolean> ci){
        if(!this.recipeCached){
            //noinspection DataFlowIssue
            BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
            this.hasOreGrowthRecipe = OreGrowthRecipeManager.get(false).getRecipeFor(state.getBlock()) != null;
            this.recipeCached = true;
        }
        if(this.hasOreGrowthRecipe)
            ci.setReturnValue(true);
    }

    @Inject(
        method = "randomTick",
        at = @At("HEAD")
    )
    private void randomTick(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci){
        if(!this.recipeCached || !this.hasOreGrowthRecipe)
            return;
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        OreGrowthRecipe recipe = OreGrowthRecipeManager.get(level.isClientSide()).getRecipeFor(state.getBlock());
        if(recipe != null)
            OreGrowthBlock.trySpawnOreGrowth(state, recipe, level, pos, random);
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
        if(block == OreGrowth.ORE_GROWTH_BLOCK || block == OreGrowth.COMPLETE_ORE_GROWTH_BLOCK)
            ci.setReturnValue(((OreGrowthBlock)block).requiresCorrectToolForDrops(state));
    }
}
