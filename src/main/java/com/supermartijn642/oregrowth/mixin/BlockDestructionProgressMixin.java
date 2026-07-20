package com.supermartijn642.oregrowth.mixin;

import com.supermartijn642.oregrowth.extension.BlockDestructionProgressExtension;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Created 17/07/2026 by SuperMartijn642
 */
@Mixin(BlockDestructionProgress.class)
public class BlockDestructionProgressMixin implements BlockDestructionProgressExtension {

    private BlockDestructionProgress[] progresses;

    @Override
    public BlockDestructionProgress[] getOreGrowthDestructionProgress(){
        return this.progresses;
    }

    @Override
    public void setOreGrowthDestructionProgress(BlockDestructionProgress[] progress){
        this.progresses = progress;
    }
}
