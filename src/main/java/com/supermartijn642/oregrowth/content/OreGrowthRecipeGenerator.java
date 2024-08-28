package com.supermartijn642.oregrowth.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.supermartijn642.core.data.condition.ModLoadedResourceCondition;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.data.recipe.ConditionalRecipeSerializer;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.ResourceGenerator;
import com.supermartijn642.core.generator.ResourceType;
import com.supermartijn642.core.registry.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.*;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
public abstract class OreGrowthRecipeGenerator extends ResourceGenerator {

    private final Map<ResourceLocation,OreGrowthRecipeBuilder> recipes = new HashMap<>();

    public OreGrowthRecipeGenerator(String modid, ResourceCache cache){
        super(modid, cache);
    }

    public OreGrowthRecipeBuilder recipe(String namespace, String location, ResourceLocation base, int stages, double spawnChance, double growthChance){
        if(stages < 1 || stages > OreGrowthBlock.MAX_STAGES)
            throw new RuntimeException("Invalid number of stages: '" + stages + "'!");
        if(spawnChance <= 0 || spawnChance > 1)
            throw new RuntimeException("Invalid spawn chance: '" + spawnChance + "'!");
        if(growthChance <= 0 || growthChance > 1)
            throw new RuntimeException("Invalid growth chance: '" + growthChance + "'!");

        ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath(namespace, location);
        if(this.recipes.containsKey(identifier))
            throw new RuntimeException("Duplicate recipe for location '" + identifier + "'!");

        OreGrowthRecipeBuilder recipe = new OreGrowthRecipeBuilder(base, stages, spawnChance, growthChance);
        this.recipes.put(identifier, recipe);
        this.cache.trackToBeGeneratedResource(ResourceType.DATA, namespace, "recipe", location, ".json");
        return recipe;
    }

    public OreGrowthRecipeBuilder recipe(String location, Block base, int stages, double spawnChance, double growthChance){
        return this.recipe(this.modid, location, Registries.BLOCKS.getIdentifier(base), stages, spawnChance, growthChance);
    }

    public OreGrowthRecipeBuilder modIntegration(String modid, String base, int stages, double spawnChance, double growthChance){
        ResourceLocation baseIdentifier = base.contains(":") ? ResourceLocation.parse(base) : ResourceLocation.fromNamespaceAndPath(modid, base);
        return this.recipe(this.modid, modid + "_" + baseIdentifier.getPath() + "_growth", baseIdentifier, stages, spawnChance, growthChance)
            .defaultNamespace(modid)
            .modLoadedCondition(modid);
    }

    @Override
    public void save(){
        for(Map.Entry<ResourceLocation,OreGrowthRecipeBuilder> entry : this.recipes.entrySet()){
            OreGrowthRecipeBuilder recipe = entry.getValue();

            // Convert the recipe to json
            JsonObject json = new JsonObject();
            json.addProperty("type", "oregrowth:ore_growth");
            if(recipe.bases.size() == 1)
                json.addProperty("base", recipe.bases.stream().findAny().get());
            else{
                JsonArray bases = new JsonArray(recipe.bases.size());
                recipe.bases.forEach(bases::add);
                json.add("base", bases);
            }
            json.addProperty("stages", recipe.stages);
            json.addProperty("spawn_chance", recipe.spawnChance);
            json.addProperty("growth_chance", recipe.growthChance);

            // Drops
            JsonArray drops = new JsonArray();
            for(OreGrowthDrop drop : recipe.drops){
                JsonObject dropJson = new JsonObject();
                if(drop.minStage == drop.maxStage)
                    dropJson.addProperty("stage", drop.minStage);
                else{
                    dropJson.addProperty("min_stage", drop.minStage);
                    dropJson.addProperty("max_stage", drop.maxStage);
                }
                if(drop.chance != 1)
                    dropJson.addProperty("chance", drop.chance);
                if(drop.item != null){
                    JsonObject itemJson = new JsonObject();
                    itemJson.addProperty("id", drop.item.toString());
                    itemJson.addProperty("count", drop.count);
                    dropJson.add("item", itemJson);
                }else
                    dropJson.addProperty("loot_table", drop.lootTable.toString());
                drops.add(dropJson);
            }
            json.add("drops", drops);

            // Add conditions
            List<ResourceCondition> conditions = recipe.conditions;
            if(!conditions.isEmpty())
                json = ConditionalRecipeSerializer.wrapRecipe(json, conditions);

            ResourceLocation location = entry.getKey();
            this.cache.saveJsonResource(ResourceType.DATA, json, location.getNamespace(), "recipe", location.getPath() + ".json");
        }
    }

