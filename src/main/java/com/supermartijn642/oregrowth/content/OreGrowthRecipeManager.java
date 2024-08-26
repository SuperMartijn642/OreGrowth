package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.util.Pair;
import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthRecipeManager {

    private static RecipeManager recipeManager;
    private static boolean reload = true;
    private static Map<Block,OreGrowthRecipe> recipesByBlock = Collections.emptyMap();

    public static synchronized void reloadRecipes(RecipeManager recipeManager){
        OreGrowthRecipeManager.recipeManager = recipeManager;
        reload = true;
        recipesByBlock = Collections.emptyMap();
    }

    public static OreGrowthRecipe getRecipeFor(Block block){
        cacheRecipes();
        return recipesByBlock.get(block);
    }

    public static List<OreGrowthRecipe> getAllRecipes(){
        cacheRecipes();
        return Arrays.asList(recipesByBlock.values().toArray(OreGrowthRecipe[]::new));
    }

    private static synchronized void cacheRecipes(){
        if(reload && recipeManager != null){
            recipesByBlock = recipeManager.recipes.getOrDefault(OreGrowth.ORE_GROWTH_RECIPE_TYPE, Collections.emptyMap()).values()
                .stream()
                .map(OreGrowthRecipe.class::cast)
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .flatMap(recipe -> recipe.bases().stream().map(block -> Pair.of(block, recipe)))
                .collect(Collectors.toUnmodifiableMap(Pair::left, Pair::right, (recipe, recipe2) -> recipe));
            reload = false;
        }
    }
}
