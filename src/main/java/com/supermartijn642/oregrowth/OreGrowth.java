package com.supermartijn642.oregrowth;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.oregrowth.compat.OreGrowthTOPPlugin;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthDefaultRecipeCondition;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.generators.OreGrowthBlockStateGenerator;
import com.supermartijn642.oregrowth.generators.OreGrowthLanguageGenerator;
import com.supermartijn642.oregrowth.generators.OreGrowthModelGenerator;
import com.supermartijn642.oregrowth.generators.OreGrowthOreGrowthRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.function.Consumer;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
@Mod(OreGrowth.MODID)
public class OreGrowth {

    public static final String MODID = "oregrowth";

    public static RecipeType<OreGrowthRecipe> ORE_GROWTH_RECIPE_TYPE;
    public static OreGrowthBlock ORE_GROWTH_BLOCK;
    public static BaseBlockItem ORE_GROWTH_ITEM;

    public OreGrowth(IEventBus eventBus){
        OreGrowthConfig.init();
        register();
        if(CommonUtils.getEnvironmentSide().isClient())
            OreGrowthClient.initializeClient();
        registerGenerators();

        // The One Probe integration
        if(CommonUtils.isModLoaded("theoneprobe"))
            eventBus.addListener((Consumer<InterModEnqueueEvent>)event -> InterModComms.sendTo("theoneprobe", "getTheOneProbe", OreGrowthTOPPlugin::new));
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get(MODID);
        handler.registerRecipeSerializer("ore_growth", () -> OreGrowthRecipe.SERIALIZER);
        handler.registerBlock("ore_growth", () -> ORE_GROWTH_BLOCK = new OreGrowthBlock());
        handler.registerItem("ore_growth", () -> ORE_GROWTH_ITEM = new BaseBlockItem(ORE_GROWTH_BLOCK, ItemProperties.create().group(CreativeItemGroup.getNaturalBlocks())));
        handler.registerRecipeType("ore_growth", () -> ORE_GROWTH_RECIPE_TYPE = RecipeType.simple(new ResourceLocation(MODID, "ore_growth")));
        handler.registerResourceConditionSerializer("default_recipes", OreGrowthDefaultRecipeCondition.SERIALIZER);
    }

    public static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get(MODID);
        handler.addGenerator(OreGrowthBlockStateGenerator::new);
        handler.addGenerator(OreGrowthLanguageGenerator::new);
        handler.addGenerator(OreGrowthModelGenerator::new);
        handler.addGenerator(OreGrowthOreGrowthRecipeGenerator::new);
    }
}
