package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
@Mixin(Block.class)
public class BlockMixin {

    @Inject(
        method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void getDrops(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, CallbackInfoReturnable<List<ItemStack>> ci) {
        if(state.is(OreGrowth.ORE_GROWTH_BLOCK))
            ci.setReturnValue(OreGrowth.ORE_GROWTH_BLOCK.getDrops(state, level, pos));
    }

    @Inject(
        method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void getDrops(BlockState state, ServerLevel level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> ci) {
        if(state.is(OreGrowth.ORE_GROWTH_BLOCK))
            ci.setReturnValue(OreGrowth.ORE_GROWTH_BLOCK.getDrops(state, level, pos));
    }
}
