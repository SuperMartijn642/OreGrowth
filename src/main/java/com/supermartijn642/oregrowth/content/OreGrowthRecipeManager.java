package com.supermartijn642.oregrowth.content;

import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthRecipeManager {

    private static Map<Block,OreGrowthRecipe> recipesByBlock = Collections.emptyMap();

    public static void reloadRecipes(RecipeManager recipeManager){
        recipesByBlock = recipeManager.recipes.getOrDefault(OreGrowth.ORE_GROWTH_RECIPE_TYPE, Collections.emptyMap()).values()
            .stream()
            .map(RecipeHolder::value)
            .map(OreGrowthRecipe.class::cast)
            .collect(Collectors.toUnmodifiableMap(OreGrowthRecipe::base, Function.identity(), (recipe, recipe2) -> recipe));
    }

    public static OreGrowthRecipe getRecipeFor(Block block){
        return recipesByBlock.get(block);
    }

    public static List<OreGrowthRecipe> getAllRecipes(){
        return Arrays.asList(recipesByBlock.values().toArray(OreGrowthRecipe[]::new));
    }
}
