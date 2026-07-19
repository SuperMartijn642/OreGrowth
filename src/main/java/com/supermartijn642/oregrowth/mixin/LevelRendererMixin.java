package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.extension.BlockDestructionProgressExtension;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 17/07/2026 by SuperMartijn642
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
        method = "removeProgress",
        at = @At("HEAD")
    )
    private void removeProgress(BlockDestructionProgress progress, CallbackInfo ci){
        // Remove crystal mining progress when base block progress is removed
        //noinspection DataFlowIssue
        LevelRenderer levelRenderer = (LevelRenderer)(Object)this;
        BlockDestructionProgress[] crystalProgresses = ((BlockDestructionProgressExtension)progress).getOreGrowthDestructionProgress();
        if(crystalProgresses == null)
            return;
        for(int i = 0; i < crystalProgresses.length; i++){
            BlockDestructionProgress crystalProgress = crystalProgresses[i];
            if(crystalProgress == null)
                continue;
            levelRenderer.removeProgress(crystalProgress);
            crystalProgresses[i] = null;
        }
    }
}
