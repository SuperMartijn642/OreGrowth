package com.supermartijn642.oregrowth.content;

import com.google.common.collect.ImmutableMap;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.extensions.OreGrowthBlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthRecipeManager {

    private static RecipeManager recipeManager;
    private static HolderLookup.RegistryLookup<Block> blockLookup;
    private static boolean reload = true;
    private static Map<Block,OreGrowthRecipe> recipesByBlock = Collections.emptyMap();

    public static synchronized void reloadRecipes(RecipeManager recipeManager){
        OreGrowthRecipeManager.recipeManager = recipeManager;
        blockLookup = BuiltInRegistries.BLOCK;
        reload = true;
        recipesByBlock = Collections.emptyMap();
        for(BlockState state : Block.BLOCK_STATE_REGISTRY)
            ((OreGrowthBlockState)state).oreGrowthInvalidate();
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
            ImmutableMap.Builder<Block,OreGrowthRecipe> builder = ImmutableMap.builder();
            recipeManager.recipes.byType(OreGrowth.ORE_GROWTH_RECIPE_TYPE)
                .stream()
                .sorted(Comparator.comparing(holder -> holder.id().toString()))
                .map(RecipeHolder::value)
                .map(OreGrowthRecipe.class::cast)
                .forEach(recipe -> recipe.bases(blockLookup).forEach(block -> builder.put(block, recipe)));
            recipesByBlock = builder.buildKeepingLast();
            reload = false;
        }
    }
}
