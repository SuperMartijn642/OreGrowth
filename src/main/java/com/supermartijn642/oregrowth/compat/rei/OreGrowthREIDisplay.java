package com.supermartijn642.oregrowth.compat.rei;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

/**
 * Created 26/08/2024 by SuperMartijn642
 */
public class OreGrowthREIDisplay implements Display {

    private final OreGrowthRecipe recipe;

    public OreGrowthREIDisplay(OreGrowthRecipe recipe){
        this.recipe = recipe;
    }

    public OreGrowthRecipe getRecipe(){
        return this.recipe;
    }

    @Override
    public List<EntryIngredient> getInputEntries(){
        return List.of(
            this.recipe.bases(BuiltInRegistries.BLOCK).stream()
                .map(EntryStacks::of)
                .collect(EntryIngredient.collector()),
            EntryIngredient.of(EntryStacks.of(OreGrowth.ORE_GROWTH_ITEM))
        );
    }

    @Override
    public List<EntryIngredient> getOutputEntries(){
        return this.recipe.getRecipeViewerDrops().stream()
            .map(OreGrowthRecipe.RecipeViewerDrop::result)
            .map(EntryStacks::of)
            .map(EntryIngredient::of)
            .toList();
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier(){
        return OreGrowthREIPlugin.ORE_GROWTH_CATEGORY;
    }
}