    public static class OreGrowthRecipeBuilder {

        private String baseNamespace;
        private final Set<String> bases = new LinkedHashSet<>();
        private final int stages;
        private final double spawnChance, growthChance;
        private final List<OreGrowthDrop> drops = new ArrayList<>();

        private final List<ResourceCondition> conditions = new ArrayList<>();

        public OreGrowthRecipeBuilder(ResourceLocation base, int stages, double spawnChance, double growthChance){
            this.bases.add(base.toString());
            this.stages = stages;
            this.spawnChance = spawnChance;
            this.growthChance = growthChance;
        }

        public OreGrowthRecipeBuilder defaultNamespace(String namespace){
            this.baseNamespace = namespace;
            return this;
        }

        public OreGrowthRecipeBuilder baseBlock(ResourceLocation block){
            if(!this.bases.add(block.toString()))
                throw new IllegalStateException("Duplicate base block '" + block + "'!");
            return this;
        }

        public OreGrowthRecipeBuilder baseBlock(Block block){
            return this.baseBlock(Registries.BLOCKS.getIdentifier(block));
        }

        public OreGrowthRecipeBuilder baseBlock(String namespace, String identifier){
            return this.baseBlock(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
        }

        public OreGrowthRecipeBuilder baseBlock(String block){
            return this.baseBlock(this.parseIdentifier(block));
        }

        public OreGrowthRecipeBuilder baseTag(ResourceLocation tag){
            if(!this.bases.add("#" + tag))
                throw new IllegalStateException("Duplicate base tag '" + tag + "'!");
            return this;
        }

        public OreGrowthRecipeBuilder baseTag(TagKey<Block> tag){
            return this.baseTag(tag.location());
        }

        public OreGrowthRecipeBuilder baseTag(String namespace, String identifier){
            return this.baseTag(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
        }

        public OreGrowthRecipeBuilder baseTag(String tag){
            return this.baseTag(this.parseIdentifier(tag));
        }

        public OreGrowthRecipeBuilder itemDrop(int minStage, int maxStage, double chance, ResourceLocation item, int count){
            if(minStage <= 0 || minStage > OreGrowthBlock.MAX_STAGES)
                throw new IllegalArgumentException("Minimum stage must be between 1 and " + OreGrowthBlock.MAX_STAGES + ", not '" + minStage + "'!");
            if(maxStage <= 0 || maxStage > OreGrowthBlock.MAX_STAGES)
                throw new IllegalArgumentException("Maximum stage must be between 1 and " + OreGrowthBlock.MAX_STAGES + ", not '" + maxStage + "'!");
            if(minStage > maxStage)
                throw new IllegalArgumentException("Minimum stage must be less than maximum stage!");
            if(chance <= 0 || chance > 1)
                throw new IllegalArgumentException("Chance must be between 0 and 1, not '" + chance + "'!");
            if(count <= 0)
                throw new RuntimeException("Count must be at least 1, not '" + count + "'!");
            this.drops.add(new OreGrowthDrop(minStage, maxStage, chance, item, count, null));
            return this;
        }

        public OreGrowthRecipeBuilder itemDrop(int minStage, int maxStage, double chance, ItemLike item, int count){
            return this.itemDrop(minStage, maxStage, chance, Registries.ITEMS.getIdentifier(item.asItem()), count);
        }

        public OreGrowthRecipeBuilder itemDrop(int minStage, int maxStage, double chance, String namespace, String identifier, int count){
            return this.itemDrop(minStage, maxStage, chance, ResourceLocation.fromNamespaceAndPath(namespace, identifier), count);
        }

        public OreGrowthRecipeBuilder itemDrop(int minStage, int maxStage, double chance, String item, int count){
            return this.itemDrop(minStage, maxStage, chance, this.parseIdentifier(item), count);
        }

        public OreGrowthRecipeBuilder itemDrop(int minStage, int maxStage, double chance, ResourceLocation item){
            return this.itemDrop(minStage, maxStage, chance, item, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int minStage, int maxStage, double chance, ItemLike item){
            return this.itemDrop(minStage, maxStage, chance, item, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int minStage, int maxStage, double chance, String namespace, String identifier){
            return this.itemDrop(minStage, maxStage, chance, namespace, identifier, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int minStage, int maxStage, double chance, String item){
            return this.itemDrop(minStage, maxStage, chance, item, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, double chance, ResourceLocation item, int count){
            return this.itemDrop(stage, stage, chance, item, count);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, double chance, ItemLike item, int count){
            return this.itemDrop(stage, stage, chance, item, count);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, double chance, String namespace, String identifier, int count){
            return this.itemDrop(stage, stage, chance, namespace, identifier, count);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, double chance, String item, int count){
            return this.itemDrop(stage, stage, chance, item, count);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, double chance, ResourceLocation item){
            return this.itemDrop(stage, chance, item, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, double chance, ItemLike item){
            return this.itemDrop(stage, chance, item, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, double chance, String namespace, String identifier){
            return this.itemDrop(stage, chance, namespace, identifier, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, double chance, String item){
            return this.itemDrop(stage, chance, item, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, ResourceLocation item, int count){
            return this.itemDrop(stage, 1, item, count);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, ItemLike item, int count){
            return this.itemDrop(stage, 1, item, count);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, String namespace, String identifier, int count){
            return this.itemDrop(stage, 1, namespace, identifier, count);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, String item, int count){
            return this.itemDrop(stage, 1, item, count);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, ResourceLocation item){
            return this.itemDrop(stage, item, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, ItemLike item){
            return this.itemDrop(stage, item, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, String namespace, String identifier){
            return this.itemDrop(stage, namespace, identifier, 1);
        }

        public OreGrowthRecipeBuilder itemDrop(int stage, String item){
            return this.itemDrop(stage, item, 1);
        }

        public OreGrowthRecipeBuilder lootTableDrop(int minStage, int maxStage, double chance, ResourceLocation lootTable){
            if(minStage <= 0 || minStage > OreGrowthBlock.MAX_STAGES)
                throw new IllegalArgumentException("Minimum stage must be between 1 and " + OreGrowthBlock.MAX_STAGES + ", not '" + minStage + "'!");
            if(maxStage <= 0 || maxStage > OreGrowthBlock.MAX_STAGES)
                throw new IllegalArgumentException("Maximum stage must be between 1 and " + OreGrowthBlock.MAX_STAGES + ", not '" + maxStage + "'!");
            if(minStage > maxStage)
                throw new IllegalArgumentException("Minimum stage must be less than maximum stage!");
            if(chance <= 0 || chance > 1)
                throw new IllegalArgumentException("Chance must be between 0 and 1, not '" + chance + "'!");
            this.drops.add(new OreGrowthDrop(minStage, maxStage, chance, null, 0, lootTable));
            return this;
        }

        public OreGrowthRecipeBuilder lootTableDrop(int stage, double chance, ResourceLocation lootTable){
            return this.lootTableDrop(stage, stage, chance, lootTable);
        }

        public OreGrowthRecipeBuilder lootTableDrop(int stage, ResourceLocation lootTable){
            return this.lootTableDrop(stage, 1, lootTable);
        }

        public OreGrowthRecipeBuilder condition(ResourceCondition condition){
            this.conditions.add(condition);
            return this;
        }

        public OreGrowthRecipeBuilder modLoadedCondition(String modid){
            return this.condition(new ModLoadedResourceCondition(modid));
        }

        private ResourceLocation parseIdentifier(String identifier){
            if(this.baseNamespace == null)
                return ResourceLocation.parse(identifier);
            int separatorIndex = identifier.indexOf(':');
            if(separatorIndex < 0)
                return ResourceLocation.fromNamespaceAndPath(this.baseNamespace, identifier);
            return ResourceLocation.fromNamespaceAndPath(identifier.substring(0, separatorIndex), identifier.substring(separatorIndex + 1));
        }
    }

    private record OreGrowthDrop(int minStage, int maxStage, double chance, ResourceLocation item, int count,
                                 ResourceLocation lootTable) {
    }
}
