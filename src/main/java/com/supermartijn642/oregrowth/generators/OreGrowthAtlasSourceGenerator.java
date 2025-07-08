package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.oregrowth.OreGrowth;

/**
 * Created 08/07/2025 by SuperMartijn642
 */
public class OreGrowthAtlasSourceGenerator extends AtlasSourceGenerator {

    public OreGrowthAtlasSourceGenerator(ResourceCache cache){
        super(OreGrowth.MODID, cache);
    }

    @Override
    public void generate(){
//        this.guiAtlas().texture(OreGrowthREIRecipeCategory.BACKGROUND); TODO
    }
}
