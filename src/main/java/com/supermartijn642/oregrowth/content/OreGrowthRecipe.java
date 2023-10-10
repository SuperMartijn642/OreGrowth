package com.supermartijn642.oregrowth.content;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

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
    public ItemStack assemble(Container container, RegistryAccess registryAccess){
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height){
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess){
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

        @Override
        public Codec<OreGrowthRecipe> codec(){
            return RecordCodecBuilder.create(instance -> instance.group(
                Registries.BLOCKS.getVanillaRegistry().byNameCodec().fieldOf("base").forGetter(OreGrowthRecipe::base),
                Codec.intRange(1, 4).fieldOf("stages").forGetter(OreGrowthRecipe::stages),
                Codec.doubleRange(0, 1).fieldOf("spawn_chance").forGetter(OreGrowthRecipe::spawnChance),
                Codec.doubleRange(0, 1).fieldOf("growth_chance").forGetter(OreGrowthRecipe::growthChance),
                CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(OreGrowthRecipe::output)
            ).apply(instance, OreGrowthRecipe::new));
        }

        @Override
        public OreGrowthRecipe fromNetwork(FriendlyByteBuf buffer){
            ResourceLocation blockIdentifier = buffer.readResourceLocation();
            if(!Registries.BLOCKS.hasIdentifier(blockIdentifier))
                throw new RuntimeException("Unknown block '" + blockIdentifier + "'!");
            Block base = Registries.BLOCKS.getValue(blockIdentifier);
            if(base == Blocks.AIR || base == Blocks.CAVE_AIR || base == Blocks.VOID_AIR)
                throw new RuntimeException("Got AIR block for identifier '" + blockIdentifier + "'!");
            int stages = buffer.readInt();
            if(stages < 1 || stages > OreGrowthBlock.MAX_STAGES)
                throw new RuntimeException("Invalid number of stages: '" + stages + "'!");
            double spawnChance = buffer.readDouble();
            if(spawnChance <= 0 || spawnChance > 1)
                throw new RuntimeException("Invalid spawn chance: '" + spawnChance + "'!");
            double growthChance = buffer.readDouble();
            if(growthChance <= 0 || growthChance > 1)
                throw new RuntimeException("Invalid growth chance: '" + growthChance + "'!");
            ItemStack output = buffer.readItem();
            if(output.isEmpty())
                throw new RuntimeException("Invalid output '" + output + "'!");
            return new OreGrowthRecipe(base, stages, spawnChance, growthChance, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OreGrowthRecipe recipe){
            buffer.writeResourceLocation(Registries.BLOCKS.getIdentifier(recipe.base));
            buffer.writeInt(recipe.stages);
            buffer.writeDouble(recipe.spawnChance);
            buffer.writeDouble(recipe.growthChance);
            buffer.writeItem(recipe.output);
        }
    }
}
