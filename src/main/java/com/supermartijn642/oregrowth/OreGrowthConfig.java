package com.supermartijn642.oregrowth;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 10/10/2023 by SuperMartijn642
 */
public class OreGrowthConfig {

    public static final Supplier<Boolean> enableRecipes;
    public static final Supplier<Double> spawnChanceScalar;
    public static final Supplier<Double> growthChanceScalar;

    static{
        IConfigBuilder builder = ConfigBuilders.newTomlConfig(OreGrowth.MODID, null, false);

        enableRecipes = builder.comment("Should the default Ore Growth recipes be enabled?").define("enableRecipes", true);
        spawnChanceScalar = builder.comment("Global scalar for the spawn chance of ore growth recipes. For example, if set to 0.5, all crystals will spawn half as often.").define("spawnChanceScalar", 1, 0.01, 10);
        growthChanceScalar = builder.comment("Global scalar for the growth chance of ore growth recipes. For example, if set to 0.5, all crystals will be half as likely to grow.").define("growthChanceScalar", 1, 0.01, 10);

        builder.build();
    }

    public static void init(){
        // Cause this class to load
    }
}
