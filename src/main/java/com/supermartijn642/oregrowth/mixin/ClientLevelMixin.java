package com.supermartijn642.oregrowth.mixin;

import com.google.common.collect.Sets;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.extension.BlockDestructionProgressExtension;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

/**
 * Created 17/07/2026 by SuperMartijn642
 */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Final
    @Shadow
    private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;
    @Final
    @Shadow
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow
    private void removeProgress(BlockDestructionProgress block) {
        throw new AssertionError();
    }

    @Unique
    private final BlockPos.MutableBlockPos dummyPos = new BlockPos.MutableBlockPos();

    private ClientLevelMixin(){
    }

    @Inject(
        method = "destroyBlockProgress",
        at = @At("TAIL")
    )
    private void mineAttachedCrystals(int playerId, BlockPos pos, int destroyStage, CallbackInfo ci){
        //noinspection DataFlowIssue
        ClientLevel level = (ClientLevel)(Object)this;
        BlockState state = level.getBlockState(pos);
        if(OreGrowth.isOreGrowthBlock(state.getBlock())) // Don't check for crystals on a crystal
            return;
        BlockDestructionProgress playerProgress = this.destroyingBlocks.get(playerId);
        if(playerProgress == null)
            return;
        // Add the same mining progress for any attached crystals
        BlockDestructionProgress[] crystalProgresses = ((BlockDestructionProgressExtension)playerProgress).getOreGrowthDestructionProgress();
        for(Direction side : Direction.values()){
            this.dummyPos.set(pos.getX() + side.getStepX(), pos.getY() + side.getStepY(), pos.getZ() + side.getStepZ());
            BlockState neighbor = level.getBlockState(this.dummyPos);
            if(OreGrowth.isOreGrowthBlock(neighbor.getBlock()) && neighbor.getValue(OreGrowthBlock.FACE) == side.getOpposite()){
                if(crystalProgresses == null)
                    ((BlockDestructionProgressExtension)playerProgress).setOreGrowthDestructionProgress(crystalProgresses = new BlockDestructionProgress[6]);
                BlockDestructionProgress crystalProgress = crystalProgresses[side.ordinal()];
                if(crystalProgress == null){
                    crystalProgress = crystalProgresses[side.ordinal()] = new BlockDestructionProgress(playerId, this.dummyPos.immutable());
                    this.destructionProgress.computeIfAbsent(crystalProgress.getPos().asLong(), o -> Sets.newTreeSet())
                        .add(crystalProgress);
                }
                crystalProgress.setProgress(playerProgress.getProgress());
                crystalProgress.updateTick(playerProgress.getUpdatedRenderTick());
            }else if(crystalProgresses != null && crystalProgresses[side.ordinal()] != null){ // If there is no crystal, remove existing crystal progress
                this.removeProgress(crystalProgresses[side.ordinal()]);
                crystalProgresses[side.ordinal()] = null;
            }
        }
    }

    @Inject(
        method = "removeProgress",
        at = @At("HEAD")
    )
    private void removeProgress(BlockDestructionProgress progress, CallbackInfo ci){
        // Remove crystal mining progress when base block progress is removed
        BlockDestructionProgress[] crystalProgresses = ((BlockDestructionProgressExtension)progress).getOreGrowthDestructionProgress();
        if(crystalProgresses == null)
            return;
        for(int i = 0; i < crystalProgresses.length; i++){
            BlockDestructionProgress crystalProgress = crystalProgresses[i];
            if(crystalProgress == null)
                continue;
            this.removeProgress(crystalProgress);
            crystalProgresses[i] = null;
        }
    }
}
