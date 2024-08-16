package com.supermartijn642.oregrowth.content;

import com.google.gson.JsonObject;
import com.supermartijn642.core.data.condition.ModLoadedResourceCondition;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.generator.ResourceType;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
public abstract class OreGrowthRecipeGenerator extends ResourceGenerator {

    private final Map<ResourceLocation,OreGrowthRecipeBuilder> recipes = new HashMap<>();

    public OreGrowthRecipeGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    public OreGrowthRecipeBuilder recipe(String namespace, String location, ResourceLocation base, int stages, double spawnChance, double growthChance, ResourceLocation resultItem, int resultCount){
        if(stages < 1 || stages > OreGrowthBlock.MAX_STAGES)
            throw new RuntimeException("Invalid number of stages: '" + stages + "'!");
        if(spawnChance <= 0 || spawnChance > 1)
            throw new RuntimeException("Invalid spawn chance: '" + spawnChance + "'!");
        if(growthChance <= 0 || growthChance > 1)
            throw new RuntimeException("Invalid growth chance: '" + growthChance + "'!");
        if(resultCount <= 0)
            throw new RuntimeException("Invalid result count '" + resultCount + "'!");

        ResourceLocation identifier = new ResourceLocation(namespace, location);
        if(this.recipes.containsKey(identifier))
            throw new RuntimeException("Duplicate recipe for location '" + identifier + "'!");

        OreGrowthRecipeBuilder recipe = new OreGrowthRecipeBuilder(base, stages, spawnChance, growthChance, resultItem, resultCount);
        this.recipes.put(identifier, recipe);
        this.cache.trackToBeGeneratedResource(ResourceType.DATA, namespace, "recipes", location, ".json");
        return recipe;
    }

    public OreGrowthRecipeBuilder recipe(String location, Block base, int stages, double spawnChance, double growthChance, Item result, int resultCount){
        return this.recipe(this.modid, location, Registries.BLOCKS.getIdentifier(base), stages, spawnChance, growthChance, Registries.ITEMS.getIdentifier(result), resultCount);
    }

    public OreGrowthRecipeBuilder recipe(String location, Block base, int stages, double spawnChance, double growthChance, Item result){
        return this.recipe(location, base, stages, spawnChance, growthChance, result, 1);
    }

    public OreGrowthRecipeBuilder modIntegration(String modid, String base, int stages, double spawnChance, double growthChance, ResourceLocation result, int resultCount){
        ResourceLocation baseIdentifier = base.contains(":") ? new ResourceLocation(base) : new ResourceLocation(modid, base);
        return this.recipe(this.modid, modid + "_" + baseIdentifier.getPath() + "_growth", baseIdentifier, stages, spawnChance, growthChance, result, resultCount)
            .modLoadedCondition(modid);
    }

    public OreGrowthRecipeBuilder modIntegration(String modid, String base, int stages, double spawnChance, double growthChance, String result, int resultCount){
        ResourceLocation resultIdentifier = result.contains(":") ? new ResourceLocation(result) : new ResourceLocation(modid, result);
        return this.modIntegration(modid, base, stages, spawnChance, growthChance, resultIdentifier, resultCount);
    }

    public OreGrowthRecipeBuilder modIntegration(String modid, String base, int stages, double spawnChance, double growthChance, String result){
        return this.modIntegration(modid, base, stages, spawnChance, growthChance, result, 1);
    }

    public OreGrowthRecipeBuilder modIntegration(String modid, String base, int stages, double spawnChance, double growthChance, Item result){
        return this.modIntegration(modid, base, stages, spawnChance, growthChance, Registries.ITEMS.getIdentifier(result), 1);
    }

    @Override
    public void save(){
        for(Map.Entry<ResourceLocation,OreGrowthRecipeBuilder> entry : this.recipes.entrySet()){
            OreGrowthRecipeBuilder recipe = entry.getValue();

            // Convert the recipe to json
            JsonObject json = new JsonObject();
            json.addProperty("type", "oregrowth:ore_growth");
            json.addProperty("base", recipe.base.toString());
            json.addProperty("stages", recipe.stages);
            json.addProperty("spawn_chance", recipe.spawnChance);
            json.addProperty("growth_chance", recipe.growthChance);
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("id", recipe.output.toString());
            itemJson.addProperty("count", recipe.outputCount);
            json.add("result", itemJson);

            // Add conditions
            List<ResourceCondition> conditions = recipe.conditions;
            if(!conditions.isEmpty())
                json = ConditionalRecipeSerializer.wrapRecipe(json, conditions);

            ResourceLocation location = entry.getKey();
            this.cache.saveJsonResource(ResourceType.DATA, json, location.getNamespace(), "recipes", location.getPath() + ".json");
        }
    }

    public static class OreGrowthRecipeBuilder {

        private final ResourceLocation base;
        private final int stages;
        private final double spawnChance, growthChance;
        private final ResourceLocation output;
        private final int outputCount;

        private final ArrayList<ResourceCondition> conditions = new ArrayList<>();

        public OreGrowthRecipeBuilder(ResourceLocation base, int stages, double spawnChance, double growthChance, ResourceLocation output, int outputCount){
            this.base = base;
            this.stages = stages;
            this.spawnChance = spawnChance;
            this.growthChance = growthChance;
            this.output = output;
            this.outputCount = outputCount;
        }

        public OreGrowthRecipeBuilder condition(ResourceCondition condition){
            this.conditions.add(condition);
            return this;
        }

        public OreGrowthRecipeBuilder modLoadedCondition(String modid){
            return this.condition(new ModLoadedResourceCondition(modid));
        }
    }
}
