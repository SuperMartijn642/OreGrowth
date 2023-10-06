package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.oregrowth.OreGrowth;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthLanguageGenerator extends LanguageGenerator {

    public OreGrowthLanguageGenerator(ResourceCache cache){
        super(OreGrowth.MODID, cache, "en_us");
    }

    @Override
    public void generate(){
        this.block(OreGrowth.ORE_GROWTH_BLOCK, "Ore Cluster");
        this.translation("oregrowth.ore_growth_block.adjusted_name", "%s Crystal");
        this.translation("oregrowth.ore_growth_block.growth_hint", "Growth: %s");
        this.translation("oregrowth.jei_category.title", "Crystal Growth");
    }
}
