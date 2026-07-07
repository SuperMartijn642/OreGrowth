package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import net.minecraft.core.HolderSet;
import net.minecraft.core.TypedInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 07/07/2026 by SuperMartijn642
 */
@Mixin(TypedInstance.class)
public interface TypedInstanceMixin {

    @Inject(
        method = "is(Lnet/minecraft/tags/TagKey;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void is(TagKey<Block> tag, CallbackInfoReturnable<Boolean> ci){
        if(!(this instanceof BlockBehaviour.BlockStateBase))
            return;
        // Intercept checks for mining tags
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        Block block = state.getBlock();
        if((block == OreGrowth.ORE_GROWTH_BLOCK || block == OreGrowth.COMPLETE_ORE_GROWTH_BLOCK)
            && ((OreGrowthBlock)block).is(state, tag))
            ci.setReturnValue(true);
    }

    @Inject(
        method = "is(Lnet/minecraft/core/HolderSet;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void is(HolderSet<Block> tag, CallbackInfoReturnable<Boolean> ci){
        if(!(this instanceof BlockBehaviour.BlockStateBase))
            return;
        // Intercept checks for mining tags
        //noinspection DataFlowIssue
        BlockBehaviour.BlockStateBase state = (BlockBehaviour.BlockStateBase)(Object)this;
        Block block = state.getBlock();
        if((block == OreGrowth.ORE_GROWTH_BLOCK || block == OreGrowth.COMPLETE_ORE_GROWTH_BLOCK)
            && tag instanceof HolderSet.Named && ((OreGrowthBlock)block).is(state, ((HolderSet.Named<Block>)tag).key()))
            ci.setReturnValue(true);
    }
}
