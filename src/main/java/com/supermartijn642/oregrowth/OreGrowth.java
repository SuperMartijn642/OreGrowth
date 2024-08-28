package com.supermartijn642.oregrowth;

import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthDefaultRecipeCondition;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.generators.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowth implements ModInitializer {

    public static final String MODID = "oregrowth";

    public static final RecipeType<OreGrowthRecipe> ORE_GROWTH_RECIPE_TYPE = RecipeType.register("oregrowth:ore_growth");
    public static OreGrowthBlock ORE_GROWTH_BLOCK;
    public static BaseBlockItem ORE_GROWTH_ITEM;

    @Override
    public void onInitialize(){
        OreGrowthConfig.init();
        register();
        registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get(MODID);
        handler.registerRecipeSerializer("ore_growth", () -> OreGrowthRecipe.SERIALIZER);
        handler.registerBlock("ore_growth", () -> ORE_GROWTH_BLOCK = new OreGrowthBlock());
        handler.registerItem("ore_growth", () -> ORE_GROWTH_ITEM = new BaseBlockItem(ORE_GROWTH_BLOCK, ItemProperties.create().group(CreativeItemGroup.getNaturalBlocks())));
        handler.registerResourceConditionSerializer("default_recipes", OreGrowthDefaultRecipeCondition.SERIALIZER);
    }

    public static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get(MODID);
        handler.addGenerator(OreGrowthBlockStateGenerator::new);
        handler.addGenerator(OreGrowthLanguageGenerator::new);
        handler.addGenerator(OreGrowthModelGenerator::new);
        handler.addGenerator(OreGrowthOreGrowthRecipeGenerator::new);
        handler.addGenerator(OreGrowthTagGenerator::new);
    }
}
