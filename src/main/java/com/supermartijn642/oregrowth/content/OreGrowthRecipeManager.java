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
import java.util.stream.Stream;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthRecipeManager {

    private static final OreGrowthRecipeManager SERVER = new OreGrowthRecipeManager(), CLIENT = new OreGrowthRecipeManager();

    public static OreGrowthRecipeManager get(boolean isClient){
        return isClient ? CLIENT : SERVER;
    }

    private RecipeManager recipeManager;
    private HolderLookup.RegistryLookup<Block> blockLookup;
    private boolean reload = true;
    private Map<Block,OreGrowthRecipe> recipesByBlock = Collections.emptyMap();

    public synchronized void reloadRecipes(RecipeManager recipeManager){
        this.recipeManager = recipeManager;
        this.blockLookup = BuiltInRegistries.BLOCK;
        this.reload = true;
        this.recipesByBlock = Collections.emptyMap();
        for(BlockState state : Block.BLOCK_STATE_REGISTRY)
            ((OreGrowthBlockState)state).oreGrowthInvalidate();
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
            this.cacheRecipes(
                this.recipeManager.recipes.byType(OreGrowth.ORE_GROWTH_RECIPE_TYPE)
                    .stream()
                    .sorted(Comparator.comparing(holder -> holder.id().toString()))
                    .map(RecipeHolder::value)
            );
        }
    }

    private synchronized void cacheRecipes(Stream<OreGrowthRecipe> recipes){
        ImmutableMap.Builder<Block,OreGrowthRecipe> builder = ImmutableMap.builder();
        recipes.forEach(recipe -> recipe.bases(this.blockLookup).forEach(block -> builder.put(block, recipe)));
        this.recipesByBlock = builder.buildKeepingLast();
        this.reload = false;
    }

    public synchronized void setClientRecipes(Collection<OreGrowthRecipe> recipes){
        if(this != CLIENT)
            throw new IllegalStateException("Recipes must only be set directly on the client!");
        this.cacheRecipes(recipes.stream());
    }
}
