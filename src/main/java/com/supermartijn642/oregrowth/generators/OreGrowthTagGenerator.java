package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.oregrowth.OreGrowth;

/**
 * Created 27/08/2024 by SuperMartijn642
 */
public class OreGrowthTagGenerator extends TagGenerator {

    public OreGrowthTagGenerator(ResourceCache cache){
        super(OreGrowth.MODID, cache);
    }

    @Override
    public void generate(){
        // Allow Applied Energistics 2 growth accelerators to work on ore clusters
        this.blockTag("ae2", "growth_acceleratable");
    }
}
