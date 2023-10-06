package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {

    @Inject(
        method = "isRandomlyTicking",
        at = @At("HEAD"),
        cancellable = true
    )
    private void initCache(CallbackInfoReturnable<Boolean> ci){
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        if(OreGrowthRecipeManager.getRecipeFor(state.getBlock()) != null)
            ci.setReturnValue(true);
    }

    @Inject(
        method = "randomTick",
        at = @At("HEAD")
    )
    private void randomTick(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci){
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        OreGrowthRecipe recipe = OreGrowthRecipeManager.getRecipeFor(state.getBlock());
        if(recipe != null)
            OreGrowthBlock.trySpawnOreGrowth(recipe, level, pos, random);
    }
}
