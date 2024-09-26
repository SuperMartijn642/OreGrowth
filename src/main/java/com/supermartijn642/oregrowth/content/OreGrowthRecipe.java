package com.supermartijn642.oregrowth.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.registry.RegistryUtil;
import com.supermartijn642.core.util.Either;
import com.supermartijn642.oregrowth.LootTableHelper;
import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthRecipe implements Recipe<Container> {

    public static final RecipeSerializer<OreGrowthRecipe> SERIALIZER = new Serializer();

    private final ResourceLocation identifier;
    private final List<Either<Block,TagKey<Block>>> bases;
    private List<Block> resolvedBases;
    private final int stages;
    private final double spawnChance, growthChance;
    private final List<OreGrowthDrop> drops;
    private List<RecipeViewerDrop> resolvedDrops; // Used to visualize drops in recipe viewer mods

    private OreGrowthRecipe(ResourceLocation identifier, List<Either<Block,TagKey<Block>>> bases, int stages, double spawnChance, double growthChance, List<OreGrowthDrop> drops){
        this.identifier = identifier;
        this.bases = Collections.unmodifiableList(bases);
        this.stages = stages;
        this.spawnChance = spawnChance;
        this.growthChance = growthChance;
        this.drops = Collections.unmodifiableList(drops);
    }

    public List<Block> bases(){
        if(this.resolvedBases == null){
            // Any time tags get reloaded, recipes *should* also be reloaded
            // Thus, this instance is only valid as long as the tag is valid, and we can cache its contents
            this.resolvedBases = this.bases.stream().flatMap(base -> base.isLeft() ?
                Stream.of(base.left()) :
                Registry.BLOCK.getTag(base.right()).map(tag -> tag.stream().map(Holder::value)).orElseGet(Stream::of)
            ).toList();
        }
        return this.resolvedBases;
    }

    public int stages(){
        return this.stages;
    }

    public double spawnChance(){
        return this.spawnChance;
    }

    public double growthChance(){
        return this.growthChance;
    }

    public List<ItemStack> generateDrops(BlockState base, int stage, LootContext context){
        List<ItemStack> drops = new ArrayList<>();
        for(OreGrowthDrop drop : this.drops){
            if(drop.minStage > stage || drop.maxStage < stage)
                continue;
            if(drop.chance < 1 && context.getLevel().getRandom().nextDouble() > drop.chance)
                continue;
            if(drop.result.isLeft())
                drops.add(drop.result.left().copy());
            else
                context.getLevel().getServer().getLootTables().get(drop.result.right()).getRandomItems(context, drops::add);
        }
        return drops;
    }

    /**
     * ((minStage, maxStage), chance, item stack)
     */
    public List<RecipeViewerDrop> getRecipeViewerDrops(){
        if(this.resolvedDrops == null){
            if(!CommonUtils.getEnvironmentSide().isClient() && ClientUtils.getMinecraft().getSingleplayerServer() == null)
                throw new IllegalStateException("Drops should have already been resolved when received from the server!");
            this.resolveDrops(CommonUtils.getServer().getLootTables()::get);
        }
        return this.resolvedDrops;
    }

    public void resolveDrops(Function<ResourceLocation,LootTable> lookup){
        if(this.resolvedDrops == null){
            this.resolvedDrops = this.drops.stream()
                .flatMap(drop -> drop.result.flatMap(
                    stack -> Stream.of(new RecipeViewerDrop(drop.minStage, drop.maxStage, drop.chance, stack, List.of())),
                    loot -> LootTableHelper.entriesInTable(loot, lookup).stream()
                        .map(entry -> new RecipeViewerDrop(drop.minStage, drop.maxStage, drop.chance * entry.chance(), entry.stack(), entry.conditions().stream().map(LootTableHelper.LootEntryConditions::toComponents).flatMap(List::stream).toList()))
                ))
                .sorted(Comparator.comparing(RecipeViewerDrop::maxStage, Comparator.reverseOrder()).thenComparing(RecipeViewerDrop::minStage, Comparator.reverseOrder()).thenComparing(RecipeViewerDrop::chance, Comparator.reverseOrder()))
                .toList();
        }
    }

    public record RecipeViewerDrop(int minStage, int maxStage, double chance, ItemStack result,
                                   List<? extends Component> tooltip) {
    }

    @Override
    public boolean matches(Container container, Level level){
        return false;
    }

    @Override
    public ItemStack assemble(Container container){
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height){
        return false;
    }

    @Override
    public ItemStack getResultItem(){
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId(){
        return this.identifier;
    }

    @Override
    public RecipeSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType(){
        return OreGrowth.ORE_GROWTH_RECIPE_TYPE;
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        // type
        json.addProperty("type", "oregrowth:ore_growth");
        // content
        if(this.bases.size() == 1)
            json.addProperty("base", this.bases.get(0).<String>flatMap(
                block -> Registries.BLOCKS.getIdentifier(block).toString(),
                tag -> "#" + tag.location()
            ));
        else{
            JsonArray bases = new JsonArray(this.bases.size());
            this.bases.stream().map(base -> base.<String>flatMap(
                block -> Registries.BLOCKS.getIdentifier(block).toString(),
                tag -> "#" + tag.location()
            )).forEach(bases::add);
            json.add("base", bases);
        }
        json.addProperty("stages", this.stages);
        json.addProperty("spawn_chance", this.spawnChance);
        json.addProperty("growth_chance", this.growthChance);
        JsonArray dropsArray = new JsonArray(this.drops.size());
        this.drops.forEach(drop -> dropsArray.add(drop.toJson()));
        json.add("drops", dropsArray);
        return json;
    }

    public static OreGrowthRecipe fromJson(ResourceLocation identifier, JsonObject json){
        // base
        if(!json.has("base") || !((json.get("base").isJsonPrimitive() && json.getAsJsonPrimitive("base").isString()) || json.get("base").isJsonArray()))
            throw new JsonParseException("Recipe must have array property 'base'!");
        JsonArray basesJson;
        if(json.get("base").isJsonPrimitive()){
            basesJson = new JsonArray(1);
            basesJson.add(json.get("base").getAsString());
        }else
            basesJson = json.getAsJsonArray("base");
        Set<Either<Block,TagKey<Block>>> bases = new HashSet<>(basesJson.size());
        for(JsonElement baseJson : basesJson){
            if(!baseJson.isJsonPrimitive() || !baseJson.getAsJsonPrimitive().isString())
                throw new JsonParseException("Property 'base' must only contain strings!");
            String baseIdentifier = baseJson.getAsString();
            if(baseIdentifier.charAt(0) == '#'){
                baseIdentifier = baseIdentifier.substring(1);
                if(!RegistryUtil.isValidIdentifier(baseIdentifier))
                    throw new JsonParseException("Property 'base' must be a valid identifier, not '" + baseIdentifier + "'!");
                bases.add(Either.right(TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(baseIdentifier))));
            }else{
                if(!RegistryUtil.isValidIdentifier(baseIdentifier))
                    throw new JsonParseException("Property 'base' must be a valid identifier, not '" + baseIdentifier + "'!");
                if(!Registries.BLOCKS.hasIdentifier(new ResourceLocation(baseIdentifier)))
                    throw new JsonParseException("Unknown base block '" + baseIdentifier + "'!");
                Block block = Registries.BLOCKS.getValue(new ResourceLocation(baseIdentifier));
                if(block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR)
                    throw new JsonParseException("Got AIR block for base identifier '" + baseIdentifier + "'!");
                bases.add(Either.left(block));
            }
        }
        // stages
        if(!json.has("stages") || !json.get("stages").isJsonPrimitive() || !json.getAsJsonPrimitive("stages").isNumber())
            throw new JsonParseException("Recipe must have int property 'stages'!");
        int stages = json.get("stages").getAsInt();
        if(stages < 1 || stages > OreGrowthBlock.MAX_STAGES)
            throw new JsonParseException("Invalid number of stages: '" + stages + "'!");
        // spawn_chance
        if(!json.has("spawn_chance") || !json.get("spawn_chance").isJsonPrimitive() || !json.getAsJsonPrimitive("spawn_chance").isNumber())
            throw new JsonParseException("Recipe must have number property 'spawn_chance'!");
        double spawnChance = json.get("spawn_chance").getAsDouble();
        if(spawnChance <= 0 || spawnChance > 1)
            throw new JsonParseException("Invalid spawn chance: '" + spawnChance + "'!");
        // growth_chance
        if(!json.has("growth_chance") || !json.get("growth_chance").isJsonPrimitive() || !json.getAsJsonPrimitive("growth_chance").isNumber())
            throw new JsonParseException("Recipe must have number property 'growth_chance'!");
        double growthChance = json.get("growth_chance").getAsDouble();
        if(growthChance <= 0 || growthChance > 1)
            throw new JsonParseException("Invalid growth chance: '" + growthChance + "'!");
        // drops
        List<OreGrowthDrop> drops = new ArrayList<>();
        if(json.has("result")){ // legacy recipes
            if(!json.get("result").isJsonObject())
                throw new JsonParseException("Property 'result' must be an object!");
            JsonObject resultJson = json.getAsJsonObject("result");
            if(!resultJson.has("item") || !resultJson.get("item").isJsonPrimitive() || !resultJson.getAsJsonPrimitive("item").isString()
                || !resultJson.has("count") || !resultJson.get("count").isJsonPrimitive() || !resultJson.getAsJsonPrimitive("count").isNumber())
                throw new JsonParseException("Property 'result' must have string property 'item' and int property 'count'!");
            if(!RegistryUtil.isValidIdentifier(resultJson.get("item").getAsString()))
                throw new JsonParseException("Drop property 'item' must be a valid identifier, not '" + resultJson.get("item").getAsString() + "'!");
            ResourceLocation id = new ResourceLocation(resultJson.get("item").getAsString());
            if(!Registries.ITEMS.hasIdentifier(id))
                throw new JsonParseException("Unknown item '" + id + "'!");
            Item item = Registries.ITEMS.getValue(id);
            int count = resultJson.get("count").getAsInt();
            if(count < 1)
                throw new JsonParseException("Drop property 'count' must be a positive integer!");
            drops.add(new OreGrowthDrop(stages, stages, 1, Either.left(new ItemStack(item, count))));
        }else{
            if(!json.has("drops") || !json.get("drops").isJsonArray())
                throw new JsonParseException("Recipe must have array property 'drops'!");
            JsonArray dropsArray = json.getAsJsonArray("drops");
            //noinspection SizeReplaceableByIsEmpty
            if(dropsArray.size() == 0)
                throw new JsonParseException("Property 'drops' must have at least one entry!");
            for(JsonElement el : dropsArray){
                if(!el.isJsonObject())
                    throw new JsonParseException("Drop property 'drops' must only contain objects!");
                drops.add(OreGrowthDrop.fromJson(el.getAsJsonObject()));
            }
        }
        return new OreGrowthRecipe(identifier, new ArrayList<>(bases), stages, spawnChance, growthChance, drops);
    }

    public record OreGrowthDrop(int minStage, int maxStage, double chance, Either<ItemStack,ResourceLocation> result) {

        public JsonObject toJson(){
            JsonObject json = new JsonObject();
            if(this.minStage == this.maxStage)
                json.addProperty("stage", this.minStage);
            else{
                json.addProperty("min_stage", this.minStage);
                json.addProperty("max_stage", this.maxStage);
            }
            json.addProperty("chance", this.chance);
            this.result.ifLeft(stack -> {
                JsonObject resultJson = new JsonObject();
                resultJson.addProperty("id", Registries.ITEMS.getIdentifier(stack.getItem()).toString());
                resultJson.addProperty("count", stack.getCount());
                json.add("item", resultJson);
            });
            this.result.ifRight(lootTable -> json.addProperty("loot_table", lootTable.toString()));
            return json;
        }

        public static OreGrowthDrop fromJson(JsonObject json){
            // (min_stage && max_stage) || stage
            if((!json.has("min_stage") || !json.has("max_stage")) && !json.has("stage"))
                throw new JsonParseException("Drop must either have int properties 'min_stage' and 'max_stage', or have int property 'stage'!");
            if((json.has("min_stage") || json.has("max_stage")) && json.has("stage"))
                throw new JsonParseException("Drop must have int properties 'min_stage' and 'max_stage', or int property 'stage', not both!");
            int minStage, maxStage;
            if(json.has("stage")){
                // stage
                if(!json.get("stage").isJsonPrimitive() || !json.getAsJsonPrimitive("stage").isNumber())
                    throw new JsonParseException("Drop property 'stage' must be an integer!");
                int stage = json.get("stage").getAsInt();
                if(stage < 1 || stage > OreGrowthBlock.MAX_STAGES)
                    throw new JsonParseException("Drop property 'stage' must be between 1 and " + OreGrowthBlock.MAX_STAGES + "!");
                minStage = maxStage = stage;
            }else{
                // min_stage
                if(!json.has("min_stage") || !json.get("min_stage").isJsonPrimitive() || !json.getAsJsonPrimitive("min_stage").isNumber())
                    throw new JsonParseException("Drop must have int property 'min_stage'!");
                minStage = json.get("min_stage").getAsInt();
                if(minStage < 1 || minStage > OreGrowthBlock.MAX_STAGES)
                    throw new JsonParseException("Drop property 'min_stage' must be between 1 and " + OreGrowthBlock.MAX_STAGES + "!");
                // max_stage
                if(!json.has("max_stage") || !json.get("max_stage").isJsonPrimitive() || !json.getAsJsonPrimitive("max_stage").isNumber())
                    throw new JsonParseException("Drop must have int property 'max_stage'!");
                maxStage = json.get("max_stage").getAsInt();
                if(maxStage < 1 || maxStage > OreGrowthBlock.MAX_STAGES)
                    throw new JsonParseException("Drop property 'maxStage' must be between 1 and " + OreGrowthBlock.MAX_STAGES + "!");
                if(minStage > maxStage)
                    throw new JsonParseException("Drop property 'min_stage' must be less than 'max_stage'!");

            }
            // chance
            if(json.has("chance") && (!json.get("chance").isJsonPrimitive() || !json.getAsJsonPrimitive("chance").isNumber()))
                throw new JsonParseException("Drop property 'chance' must be a number!");
            double chance = json.has("chance") ? json.get("chance").getAsDouble() : 1;
            if(chance < 0 || chance > 1)
                throw new JsonParseException("Drop property 'chance' must be between 0 and 1!");
            // result
            if(!json.has("item") && !json.has("loot_table"))
                throw new JsonParseException("Drop must have either object property 'item' or string property 'loot_table'!");
            if(json.has("item") && json.has("loot_table"))
                throw new JsonParseException("Drop can only have either 'item' or 'loot_table', not both!");
            Either<ItemStack,ResourceLocation> result;
            if(json.has("item")){
                if(!json.get("item").isJsonObject())
                    throw new JsonParseException("Drop property 'item' must be an object!");
                JsonObject resultJson = json.getAsJsonObject("item");
                if(!resultJson.has("id") || !resultJson.get("id").isJsonPrimitive() || !resultJson.getAsJsonPrimitive("id").isString()
                    || !resultJson.has("count") || !resultJson.get("count").isJsonPrimitive() || !resultJson.getAsJsonPrimitive("count").isNumber())
                    throw new JsonParseException("Drop property 'item' must have string property 'id' and int property 'count'!");
                if(!RegistryUtil.isValidIdentifier(resultJson.get("id").getAsString()))
                    throw new JsonParseException("Drop property 'id' must be a valid identifier, not '" + resultJson.get("id").getAsString() + "'!");
                ResourceLocation id = new ResourceLocation(resultJson.get("id").getAsString());
                if(!Registries.ITEMS.hasIdentifier(id))
                    throw new JsonParseException("Unknown item '" + id + "'!");
                Item item = Registries.ITEMS.getValue(id);
                int count = resultJson.get("count").getAsInt();
                if(count < 1)
                    throw new JsonParseException("Drop property 'count' must be a positive integer!");
                result = Either.left(new ItemStack(item, count));
            }else{
                if(!json.get("loot_table").isJsonPrimitive() || !json.getAsJsonPrimitive("loot_table").isString())
                    throw new JsonParseException("Drop property 'loot_table' must be a string!");
                if(!RegistryUtil.isValidIdentifier(json.get("loot_table").getAsString()))
                    throw new JsonParseException("Drop property 'loot_table' must be a valid identifier, not '" + json.get("loot_table").getAsString() + "'!");
                result = Either.right(new ResourceLocation(json.get("loot_table").getAsString()));
            }
            return new OreGrowthDrop(minStage, maxStage, chance, result);
        }
    }

    private static class Serializer implements RecipeSerializer<OreGrowthRecipe> {

        @Override
        public OreGrowthRecipe fromJson(ResourceLocation identifier, JsonObject json){
            return OreGrowthRecipe.fromJson(identifier, json);
        }

        @Override
        public OreGrowthRecipe fromNetwork(ResourceLocation identifier, FriendlyByteBuf buffer){
            int baseCount = buffer.readInt();
            List<Either<Block,TagKey<Block>>> bases = new ArrayList<>(baseCount);
            for(int i = 0; i < baseCount; i++){
                boolean isBlock = buffer.readBoolean();
                ResourceLocation baseIdentifier = buffer.readResourceLocation();
                if(isBlock){
                    if(!Registries.BLOCKS.hasIdentifier(baseIdentifier))
                        throw new IllegalArgumentException("Unknown block '" + baseIdentifier + "'!");
                    Block block = Registries.BLOCKS.getValue(baseIdentifier);
                    if(block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR)
                        throw new IllegalArgumentException("Got AIR block for identifier '" + baseIdentifier + "'!");
                    bases.add(Either.left(block));
                }else
                    bases.add(Either.right(TagKey.create(Registry.BLOCK_REGISTRY, baseIdentifier)));
            }
            int stages = buffer.readInt();
            if(stages < 1 || stages > OreGrowthBlock.MAX_STAGES)
                throw new IllegalArgumentException("Invalid number of stages: '" + stages + "'!");
            double spawnChance = buffer.readDouble();
            if(spawnChance <= 0 || spawnChance > 1)
                throw new IllegalArgumentException("Invalid spawn chance: '" + spawnChance + "'!");
            double growthChance = buffer.readDouble();
            if(growthChance <= 0 || growthChance > 1)
                throw new IllegalArgumentException("Invalid growth chance: '" + growthChance + "'!");
            int dropCount = buffer.readInt();
            List<OreGrowthDrop> drops = new ArrayList<>(dropCount);
            for(int i = 0; i < dropCount; i++){
                int minStage = buffer.readInt();
                int maxStage = buffer.readInt();
                double chance = buffer.readDouble();
                Either<ItemStack,ResourceLocation> result;
                if(buffer.readBoolean())
                    result = Either.left(buffer.readItem());
                else
                    result = Either.right(buffer.readResourceLocation());
                drops.add(new OreGrowthDrop(minStage, maxStage, chance, result));
            }
            int resolvedDropCount = buffer.readInt();
            List<RecipeViewerDrop> resolvedDrops = new ArrayList<>(resolvedDropCount);
            for(int i = 0; i < resolvedDropCount; i++){
                int minStage = buffer.readInt();
                int maxStage = buffer.readInt();
                double chance = buffer.readDouble();
                ItemStack result = buffer.readItem();
                int tooltipCount = buffer.readInt();
                List<Component> tooltip = new ArrayList<>(tooltipCount);
                for(int j = 0; j < tooltipCount; j++)
                    tooltip.add(buffer.readComponent());
                resolvedDrops.add(new RecipeViewerDrop(minStage, maxStage, chance, result, tooltip));
            }
            OreGrowthRecipe recipe = new OreGrowthRecipe(identifier, bases, stages, spawnChance, growthChance, drops);
            recipe.resolvedDrops = resolvedDrops;
            return recipe;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OreGrowthRecipe recipe){
            buffer.writeInt(recipe.bases.size());
            for(Either<Block,TagKey<Block>> base : recipe.bases){
                buffer.writeBoolean(base.isLeft());
                buffer.writeResourceLocation(base.flatMap(Registries.BLOCKS::getIdentifier, TagKey::location));
            }
            buffer.writeInt(recipe.stages);
            buffer.writeDouble(recipe.spawnChance);
            buffer.writeDouble(recipe.growthChance);
            buffer.writeInt(recipe.drops.size());
            for(OreGrowthDrop drop : recipe.drops){
                buffer.writeInt(drop.minStage);
                buffer.writeInt(drop.maxStage);
                buffer.writeDouble(drop.chance);
                buffer.writeBoolean(drop.result.isLeft());
                drop.result.ifLeft(buffer::writeItem);
                drop.result.ifRight(buffer::writeResourceLocation);
            }
            // Send the resolved drops as well
            //noinspection ConstantValue
            if(CommonUtils.getServer() != null && CommonUtils.getServer().getLootTables() != null) // I don't think this should ever occur, but just in case prevent a crash
                recipe.resolveDrops(CommonUtils.getServer().getLootTables()::get);
            if(recipe.resolvedDrops == null){
                buffer.writeInt(0);
                return;
            }
            buffer.writeInt(recipe.resolvedDrops.size());
            for(RecipeViewerDrop drop : recipe.resolvedDrops){
                buffer.writeInt(drop.minStage);
                buffer.writeInt(drop.maxStage);
                buffer.writeDouble(drop.chance);
                buffer.writeItem(drop.result);
                buffer.writeInt(drop.tooltip.size());
                for(Component component : drop.tooltip)
                    buffer.writeComponent(component);
            }
        }
    }
}
