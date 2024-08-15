package com.supermartijn642.oregrowth.content;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.core.util.Triple;
import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthRecipe implements Recipe<Container> {

    public static final Serializer SERIALIZER = new Serializer();

    private final Block base;
    private final int stages;
    private final double spawnChance, growthChance;
    private final ItemStack output;

    public OreGrowthRecipe(Block base, int stages, double spawnChance, double growthChance, ItemStack output){
        this.base = base;
        this.stages = stages;
        this.spawnChance = spawnChance;
        this.growthChance = growthChance;
        this.output = output;
    }

    public Block base(){
        return this.base;
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

    public ItemStack output(){
        return this.output.copy();
    }

    @Override
    public boolean matches(Container container, Level level){
        return false;
    }

    @Override
    public ItemStack assemble(Container container, HolderLookup.Provider provider){
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height){
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider){
        return ItemStack.EMPTY;
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
        json.addProperty("base", Registries.BLOCKS.getIdentifier(this.base).toString());
        json.addProperty("stages", this.stages);
        json.addProperty("spawn_chance", this.spawnChance);
        json.addProperty("growth_chance", this.growthChance);
        JsonObject itemJson = new JsonObject();
        itemJson.addProperty("item", Registries.ITEMS.getIdentifier(this.output.getItem()).toString());
        itemJson.addProperty("count", this.output.getCount());
        json.add("result", itemJson);
        return json;
    }

    private static class Serializer implements RecipeSerializer<OreGrowthRecipe> {

        public static final Codec<ItemStack> ITEM_CODEC = Codec.lazyInitialized(
            () -> RecordCodecBuilder.<Triple<Pair<Holder<Item>,Holder<Item>>,Integer,DataComponentPatch>>create(
                instance -> instance.group(
                        ItemStack.ITEM_NON_AIR_CODEC.optionalFieldOf("id").forGetter(t -> Optional.of(t.left().left())),
                        ItemStack.ITEM_NON_AIR_CODEC.optionalFieldOf("item").forGetter(t -> Optional.empty()),
                        ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(Triple::middle),
                        DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(Triple::right)
                    )
                    .apply(instance, (t1, t2, t3, t4) -> Triple.of(Pair.of(t1.orElse(null), t2.orElse(null)), t3, t4))
            ).flatXmap(
                t -> {
                    Pair<Holder<Item>,Holder<Item>> items = t.left();
                    if(items.left() == null && items.right() == null)
                        return DataResult.error(() -> "Missing item identifier field 'id'!");
                    if(items.left() != null && items.right() != null)
                        return DataResult.error(() -> "Can only have one of 'item' or 'id' fields, not both!");
                    return DataResult.success(new ItemStack(items.left() == null ? items.right() : items.left(), t.middle(), t.right()));
                },
                s -> DataResult.success(Triple.of(Pair.of(s.getItemHolder(), null), s.getCount(), s.getComponentsPatch()))
            )
        ).validate(ItemStack::validateStrict); // Allow either 'id' or 'item' as key for the item identifier
        private static final MapCodec<OreGrowthRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Registries.BLOCKS.getVanillaRegistry().byNameCodec().validate(block -> block == Blocks.AIR ? DataResult.error(() -> "Unknown base block!") : DataResult.success(block)).fieldOf("base").forGetter(OreGrowthRecipe::base),
            Codec.intRange(1, 4).fieldOf("stages").forGetter(OreGrowthRecipe::stages),
            Codec.doubleRange(0, 1).fieldOf("spawn_chance").forGetter(OreGrowthRecipe::spawnChance),
            Codec.doubleRange(0, 1).fieldOf("growth_chance").forGetter(OreGrowthRecipe::growthChance),
            ITEM_CODEC.fieldOf("result").forGetter(OreGrowthRecipe::output)
        ).apply(instance, OreGrowthRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,OreGrowthRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<OreGrowthRecipe> codec(){
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf,OreGrowthRecipe> streamCodec(){
            return STREAM_CODEC;
        }

        private static OreGrowthRecipe fromNetwork(RegistryFriendlyByteBuf buffer){
            ResourceLocation blockIdentifier = buffer.readResourceLocation();
            if(!Registries.BLOCKS.hasIdentifier(blockIdentifier))
                throw new IllegalArgumentException("Unknown block '" + blockIdentifier + "'!");
            Block base = Registries.BLOCKS.getValue(blockIdentifier);
            if(base == Blocks.AIR || base == Blocks.CAVE_AIR || base == Blocks.VOID_AIR)
                throw new IllegalArgumentException("Got AIR block for identifier '" + blockIdentifier + "'!");
            int stages = buffer.readInt();
            if(stages < 1 || stages > OreGrowthBlock.MAX_STAGES)
                throw new IllegalArgumentException("Invalid number of stages: '" + stages + "'!");
            double spawnChance = buffer.readDouble();
            if(spawnChance <= 0 || spawnChance > 1)
                throw new IllegalArgumentException("Invalid spawn chance: '" + spawnChance + "'!");
            double growthChance = buffer.readDouble();
            if(growthChance <= 0 || growthChance > 1)
                throw new IllegalArgumentException("Invalid growth chance: '" + growthChance + "'!");
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            return new OreGrowthRecipe(base, stages, spawnChance, growthChance, output);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, OreGrowthRecipe recipe){
            buffer.writeResourceLocation(Registries.BLOCKS.getIdentifier(recipe.base));
            buffer.writeInt(recipe.stages);
            buffer.writeDouble(recipe.spawnChance);
            buffer.writeDouble(recipe.growthChance);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
        }
    }
}
