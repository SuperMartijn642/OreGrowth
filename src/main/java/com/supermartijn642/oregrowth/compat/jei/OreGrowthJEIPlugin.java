package com.supermartijn642.oregrowth.compat.jei;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
@JeiPlugin
public class OreGrowthJEIPlugin implements IModPlugin {

    public static final RecipeType<OreGrowthRecipe> ORE_GROWTH_RECIPE_TYPE = RecipeType.create(OreGrowth.MODID, "ore_growth", OreGrowthRecipe.class);

    @Override
    public ResourceLocation getPluginUid(){
        return new ResourceLocation(OreGrowth.MODID, "ore_growth_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration){
        registration.addRecipeCategories(new OreGrowthJEIRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration){
        registration.addRecipes(ORE_GROWTH_RECIPE_TYPE, OreGrowthRecipeManager.getAllRecipes());
    }
}
