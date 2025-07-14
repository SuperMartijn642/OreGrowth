package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.oregrowth.OreGrowth;

/**
 * Created 26/12/2024 by SuperMartijn642
 */
public class OreGrowthItemInfoGenerator extends ItemInfoGenerator {

    public OreGrowthItemInfoGenerator(ResourceCache cache){
        super(OreGrowth.MODID, cache);
    }

    @Override
    public void generate(){
        this.simpleInfo(OreGrowth.ORE_GROWTH_ITEM, "block/ore_growth_stage_4");
        this.simpleInfo(OreGrowth.COMPLETE_ORE_GROWTH_ITEM, "block/ore_growth_stage_4");
    }
}
