package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.generator.ResourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
public abstract class OreGrowthRecipeGenerator extends ResourceGenerator {

    private final Map<ResourceLocation,OreGrowthRecipe> recipes = new HashMap<>();

    public OreGrowthRecipeGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    public void recipe(String namespace, String location, Block base, int stages, double spawnChance, double growthChance, ItemStack result){
        if(stages < 1 || stages > OreGrowthBlock.MAX_STAGES)
            throw new RuntimeException("Invalid number of stages: '" + stages + "'!");
        if(spawnChance <= 0 || spawnChance > 1)
            throw new RuntimeException("Invalid spawn chance: '" + spawnChance + "'!");
        if(growthChance <= 0 || growthChance > 1)
            throw new RuntimeException("Invalid growth chance: '" + growthChance + "'!");
        if(result.isEmpty())
            throw new RuntimeException("Invalid result '" + result + "'!");

        OreGrowthRecipe recipe = new OreGrowthRecipe(new ResourceLocation(namespace, location), base, stages, spawnChance, growthChance, result);
        if(this.recipes.containsKey(recipe.getId()))
            throw new RuntimeException("Duplicate recipe for location '" + recipe.getId() + "'!");

        this.recipes.put(recipe.getId(), recipe);
        this.cache.trackToBeGeneratedResource(ResourceType.DATA, namespace, "recipes", location, ".json");
    }

    public void recipe(String location, Block base, int stages, double spawnChance, double growthChance, ItemStack result){
        this.recipe(this.modid, location, base, stages, spawnChance, growthChance, result);
    }

    public void recipe(String location, Block base, int stages, double spawnChance, double growthChance, Item result){
        this.recipe(location, base, stages, spawnChance, growthChance, result.getDefaultInstance());
    }

    @Override
    public void save(){
        for(OreGrowthRecipe recipe : this.recipes.values()){
            ResourceLocation location = recipe.getId();
            this.cache.saveJsonResource(ResourceType.DATA, recipe.toJson(), location.getNamespace(), "recipes", location.getPath() + ".json");
        }
    }
}
