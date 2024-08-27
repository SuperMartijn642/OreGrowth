package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.data.condition.ModLoadedResourceCondition;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthDefaultRecipeCondition;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
public class OreGrowthOreGrowthRecipeGenerator extends OreGrowthRecipeGenerator {

    public OreGrowthOreGrowthRecipeGenerator(ResourceCache cache){
        super(OreGrowth.MODID, cache);
    }

    @Override
    public OreGrowthRecipeBuilder recipe(String namespace, String location, ResourceLocation base, int stages, double spawnChance, double growthChance){
        return super.recipe(namespace, location, base, stages, spawnChance, growthChance)
            .condition(new OreGrowthDefaultRecipeCondition());
    }

    @Override
    public void generate(){
        // Some vanilla recipes need to be disabled when Spelunkery is installed
        ResourceCondition noSpelunkery = new ModLoadedResourceCondition("spelunkery").negate();

        // ----- Vanilla ----- //
        // Coal ore
        this.recipe("coal_ore_growth", Blocks.COAL_ORE, 4, 0.3, 0.4)
            .baseBlock(Blocks.DEEPSLATE_COAL_ORE)
            .itemDrop(4, Items.COAL)
            .itemDrop(1, 3, 0.1, Items.COAL);
        // Iron ore
        this.recipe("iron_ore_growth", Blocks.IRON_ORE, 4, 0.2, 0.25)
            .baseBlock(Blocks.DEEPSLATE_IRON_ORE)
            .itemDrop(4, Items.RAW_IRON)
            .itemDrop(4, 0.1, Items.IRON_NUGGET)
            .itemDrop(1, 3, 0.1, Items.RAW_IRON);
        // Copper ore
        this.recipe("copper_ore_growth", Blocks.COPPER_ORE, 4, 0.2, 0.3)
            .baseBlock(Blocks.DEEPSLATE_COPPER_ORE)
            .itemDrop(4, Items.RAW_COPPER)
            .itemDrop(1, 3, 0.1, Items.RAW_COPPER);
        // Gold ore
        this.recipe("gold_ore_growth", Blocks.GOLD_ORE, 4, 0.2, 0.2)
            .baseBlock(Blocks.DEEPSLATE_GOLD_ORE)
            .itemDrop(4, Items.RAW_GOLD)
            .itemDrop(4, 0.1, Items.GOLD_NUGGET)
            .itemDrop(1, 3, 0.1, Items.RAW_GOLD);
        this.recipe("nether_gold_ore_growth", Blocks.NETHER_GOLD_ORE, 3, 0.3, 0.5)
            .itemDrop(3, Items.GOLD_NUGGET)
            .itemDrop(3, 0.3, Items.GOLD_NUGGET)
            .itemDrop(2, Items.GOLD_NUGGET)
            .itemDrop(1, 0.7, Items.GOLD_NUGGET)
            .condition(noSpelunkery);
        // Lapis ore
        this.recipe("lapis_ore_growth", Blocks.LAPIS_ORE, 4, 0.3, 0.3)
            .baseBlock(Blocks.DEEPSLATE_LAPIS_ORE)
            .itemDrop(4, Items.LAPIS_LAZULI)
            .itemDrop(4, 0.2, Items.LAPIS_LAZULI)
            .itemDrop(3, 0.6, Items.LAPIS_LAZULI)
            .itemDrop(1, 2, 0.3, Items.LAPIS_LAZULI)
            .condition(noSpelunkery);
        // Redstone ore
        this.recipe("redstone_ore_growth", Blocks.REDSTONE_ORE, 4, 0.3, 0.3)
            .baseBlock(Blocks.DEEPSLATE_REDSTONE_ORE)
            .itemDrop(4, Items.REDSTONE)
            .itemDrop(4, 0.4, Items.REDSTONE)
            .itemDrop(3, 0.8, Items.REDSTONE)
            .itemDrop(1, 2, 0.4, Items.REDSTONE)
            .condition(noSpelunkery);
        // Emerald ore
        this.recipe("emerald_ore_growth", Blocks.EMERALD_ORE, 4, 0.1, 0.15)
            .baseBlock(Blocks.DEEPSLATE_EMERALD_ORE)
            .itemDrop(4, Items.EMERALD)
            .itemDrop(1, 3, 0.1, Items.EMERALD)
            .condition(noSpelunkery);
        // Diamond ore
        this.recipe("diamond_ore_growth", Blocks.DIAMOND_ORE, 4, 0.1, 0.1)
            .baseBlock(Blocks.DEEPSLATE_DIAMOND_ORE)
            .itemDrop(4, Items.DIAMOND)
            .itemDrop(1, 3, 0.1, Items.DIAMOND)
            .condition(noSpelunkery);
        // Quartz ore
        this.recipe("nether_quartz_ore_growth", Blocks.NETHER_QUARTZ_ORE, 4, 0.2, 0.3)
            .itemDrop(4, Items.QUARTZ)
            .itemDrop(4, 0.2, Items.QUARTZ)
            .itemDrop(3, 0.8, Items.QUARTZ)
            .itemDrop(1, 2, 0.3, Items.QUARTZ);
        // Netherite scrap
        this.recipe("ancient_debris_growth", Blocks.ANCIENT_DEBRIS, 4, 0.1, 0.05)
            .itemDrop(4, 0.4, Items.NETHERITE_SCRAP)
            .itemDrop(3, 0.1, Items.NETHERITE_SCRAP)
            .itemDrop(2, 0.05, Items.NETHERITE_SCRAP);

        // ----- Additional Blocks: Stone Edition ----- //
        String abStoneEdition = "abstoneedition";
        // Silver ore
        this.modIntegration(abStoneEdition, "silver_ore", 4, 0.25, 0.25)
            .itemDrop(4, "raw_silver")
            .itemDrop(4, 0.1, "silver_nugget")
            .itemDrop(1, 3, 0.1, "raw_silver");
        // Bismuth ore
        this.modIntegration(abStoneEdition, "bismuth_ore", 4, 0.1, 0.05)
            .itemDrop(4, "raw_bismuth_chunk")
            .itemDrop(1, 3, 0.1, "raw_bismuth_chunk");
        // Uranium ore
        this.modIntegration(abStoneEdition, "uranium_ore", 4, 0.15, 0.2)
            .itemDrop(4, "raw_uranium")
            .itemDrop(1, 3, 0.1, "raw_uranium");

        // ----- BetterEnd ----- //
        String betterEnd = "betterend";
        // Ender ore
        this.modIntegration(betterEnd, "ender_ore", 4, 0.15, 0.15)
            .itemDrop(4, "ender_shard")
            .itemDrop(1, 3, 0.1, "ender_shard");
        // Amber ore
        this.modIntegration(betterEnd, "amber_ore", 4, 0.2, 0.15)
            .itemDrop(4, "raw_amber")
            .itemDrop(3, 0.6, "raw_amber")
            .itemDrop(1, 2, 0.2, "raw_amber");
        // Thallasium ore
        this.modIntegration(betterEnd, "thallasium_ore", 4, 0.1, 0.15)
            .itemDrop(4, "thallasium_raw")
            .itemDrop(4, 0.1, "thallasium_nugget")
            .itemDrop(1, 3, 0.1, "thallasium_raw");

        // ----- BetterNether ----- //
        String betterNether = "betternether";
        // Cincinnasite ore
        this.modIntegration(betterNether, "cincinnasite_ore", 4, 0.15, 0.15)
            .itemDrop(4, "cincinnasite")
            .itemDrop(1, 3, 0.1, "cincinnasite");
        // Nether ruby ore
        this.modIntegration(betterNether, "nether_ruby_ore", 4, 0.1, 0.1)
            .itemDrop(4, "nether_ruby")
            .itemDrop(1, 3, 0.1, "nether_ruby");
        // Nether lapis ore
        this.modIntegration(betterNether, "nether_lapis_ore", 2, 0.25, 0.25)
            .itemDrop(2, "lapis_pile")
            .itemDrop(2, 0.3, "lapis_pile")
            .itemDrop(1, 0.9, "lapis_pile");
        // Nether redstone ore
        this.modIntegration(betterNether, "nether_redstone_ore", 2, 0.2, 0.2)
            .itemDrop(2, Items.REDSTONE)
            .itemDrop(1, 0.6, Items.REDSTONE);

        // ----- Bigger Reactors ----- //
        String biggerReactors = "biggerreactors";
        // Uranium ore
        this.modIntegration(biggerReactors, "uranium_ore", 4, 0.15, 0.2)
            .itemDrop(4, "uranium_chunk")
            .itemDrop(1, 3, 0.1, "uranium_chunk");

        // ----- Create ----- //
        String create = "create";
        // Zinc ore
        this.modIntegration(create, "zinc_ore", 4, 0.2, 0.25)
            .baseBlock("deepslate_zinc_ore")
            .itemDrop(4, "raw_zinc")
            .itemDrop(4, 0.1, "zinc_nugget")
            .itemDrop(1, 3, 0.1, "raw_zinc");

        // ----- Deep Aether ----- //
        String deepAether = "deep_aether";
        // Skyjade ore
        this.modIntegration(deepAether, "skyjade_ore", 4, 0.1, 0.15)
            .itemDrop(4, "skyjade")
            .itemDrop(1, 3, 0.1, "skyjade");

        // ----- Enderscape ----- //
        String enderscape = "enderscape";
        // Nebulite ore
        this.modIntegration(enderscape, "nebulite_ore", 3, 0.3, 0.3)
            .itemDrop(3, "nebulite_shards")
            .itemDrop(1, 2, 0.3, "nebulite_shards");
        // Shadow quartz ore
        this.modIntegration(enderscape, "shadow_quartz_ore", 4, 0.2, 0.3)
            .itemDrop(4, "shadow_quartz")
            .itemDrop(3, 0.4, "shadow_quartz")
            .itemDrop(1, 2, 0.1, "shadow_quartz");

        // ----- Extreme Reactors ----- //
        String extremeReactors = "bigreactors";
        // Yellorite ore
        this.modIntegration(extremeReactors, "yellorite_ore", 4, 0.15, 0.2)
            .baseBlock("deepslate_yellorite_ore")
            .itemDrop(4, "yellorium_nugget")
            .itemDrop(4, 0.2, "yellorium_nugget")
            .itemDrop(3, 0.8, "yellorium_nugget")
            .itemDrop(1, 2, 0.4, "yellorium_nugget");
        // Anglesite ore
        this.modIntegration(extremeReactors, "anglesite_ore", 4, 0.1, 0.1)
            .itemDrop(4, "anglesite_crystal")
            .itemDrop(1, 3, 0.1, "anglesite_crystal");
        // Benitoite ore
        this.modIntegration(extremeReactors, "benitoite_ore", 4, 0.1, 0.1)
            .itemDrop(4, "benitoite_crystal")
            .itemDrop(1, 3, 0.1, "benitoite_crystal");

        // ----- Industrial Revolution ----- //
        String industrialRevolution = "indrev";
        // Tungsten ore
        this.modIntegration(industrialRevolution, "tungsten_ore", 4, 0.15, 0.2)
            .baseBlock("deepslate_tungsten_ore")
            .itemDrop(4, "raw_tungsten")
            .itemDrop(4, 0.1, "tungsten_nugget")
            .itemDrop(1, 3, 0.1, "raw_tungsten");
        // Tin ore
        this.modIntegration(industrialRevolution, "tin_ore", 4, 0.2, 0.25)
            .baseBlock("deepslate_tin_ore")
            .itemDrop(4, "raw_tin")
            .itemDrop(4, 0.1, "tin_nugget")
            .itemDrop(1, 3, 0.1, "raw_tin");
        // Silver ore
        this.modIntegration(industrialRevolution, "silver_ore", 4, 0.25, 0.25)
            .baseBlock("deepslate_silver_ore")
            .itemDrop(4, "raw_silver")
            .itemDrop(4, 0.1, "silver_nugget")
            .itemDrop(1, 3, 0.1, "raw_silver");
        // Nikolite ore
        this.modIntegration(industrialRevolution, "nikolite_ore", 4, 0.3, 0.35)
            .baseBlock("deepslate_nikolite_ore")
            .itemDrop(4, "nikolite_dust")
            .itemDrop(4, 0.2, "nikolite_dust")
            .itemDrop(3, 0.7, "nikolite_dust")
            .itemDrop(1, 2, 0.2, "nikolite_dust");
        // Lead ore
        this.modIntegration(industrialRevolution, "lead_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_lead_ore")
            .itemDrop(4, "raw_lead")
            .itemDrop(4, 0.1, "lead_nugget")
            .itemDrop(1, 3, 0.1, "raw_lead");

        // ----- Gems & Jewels ----- //
        String gemsNJewels = "gemsnjewels";
        // Pale diamond ore block
        this.modIntegration(gemsNJewels, "pale_diamond_ore_block", 4, 0.1, 0.1)
            .baseBlock("pale_diamond_deepslate_ore_block").baseBlock("pale_diamond_nether_ore_block")
            .itemDrop(4, "pale_diamond")
            .itemDrop(1, 3, 0.1, "pale_diamond");
        // Dusk emerald ore block
        this.modIntegration(gemsNJewels, "emerald_ore_block", 4, 0.1, 0.15)
            .baseBlock("emerald_deepslate_ore_block").baseBlock("emerald_nether_ore_block")
            .itemDrop(4, "emerald")
            .itemDrop(1, 3, 0.1, "emerald");
        // Ruby ore block
        this.modIntegration(gemsNJewels, "ruby_ore_block", 4, 0.1, 0.1)
            .baseBlock("ruby_deepslate_ore_block").baseBlock("ruby_nether_ore_block")
            .itemDrop(4, "ruby")
            .itemDrop(1, 3, 0.1, "ruby");
        // Sapphire ore block
        this.modIntegration(gemsNJewels, "sapphire_ore_block", 4, 0.15, 0.15)
            .baseBlock("sapphire_deepslate_ore_block").baseBlock("sapphire_nether_ore_block")
            .itemDrop(4, "sapphire")
            .itemDrop(1, 3, 0.1, "sapphire");
        // Amethyst ore block
        this.modIntegration(gemsNJewels, "amethyst_ore_block", 4, 0.15, 0.15)
            .baseBlock("amethyst_deepslate_ore_block").baseBlock("amethyst_nether_ore_block")
            .itemDrop(4, "amethyst")
            .itemDrop(1, 3, 0.1, "amethyst");
        // Opal ore block
        this.modIntegration(gemsNJewels, "opal_ore_block", 4, 0.2, 0.15)
            .baseBlock("bright_opal_ore_block").baseBlock("opal_deepslate_ore_block").baseBlock("opal_nether_ore_block")
            .itemDrop(4, "opal")
            .itemDrop(1, 3, 0.1, "opal");
        // Garnet ore block
        this.modIntegration(gemsNJewels, "garnet_ore_block", 4, 0.15, 0.15)
            .baseBlock("garnet_deepslate_ore_block").baseBlock("garnet_nether_ore_block")
            .itemDrop(4, "garnet")
            .itemDrop(1, 3, 0.1, "garnet");
        // Topaz ore block
        this.modIntegration(gemsNJewels, "topaz_ore_block", 4, 0.1, 0.1)
            .baseBlock("topaz_deepslate_ore_block").baseBlock("topaz_nether_ore_block")
            .itemDrop(4, "topaz")
            .itemDrop(1, 3, 0.1, "topaz");
        // Peridot ore block
        this.modIntegration(gemsNJewels, "peridot_ore_block", 4, 0.15, 0.15)
            .baseBlock("peridot_deepslate_ore_block").baseBlock("peridot_nether_ore_block")
            .itemDrop(4, "peridot")
            .itemDrop(1, 3, 0.1, "peridot");
        // Aquamarine ore block
        this.modIntegration(gemsNJewels, "aquamarine_ore_block", 4, 0.15, 0.15)
            .baseBlock("aquamarine_deepslate_ore_block").baseBlock("aquamarine_nether_ore_block")
            .itemDrop(4, "aquamarine")
            .itemDrop(1, 3, 0.1, "aquamarine");
        // Zircon ore block
        this.modIntegration(gemsNJewels, "zircon_ore_block", 4, 0.15, 0.15)
            .baseBlock("zircon_deepslate_ore_block").baseBlock("zircon_nether_ore_block")
            .itemDrop(4, "zircon")
            .itemDrop(1, 3, 0.1, "zircon");
        // Alexandrite ore block
        this.modIntegration(gemsNJewels, "alexandrite_ore_block", 4, 0.15, 0.15)
            .baseBlock("alexandrite_deepslate_ore_block").baseBlock("alexandrite_nether_ore_block")
            .itemDrop(4, "alexandrite")
            .itemDrop(1, 3, 0.1, "alexandrite");
        // Tanzanite ore block
        this.modIntegration(gemsNJewels, "tanzanite_ore_block", 4, 0.2, 0.15)
            .baseBlock("tanzanite_deepslate_ore_block").baseBlock("tanzanite_nether_ore_block")
            .itemDrop(4, "tanzanite")
            .itemDrop(1, 3, 0.1, "tanzanite");
        // Tourmaline ore block
        this.modIntegration(gemsNJewels, "tourmaline_ore_block", 4, 0.15, 0.15)
            .baseBlock("tourmaline_deepslate_ore_block").baseBlock("tourmaline_nether_ore_block")
            .itemDrop(4, "tourmaline")
            .itemDrop(1, 3, 0.1, "tourmaline");
        // Spinel ore block
        this.modIntegration(gemsNJewels, "spinel_ore_block", 4, 0.1, 0.1)
            .baseBlock("spinel_deepslate_ore_block").baseBlock("spinel_nether_ore_block")
            .itemDrop(4, "spinel")
            .itemDrop(1, 3, 0.1, "spinel");
        // Black opal ore block
        this.modIntegration(gemsNJewels, "black_opal_ore_block", 4, 0.2, 0.15)
            .baseBlock("black_opal_deepslate_ore_block").baseBlock("black_opal_nether_ore_block")
            .itemDrop(4, "black_opal")
            .itemDrop(1, 3, 0.1, "black_opal");
        // Citrine ore block
        this.modIntegration(gemsNJewels, "citrine_ore_block", 4, 0.15, 0.15)
            .baseBlock("citrine_deepslate_ore_block").baseBlock("citrine_nether_ore_block")
            .itemDrop(4, "citrine")
            .itemDrop(1, 3, 0.1, "citrine");
        // Morganite ore block
        this.modIntegration(gemsNJewels, "morganite_ore_block", 4, 0.15, 0.15)
            .baseBlock("morganite_deepslate_ore_block").baseBlock("morganite_nether_ore_block")
            .itemDrop(4, "morganite")
            .itemDrop(1, 3, 0.1, "morganite");
        // Ametrine ore block
        this.modIntegration(gemsNJewels, "ametrine_ore_block", 4, 0.15, 0.15)
            .baseBlock("ametrine_deepslate_ore_block").baseBlock("ametrine_nether_ore_block")
            .itemDrop(4, "ametrine")
            .itemDrop(1, 3, 0.1, "ametrine");
        // Kunzite ore block
        this.modIntegration(gemsNJewels, "kunzite_ore_block", 4, 0.15, 0.15)
            .baseBlock("kunzite_deepslate_ore_block").baseBlock("kunzite_nether_ore_block")
            .itemDrop(4, "kunzite")
            .itemDrop(1, 3, 0.1, "kunzite");
        // Iolite ore block
        this.modIntegration(gemsNJewels, "iolite_ore_block", 4, 0.15, 0.15)
            .baseBlock("iolite_deepslate_ore_block").baseBlock("iolite_nether_ore_block")
            .itemDrop(4, "iolite")
            .itemDrop(1, 3, 0.1, "iolite");
        // Diamond nether ore block
        this.modIntegration(gemsNJewels, "diamond_nether_ore_block", 4, 0.1, 0.1)
            .itemDrop(4, Items.DIAMOND)
            .itemDrop(1, 3, 0.1, Items.DIAMOND);
        // Emerald nether ore block
        this.modIntegration(gemsNJewels, "v_emerald_nether_ore_block", 4, 0.1, 0.15)
            .itemDrop(4, Items.EMERALD)
            .itemDrop(1, 3, 0.1, Items.EMERALD);

        // ----- Mekanism ----- //
        String mekanism = "mekanism";
        // Tin ore
        this.modIntegration(mekanism, "tin_ore", 4, 0.2, 0.25)
            .baseBlock("deepslate_tin_ore")
            .itemDrop(4, "raw_tin")
            .itemDrop(4, 0.1, "tin_nugget")
            .itemDrop(1, 3, 0.1, "raw_tin");
        // Osmium ore
        this.modIntegration(mekanism, "osmium_ore", 4, 0.15, 0.2)
            .baseBlock("deepslate_osmium_ore")
            .itemDrop(4, "raw_osmium")
            .itemDrop(4, 0.1, "osmium_nugget")
            .itemDrop(1, 3, 0.1, "raw_osmium");
        // Uranium ore
        this.modIntegration(mekanism, "uranium_ore", 4, 0.15, 0.2)
            .baseBlock("deepslate_uranium_ore")
            .itemDrop(4, "raw_uranium")
            .itemDrop(4, 0.1, "uranium_nugget")
            .itemDrop(1, 3, 0.1, "raw_uranium");
        // Fluorite ore
        this.modIntegration(mekanism, "fluorite_ore", 4, 0.3, 0.4)
            .baseBlock("deepslate_fluorite_ore")
            .itemDrop(4, "fluorite_gem")
            .itemDrop(3, 0.7, "fluorite_gem")
            .itemDrop(1, 2, 0.2, "fluorite_gem");
        // Lead ore
        this.modIntegration(mekanism, "lead_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_lead_ore")
            .itemDrop(4, "raw_lead")
            .itemDrop(4, 0.1, "lead_nugget")
            .itemDrop(1, 3, 0.1, "raw_lead");

        // ----- Modern Industrialization ----- //
        String modernIndustrialization = "modern_industrialization";
        // Quartz ore
        this.modIntegration(modernIndustrialization, "quartz_ore", 4, 0.2, 0.3)
            .itemDrop(4, Items.QUARTZ)
            .itemDrop(4, 0.2, Items.QUARTZ)
            .itemDrop(3, 0.8, Items.QUARTZ)
            .itemDrop(1, 2, 0.3, Items.QUARTZ);
        // Tin ore
        this.modIntegration(modernIndustrialization, "tin_ore", 4, 0.2, 0.25)
            .baseBlock("deepslate_tin_ore")
            .itemDrop(4, "raw_tin")
            .itemDrop(4, 0.1, "tin_nugget")
            .itemDrop(1, 3, 0.1, "raw_tin");
        // Lignite coal ore
        this.modIntegration(modernIndustrialization, "lignite_coal_ore", 4, 0.3, 0.4)
            .baseBlock("deepslate_lignite_coal_ore")
            .itemDrop(4, "lignite_coal")
            .itemDrop(1, 3, 0.1, "lignite_coal");
        // Bauxite ore
        this.modIntegration(modernIndustrialization, "bauxite_ore", 4, 0.15, 0.25)
            .baseBlock("deepslate_bauxite_ore")
            .itemDrop(4, "bauxite_dust")
            .itemDrop(3, 0.7, "bauxite_dust")
            .itemDrop(1, 2, 0.3, "bauxite_dust");
        // Lead ore
        this.modIntegration(modernIndustrialization, "lead_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_lead_ore")
            .itemDrop(4, "raw_lead")
            .itemDrop(4, 0.1, "lead_nugget")
            .itemDrop(1, 3, 0.1, "raw_lead");
        // Antimony ore
        this.modIntegration(modernIndustrialization, "antimony_ore", 4, 0.15, 0.2)
            .baseBlock("deepslate_antimony_ore")
            .itemDrop(4, "raw_antimony")
            .itemDrop(4, 0.1, "antimony_nugget")
            .itemDrop(1, 3, 0.1, "raw_antimony");
        // Nickel ore
        this.modIntegration(modernIndustrialization, "nickel_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_nickel_ore")
            .itemDrop(4, "raw_nickel")
            .itemDrop(4, 0.1, "nickel_nugget")
            .itemDrop(1, 3, 0.1, "raw_nickel");
        // Salt ore
        this.modIntegration(modernIndustrialization, "salt_ore", 2, 0.2, 0.3)
            .baseBlock("deepslate_salt_ore")
            .itemDrop(2, 0.2, "salt_dust")
            .itemDrop(1, 2, 1, "salt_tiny_dust");
        // Titanium ore
        this.modIntegration(modernIndustrialization, "titanium_ore", 4, 0.1, 0.1)
            .itemDrop(4, 0.8, "raw_titanium")
            .itemDrop(3, 4, 0.1, "titanium_dust")
            .itemDrop(3, 0.2, "raw_titanium")
            .itemDrop(1, 2, 0.05, "raw_titanium");
        // Uranium ore
        this.modIntegration(modernIndustrialization, "uranium_ore", 4, 0.15, 0.2)
            .baseBlock("deepslate_uranium_ore")
            .itemDrop(4, "raw_uranium")
            .itemDrop(4, 0.1, "uranium_nugget")
            .itemDrop(1, 3, 0.1, "raw_uranium");
        // Platinum ore
        this.modIntegration(modernIndustrialization, "platinum_ore", 4, 0.2, 0.2)
            .itemDrop(4, "raw_platinum")
            .itemDrop(4, 0.1, "platinum_nugget")
            .itemDrop(1, 3, 0.1, "raw_platinum");
        // Iridium ore
        this.modIntegration(modernIndustrialization, "iridium_ore", 4, 0.1, 0.1)
            .baseBlock("deepslate_iridium_ore")
            .itemDrop(4, 0.7, "raw_iridium")
            .itemDrop(4, 0.1, "iridium_nugget")
            .itemDrop(3, 0.2, "raw_iridium")
            .itemDrop(1, 2, 0.05, "raw_iridium");
        // Monazite ore
        this.modIntegration(modernIndustrialization, "monazite_ore", 4, 0.15, 0.25)
            .baseBlock("deepslate_monazite_ore")
            .itemDrop(4, "monazite_dust")
            .itemDrop(3, 0.7, "monazite_dust")
            .itemDrop(1, 2, 0.3, "monazite_dust");
        // Tungsten ore
        this.modIntegration(modernIndustrialization, "tungsten_ore", 4, 0.15, 0.2)
            .baseBlock("deepslate_tungsten_ore")
            .itemDrop(4, "raw_tungsten")
            .itemDrop(4, 0.1, "tungsten_nugget")
            .itemDrop(1, 3, 0.1, "raw_tungsten");

        // ----- More Gems [FABRIC] ----- //
        String moreGems = "more_gems";
        // Citrine ore
        this.modIntegration(moreGems, "citrine_ore", 4, 0.15, 0.15)
            .itemDrop(4, "citrine")
            .itemDrop(1, 3, 0.1, "citrine");
        // Tourmaline ore
        this.modIntegration(moreGems, "tourmaline_ore", 4, 0.15, 0.15)
            .itemDrop(4, "tourmaline")
            .itemDrop(1, 3, 0.1, "tourmaline");
        // Kunzite ore
        this.modIntegration(moreGems, "kunzite_ore", 4, 0.15, 0.15)
            .baseBlock("kunzite_ore_nether")
            .itemDrop(4, "kunzite")
            .itemDrop(1, 3, 0.1, "kunzite");
        // Topaz ore
        this.modIntegration(moreGems, "topaz_ore", 4, 0.15, 0.15)
            .itemDrop(4, "topaz")
            .itemDrop(1, 3, 0.1, "topaz");
        // Alexandrite ore
        this.modIntegration(moreGems, "alexandrite_ore", 4, 0.15, 0.15)
            .baseBlock("alexandrite_ore_nether")
            .itemDrop(4, "alexandrite")
            .itemDrop(1, 3, 0.1, "alexandrite");
        // Corundum ore
        this.modIntegration(moreGems, "corundum_ore", 4, 0.15, 0.15)
            .baseBlock("corundum_ore_nether")
            .itemDrop(4, "corundum")
            .itemDrop(1, 3, 0.1, "corundum");
        // Sapphire ore
        this.modIntegration(moreGems, "sapphire_ore", 4, 0.15, 0.15)
            .baseBlock("sapphire_ore_deepslate")
            .itemDrop(4, "sapphire")
            .itemDrop(1, 3, 0.1, "sapphire");
        // Spinel ore
        this.modIntegration(moreGems, "spinel_ore", 4, 0.15, 0.15)
            .baseBlock("spinel_ore_deepslate")
            .itemDrop(4, "spinel")
            .itemDrop(1, 3, 0.1, "spinel");
        // Carbonado ore
        this.modIntegration(moreGems, "carbonado_ore", 4, 0.15, 0.15)
            .baseBlock("carbonado_ore_deepslate")
            .itemDrop(4, "carbonado")
            .itemDrop(1, 3, 0.1, "carbonado");
        // Moissanite ore
        this.modIntegration(moreGems, "moissanite_ore", 4, 0.1, 0.1)
            .itemDrop(4, "moissanite")
            .itemDrop(1, 3, 0.1, "moissanite");

        // ----- Mystical Agriculture ----- //
        String mysticalAgriculture = "mysticalagriculture";
        // Prosperity ore
        this.modIntegration(mysticalAgriculture, "prosperity_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_prosperity_ore")
            .itemDrop(4, "prosperity_shard")
            .itemDrop(4, 0.05, "prosperity_shard")
            .itemDrop(3, 0.6, "prosperity_shard")
            .itemDrop(1, 2, 0.1, "prosperity_shard");
        // Inferium ore
        this.modIntegration(mysticalAgriculture, "inferium_ore", 4, 0.3, 0.4)
            .baseBlock("deepslate_inferium_ore")
            .itemDrop(4, "inferium_essence")
            .itemDrop(4, 0.3, "inferium_essence")
            .itemDrop(3, 0.7, "inferium_essence")
            .itemDrop(1, 2, 0.3, "inferium_essence");
        // Soulium ore
        this.modIntegration(mysticalAgriculture, "soulium_ore", 4, 0.2, 0.2)
            .itemDrop(4, 0.7, "soulium_dust")
            .itemDrop(1, 3, 0.1, "soulium_dust");

        // ----- Mythic Metals ----- //
        String mythicMetals = "mythicmetals";
        // Adamantite ore
        this.modIntegration(mythicMetals, "adamantite_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_adamantite_ore")
            .itemDrop(4, "raw_adamantite")
            .itemDrop(4, 0.1, "adamantite_nugget")
            .itemDrop(1, 3, 0.1, "raw_adamantite");
        // Aquarium ore
        this.modIntegration(mythicMetals, "aquarium_ore", 4, 0.2, 0.2)
            .itemDrop(4, "raw_aquarium")
            .itemDrop(4, 0.1, "aquarium_nugget")
            .itemDrop(1, 3, 0.1, "raw_aquarium");
        // Banglum ore
        this.modIntegration(mythicMetals, "banglum_ore", 4, 0.2, 0.2)
            .baseBlock("nether_banglum_ore")
            .itemDrop(4, "raw_banglum")
            .itemDrop(4, 0.08, "banglum_nugget")
            .itemDrop(4, 0.005, "banglum_chunk")
            .itemDrop(1, 3, 0.1, "raw_banglum");
        // Carmot ore
        this.modIntegration(mythicMetals, "carmot_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_carmot_ore")
            .itemDrop(4, "raw_carmot")
            .itemDrop(4, 0.1, "carmot_nugget")
            .itemDrop(1, 3, 0.1, "raw_carmot");
        // Kyber ore
        this.modIntegration(mythicMetals, "kyber_ore", 4, 0.2, 0.2)
            .baseBlock("calcite_kyber_ore")
            .itemDrop(4, "raw_kyber")
            .itemDrop(4, 0.1, "kyber_nugget")
            .itemDrop(1, 3, 0.1, "raw_kyber");
        // Manganese ore
        this.modIntegration(mythicMetals, "manganese_ore", 4, 0.2, 0.2)
            .itemDrop(4, "raw_manganese")
            .itemDrop(4, 0.1, "manganese_nugget")
            .itemDrop(1, 3, 0.1, "raw_manganese");
        // Morkite ore
        this.modIntegration(mythicMetals, "morkite_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_morkite_ore")
            .itemDrop(4, "morkite")
            .itemDrop(1, 3, 0.1, "morkite");
        // Midas gold ore
        this.modIntegration(mythicMetals, "midas_gold_ore", 4, 0.2, 0.2)
            .itemDrop(4, "raw_midas_gold")
            .itemDrop(4, 0.1, "midas_gold_nugget")
            .itemDrop(1, 3, 0.1, "raw_midas_gold");
        // Mythril ore
        this.modIntegration(mythicMetals, "mythril_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_mythril_ore")
            .itemDrop(4, "raw_mythril")
            .itemDrop(4, 0.1, "mythril_nugget")
            .itemDrop(1, 3, 0.1, "raw_mythril");
        // Orichalcum ore
        this.modIntegration(mythicMetals, "orichalcum_ore", 4, 0.2, 0.2)
            .baseBlock("tuff_orichalcum_ore").baseBlock("smooth_basalt_orichalcum_ore").baseBlock("deepslate_orichalcum_ore")
            .itemDrop(4, "raw_orichalcum")
            .itemDrop(4, 0.1, "orichalcum_nugget")
            .itemDrop(1, 3, 0.1, "raw_orichalcum");
        // Osmium ore
        this.modIntegration(mythicMetals, "osmium_ore", 4, 0.15, 0.2)
            .itemDrop(4, "raw_osmium")
            .itemDrop(4, 0.1, "osmium_nugget")
            .itemDrop(1, 3, 0.1, "raw_osmium");
        // Palladium ore
        this.modIntegration(mythicMetals, "palladium_ore", 4, 0.2, 0.2)
            .itemDrop(4, "raw_palladium")
            .itemDrop(4, 0.1, "palladium_nugget")
            .itemDrop(1, 3, 0.1, "raw_palladium");
        // Platinum ore
        this.modIntegration(mythicMetals, "platinum_ore", 4, 0.2, 0.2)
            .itemDrop(4, "raw_platinum")
            .itemDrop(4, 0.1, "platinum_nugget")
            .itemDrop(1, 3, 0.1, "raw_platinum");
        // Prometheum ore
        this.modIntegration(mythicMetals, "prometheum_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_prometheum_ore")
            .itemDrop(4, "raw_prometheum")
            .itemDrop(4, 0.1, "prometheum_nugget")
            .itemDrop(1, 3, 0.1, "raw_prometheum");
        // Quadrillum ore
        this.modIntegration(mythicMetals, "quadrillum_ore", 4, 0.2, 0.2)
            .itemDrop(4, "raw_quadrillum")
            .itemDrop(4, 0.1, "quadrillum_nugget")
            .itemDrop(1, 3, 0.1, "raw_quadrillum");
        // Runite ore
        this.modIntegration(mythicMetals, "runite_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_runite_ore")
            .itemDrop(4, "raw_runite")
            .itemDrop(4, 0.1, "runite_nugget")
            .itemDrop(1, 3, 0.1, "raw_runite");
        // Silver ore
        this.modIntegration(mythicMetals, "silver_ore", 4, 0.25, 0.25)
            .itemDrop(4, "raw_silver")
            .itemDrop(4, 0.1, "silver_nugget")
            .itemDrop(1, 3, 0.1, "raw_silver");
        // Starrite ore
        this.modIntegration(mythicMetals, "starrite_ore", 4, 0.2, 0.2)
            .baseBlock("calcite_starrite_ore").baseBlock("end_stone_starrite_ore")
            .itemDrop(4, "starrite")
            .itemDrop(1, 3, 0.1, "starrite");
        // Stormyx ore
        this.modIntegration(mythicMetals, "stormyx_ore", 4, 0.2, 0.2)
            .baseBlock("blackstone_stormyx_ore")
            .itemDrop(4, "raw_stormyx")
            .itemDrop(4, 0.1, "stormyx_nugget")
            .itemDrop(1, 3, 0.1, "raw_stormyx");
        // Tin ore
        this.modIntegration(mythicMetals, "tin_ore", 4, 0.2, 0.25)
            .itemDrop(4, "raw_tin")
            .itemDrop(4, 0.1, "tin_nugget")
            .itemDrop(1, 3, 0.1, "raw_tin");
        // Unobtanium ore
        this.modIntegration(mythicMetals, "unobtainium_ore", 4, 0.1, 0.05)
            .baseBlock("deepslate_unobtainium_ore")
            .itemDrop(4, 0.3, "unobtainium");

        // ----- Pigsteel ----- //
        String pigsteel = "pigsteel";
        // Pigsteel ore
        this.modIntegration(pigsteel, "porkslag", 3, 0.3, 0.35)
            .itemDrop(3, "iron_nugget")
            .itemDrop(3, 0.3, "iron_nugget")
            .itemDrop(1, 2, 0.4, "iron_nugget");

        // ----- Powah ----- //
        String powah = "powah";
        // Unraninite ore poor
        this.modIntegration(powah, "uraninite_ore_poor", 2, 0.15, 0.2)
            .baseBlock("deepslate_uraninite_ore_poor")
            .itemDrop(2, 0.7, "uraninite")
            .itemDrop(1, 0.1, "uraninite");
        // Unraninite ore
        this.modIntegration(powah, "uraninite_ore", 3, 0.15, 0.2)
            .baseBlock("deepslate_uraninite_ore")
            .itemDrop(3, "uraninite")
            .itemDrop(3, 0.8, "uraninite")
            .itemDrop(2, 0.9, "uraninite")
            .itemDrop(1, 0.2, "uraninite");
        // Unraninite ore dense
        this.modIntegration(powah, "uraninite_ore_dense", 4, 0.15, 0.2)
            .baseBlock("deepslate_uraninite_ore_dense")
            .itemDrop(4, "uraninite", 3)
            .itemDrop(4, 0.3, "uraninite")
            .itemDrop(3, "uraninite")
            .itemDrop(3, 0.6, "uraninite")
            .itemDrop(2, 0.7, "uraninite")
            .itemDrop(1, 0.3, "uraninite");

        // ----- RFTools ----- //
        String rftools = "rftoolsbase";
        // Dimensional shard ore
        this.modIntegration(rftools, "dimensionalshard_overworld", 4, 0.15, 0.2)
            .baseBlock("dimensionalshard_nether").baseBlock("dimensionalshard_end")
            .itemDrop(4, "dimensionalshard")
            .itemDrop(4, 0.8, "dimensionalshard")
            .itemDrop(3, "dimensionalshard")
            .itemDrop(1, 2, 0.6, "dimensionalshard");

        // ----- Spelunkery ----- //
        String spelunkery = "spelunkery";
        // Coal ore
        this.modIntegration(spelunkery, "andesite_coal_ore", 4, 0.3, 0.4)
            .baseBlock("diorite_coal_ore").baseBlock("granite_coal_ore").baseBlock("tuff_coal_ore")
            .itemDrop(4, Items.COAL)
            .itemDrop(1, 3, 0.1, Items.COAL);
        // Iron ore
        this.modIntegration(spelunkery, "andesite_iron_ore", 4, 0.2, 0.25)
            .baseBlock("diorite_iron_ore").baseBlock("granite_iron_ore").baseBlock("tuff_iron_ore")
            .itemDrop(4, Items.RAW_IRON)
            .itemDrop(4, 0.1, Items.IRON_NUGGET)
            .itemDrop(1, 3, 0.1, Items.RAW_IRON);
        // Copper ore
        this.modIntegration(spelunkery, "andesite_copper_ore", 4, 0.2, 0.3)
            .baseBlock("diorite_copper_ore").baseBlock("granite_copper_ore").baseBlock("tuff_copper_ore")
            .itemDrop(4, Items.RAW_COPPER)
            .itemDrop(1, 3, 0.1, Items.RAW_COPPER);
        // Gold ore
        this.modIntegration(spelunkery, "minecraft:nether_gold_ore", 3, 0.3, 0.5)
            .itemDrop(3, "raw_gold_nugget")
            .itemDrop(3, 0.3, "raw_gold_nugget")
            .itemDrop(2, "raw_gold_nugget")
            .itemDrop(1, 0.7, "raw_gold_nugget");
        this.modIntegration(spelunkery, "andesite_gold_ore", 4, 0.2, 0.2)
            .baseBlock("diorite_gold_ore").baseBlock("granite_gold_ore").baseBlock("tuff_gold_ore")
            .itemDrop(4, Items.RAW_GOLD)
            .itemDrop(4, 0.1, Items.GOLD_NUGGET);
        // Redstone ore
        this.modIntegration(spelunkery, "minecraft:redstone_ore", 4, 0.3, 0.3)
            .baseBlock("minecraft:deepslate_redstone_ore").baseBlock("andesite_redstone_ore").baseBlock("diorite_redstone_ore").baseBlock("granite_redstone_ore").baseBlock("tuff_redstone_ore").baseBlock("calcite_redstone_ore")
            .itemDrop(4, "rough_cinnabar")
            .itemDrop(4, 0.4, "rough_cinnabar")
            .itemDrop(3, 0.8, "rough_cinnabar")
            .itemDrop(1, 2, 0.4, "rough_cinnabar");
        // Emerald ore
        this.modIntegration(spelunkery, "minecraft:emerald_ore", 4, 0.1, 0.15)
            .baseBlock("minecraft:deepslate_emerald_ore").baseBlock("andesite_emerald_ore").baseBlock("diorite_emerald_ore").baseBlock("granite_emerald_ore").baseBlock("tuff_emerald_ore")
            .itemDrop(4, "rough_emerald")
            .itemDrop(1, 3, 0.1, "rough_emerald");
        // Lapis ore
        this.modIntegration(spelunkery, "minecraft:lapis_ore", 4, 0.3, 0.3)
            .baseBlock("minecraft:deepslate_lapis_ore").baseBlock("andesite_lapis_ore").baseBlock("diorite_lapis_ore").baseBlock("granite_lapis_ore").baseBlock("tuff_lapis_ore").baseBlock("sandstone_lapis_ore")
            .itemDrop(4, "rough_lazurite")
            .itemDrop(4, 0.2, "rough_lazurite")
            .itemDrop(3, 0.6, "rough_lazurite")
            .itemDrop(1, 2, 0.3, "rough_lazurite");
        // Diamond ore
        this.modIntegration(spelunkery, "minecraft:diamond_ore", 4, 0.1, 0.1)
            .baseBlock("minecraft:deepslate_diamond_ore").baseBlock("andesite_diamond_ore").baseBlock("diorite_diamond_ore").baseBlock("granite_diamond_ore").baseBlock("tuff_diamond_ore").baseBlock("smooth_basalt_diamond_ore")
            .itemDrop(4, "rough_diamond")
            .itemDrop(1, 3, 0.1, "rough_diamond");

        // ----- The Aether ----- //
        String aether = "aether";
        // Ambrosium ore
        this.modIntegration(aether, "ambrosium_ore", 3, 0.15, 0.15)
            .itemDrop(3, "ambrosium_shard")
            .itemDrop(1, 2, 0.3, "ambrosium_shard");
        // Zanite ore
        this.modIntegration(aether, "zanite_ore", 3, 0.15, 0.15)
            .itemDrop(3, "zanite_gemstone")
            .itemDrop(1, 2, 0.3, "zanite_gemstone");
        // Gravitite ore
        this.modIntegration(aether, "gravitite_ore", 4, 0.1, 0.1)
            .itemDrop(4, 0.1, "enchanted_gravitite")
            .itemDrop(1, 3, 0.02, "enchanted_gravitite");

        // ----- Thermal Foundation ----- //
        String thermalFoundation = "thermal";
        // Apatite ore
        this.modIntegration(thermalFoundation, "apatite_ore", 3, 0.15, 0.3)
            .baseBlock("deepslate_apatite_ore")
            .itemDrop(3, "apatite")
            .itemDrop(3, 0.05, "apatite_dust")
            .itemDrop(1, 2, 0.4, "apatite");
        // Niter ore
        this.modIntegration(thermalFoundation, "niter_ore", 2, 0.15, 0.2)
            .baseBlock("deepslate_niter_ore")
            .itemDrop(2, "niter")
            .itemDrop(2, 0.05, "niter_dust")
            .itemDrop(1, 0.4, "niter");
        // Sulfur ore
        this.modIntegration(thermalFoundation, "sulfur_ore", 2, 0.15, 0.2)
            .baseBlock("deepslate_sulfur_ore")
            .itemDrop(2, "sulfur")
            .itemDrop(2, 0.05, "sulfur_dust")
            .itemDrop(1, 0.4, "sulfur");
        // Tin ore
        this.modIntegration(thermalFoundation, "tin_ore", 4, 0.2, 0.25)
            .baseBlock("deepslate_tin_ore")
            .itemDrop(4, "raw_tin")
            .itemDrop(4, 0.1, "tin_nugget")
            .itemDrop(1, 3, 0.1, "raw_tin");
        // Lead ore
        this.modIntegration(thermalFoundation, "lead_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_lead_ore")
            .itemDrop(4, "raw_lead")
            .itemDrop(4, 0.1, "lead_nugget")
            .itemDrop(1, 3, 0.1, "raw_lead");
        // Silver ore
        this.modIntegration(thermalFoundation, "silver_ore", 4, 0.25, 0.25)
            .baseBlock("deepslate_silver_ore")
            .itemDrop(4, "raw_silver")
            .itemDrop(4, 0.1, "silver_nugget")
            .itemDrop(1, 3, 0.1, "raw_silver");
        // Nickel ore
        this.modIntegration(thermalFoundation, "nickel_ore", 4, 0.2, 0.2)
            .baseBlock("deepslate_nickel_ore")
            .itemDrop(4, "raw_nickel")
            .itemDrop(4, 0.1, "nickel_nugget")
            .itemDrop(1, 3, 0.1, "raw_nickel");
        // Cinnabar
        this.modIntegration(thermalFoundation, "cinnabar_ore", 3, 0.2, 0.2)
            .baseBlock("deepslate_cinnabar_ore")
            .itemDrop(3, "cinnabar")
            .itemDrop(3, 0.05, "cinnabar_dust")
            .itemDrop(1, 2, 0.4, "cinnabar");
        // Ruby ore
        this.modIntegration(thermalFoundation, "ruby_ore", 4, 0.1, 0.1)
            .baseBlock("deepslate_ruby_ore")
            .itemDrop(4, "ruby")
            .itemDrop(1, 3, 0.1, "ruby");
        // Sapphire ore
        this.modIntegration(thermalFoundation, "sapphire_ore", 4, 0.15, 0.15)
            .baseBlock("deepslate_sapphire_ore")
            .itemDrop(4, "sapphire")
            .itemDrop(1, 3, 0.1, "sapphire");
    }
}
