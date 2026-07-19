package com.supermartijn642.oregrowth.extension;

import net.minecraft.server.level.BlockDestructionProgress;

/**
 * Created 17/07/2026 by SuperMartijn642
 */
public interface BlockDestructionProgressExtension {

    BlockDestructionProgress[] getOreGrowthDestructionProgress();

    void setOreGrowthDestructionProgress(BlockDestructionProgress[] progress);
}
