package com.supermartijn642.oregrowth.compat.rei;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.forge.REIPluginClient;

/**
 * Created 26/08/2024 by SuperMartijn642
 */
@REIPluginClient
public class OreGrowthREIPlugin implements REIClientPlugin {

    public static final CategoryIdentifier<OreGrowthREIDisplay> ORE_GROWTH_CATEGORY = CategoryIdentifier.of(OreGrowth.MODID, "ore_growth");

    @Override
    public void registerCategories(CategoryRegistry registry){
        registry.add(new OreGrowthREIRecipeCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry){
        OreGrowthRecipeManager.getAllRecipes().stream()
            .map(OreGrowthREIDisplay::new)
            .forEach(registry::add);
    }

    @Override
    public void registerDisplaySerializer(DisplaySerializerRegistry registry){
        REIClientPlugin.super.registerDisplaySerializer(registry); // TODO
    }
}
