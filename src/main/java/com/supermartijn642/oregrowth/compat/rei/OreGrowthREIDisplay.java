package com.supermartijn642.oregrowth.compat.rei;

import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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
            EntryIngredient.of(List.of(EntryStacks.of(OreGrowth.ORE_GROWTH_ITEM), EntryStacks.of(OreGrowth.COMPLETE_ORE_GROWTH_ITEM)))
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

    @Override
    public Optional<Identifier> getDisplayLocation(){
        return Optional.empty();
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer(){
        return null;
    }
}
