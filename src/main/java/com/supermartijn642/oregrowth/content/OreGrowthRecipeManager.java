package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.util.Pair;
import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthRecipeManager {

    private static final OreGrowthRecipeManager SERVER = new OreGrowthRecipeManager(), CLIENT = new OreGrowthRecipeManager();

    public static OreGrowthRecipeManager get(boolean isClient){
        return isClient ? CLIENT : SERVER;
    }

    private RecipeManager recipeManager;
    private HolderLookup<Block> blockLookup;
    private boolean reload = true;
    private Map<Block,OreGrowthRecipe> recipesByBlock = Collections.emptyMap();

    public synchronized void reloadRecipes(RecipeManager recipeManager){
        this.recipeManager = recipeManager;
        this.blockLookup = HolderLookup.forRegistry(Registry.BLOCK);
        this.reload = true;
        this.recipesByBlock = Collections.emptyMap();
    }

    public OreGrowthRecipe getRecipeFor(Block block){
        this.cacheRecipes();
        return this.recipesByBlock.get(block);
    }

    public List<OreGrowthRecipe> getAllRecipes(){
        this.cacheRecipes();
        return Arrays.asList(this.recipesByBlock.values().toArray(OreGrowthRecipe[]::new));
    }

    private synchronized void cacheRecipes(){
        if(this.reload && this.recipeManager != null){
            this.recipesByBlock = this.recipeManager.recipes.getOrDefault(OreGrowth.ORE_GROWTH_RECIPE_TYPE, Collections.emptyMap()).values()
                .stream()
                .map(OreGrowthRecipe.class::cast)
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .flatMap(recipe -> recipe.bases(this.blockLookup).stream().map(block -> Pair.of(block, recipe)))
                .collect(Collectors.toUnmodifiableMap(Pair::left, Pair::right, (recipe, recipe2) -> recipe));
            this.reload = false;
        }
    }
}
