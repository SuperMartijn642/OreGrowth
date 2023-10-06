package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import net.minecraft.core.Direction;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthBlockStateGenerator extends BlockStateGenerator {

    public OreGrowthBlockStateGenerator(ResourceCache cache){
        super(OreGrowth.MODID, cache);
    }

    @Override
    public void generate(){
        this.blockState(OreGrowth.ORE_GROWTH_BLOCK)
            .variantsForAllExcept((state, variant) -> {
                int stage = state.get(OreGrowthBlock.STAGE);
                Direction facing = state.get(OreGrowthBlock.FACE);
                int xRotation = facing == Direction.DOWN ? 0 : facing == Direction.UP ? 180 : 90;
                int yRotation = facing.getAxis() == Direction.Axis.Y ? 0 : (int)facing.toYRot();
                variant.model("block/ore_growth_stage_" + stage, xRotation, yRotation);
            }, OreGrowthBlock.WATERLOGGED);
    }
}
