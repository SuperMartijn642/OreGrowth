package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.oregrowth.OreGrowth;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthModelGenerator extends ModelGenerator {

    public OreGrowthModelGenerator(ResourceCache cache){
        super(OreGrowth.MODID, cache);
    }

    @Override
    public void generate(){
        this.model("item/ore_growth")
            .parent("block/ore_growth_stage_4");
    }
}
