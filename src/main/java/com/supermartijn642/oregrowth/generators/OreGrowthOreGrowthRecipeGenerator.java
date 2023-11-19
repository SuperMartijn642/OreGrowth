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
    public OreGrowthRecipeBuilder recipe(String namespace, String location, ResourceLocation base, int stages, double spawnChance, double growthChance, ResourceLocation resultItem, int resultCount){
        return super.recipe(namespace, location, base, stages, spawnChance, growthChance, resultItem, resultCount)
            .condition(new OreGrowthDefaultRecipeCondition());
    }

    @Override
    public void generate(){
        // Some vanilla recipes need to be disabled when Spelunkery is installed
        ResourceCondition noSpelunkery = new ModLoadedResourceCondition("spelunkery").negate();

        // ----- Vanilla -----
        // Coal ore
        this.recipe("coal_ore_growth", Blocks.COAL_ORE, 4, 0.3, 0.4, Items.COAL);
        this.recipe("deepslate_coal_ore_growth", Blocks.DEEPSLATE_COAL_ORE, 4, 0.3, 0.4, Items.COAL);
        // Iron ore
        this.recipe("iron_ore_growth", Blocks.IRON_ORE, 4, 0.2, 0.25, Items.RAW_IRON);
        this.recipe("deepslate_iron_ore_growth", Blocks.DEEPSLATE_IRON_ORE, 4, 0.2, 0.25, Items.RAW_IRON);
        // Copper ore
        this.recipe("copper_ore_growth", Blocks.COPPER_ORE, 4, 0.2, 0.3, Items.RAW_COPPER);
        this.recipe("deepslate_copper_ore_growth", Blocks.DEEPSLATE_COPPER_ORE, 4, 0.2, 0.3, Items.RAW_COPPER);
        // Gold ore
        this.recipe("gold_ore_growth", Blocks.GOLD_ORE, 4, 0.2, 0.2, Items.RAW_GOLD);
        this.recipe("deepslate_gold_ore_growth", Blocks.DEEPSLATE_GOLD_ORE, 4, 0.2, 0.2, Items.RAW_GOLD);
        this.recipe("nether_gold_ore_growth", Blocks.NETHER_GOLD_ORE, 4, 0.3, 0.5, Items.GOLD_NUGGET).condition(noSpelunkery);
        // Lapis ore
        this.recipe("lapis_ore_growth", Blocks.LAPIS_ORE, 4, 0.3, 0.3, Items.LAPIS_LAZULI).condition(noSpelunkery);
        this.recipe("deepslate_lapis_ore_growth", Blocks.DEEPSLATE_LAPIS_ORE, 4, 0.3, 0.3, Items.LAPIS_LAZULI).condition(noSpelunkery);
        // Redstone ore
        this.recipe("redstone_ore_growth", Blocks.REDSTONE_ORE, 4, 0.3, 0.3, Items.REDSTONE).condition(noSpelunkery);
        this.recipe("deepslate_redstone_ore_growth", Blocks.DEEPSLATE_REDSTONE_ORE, 4, 0.3, 0.3, Items.REDSTONE).condition(noSpelunkery);
        // Emerald ore
        this.recipe("emerald_ore_growth", Blocks.EMERALD_ORE, 4, 0.1, 0.15, Items.EMERALD).condition(noSpelunkery);
        this.recipe("deepslate_emerald_ore_growth", Blocks.DEEPSLATE_EMERALD_ORE, 4, 0.1, 0.15, Items.EMERALD).condition(noSpelunkery);
        // Diamond ore
        this.recipe("diamond_ore_growth", Blocks.DIAMOND_ORE, 4, 0.1, 0.1, Items.DIAMOND).condition(noSpelunkery);
        this.recipe("deepslate_diamond_ore_growth", Blocks.DEEPSLATE_DIAMOND_ORE, 4, 0.1, 0.1, Items.DIAMOND).condition(noSpelunkery);
        // Quartz ore
        this.recipe("nether_quartz_ore_growth", Blocks.NETHER_QUARTZ_ORE, 4, 0.2, 0.3, Items.QUARTZ);
        // Netherite scrap
        this.recipe("ancient_debris_growth", Blocks.ANCIENT_DEBRIS, 4, 0.1, 0.05, Items.NETHERITE_SCRAP);

        // ----- Additional Blocks: Stone Edition -----
        String abStoneEdition = "abstoneedition";
        // Silver ore
        this.modIntegration(abStoneEdition, "silver_ore", 4, 0.25, 0.25, "raw_silver");
        // Bismuth ore
        this.modIntegration(abStoneEdition, "bismuth_ore", 4, 0.1, 0.05, "raw_bismuth_chunk");
        // Uranium ore
        this.modIntegration(abStoneEdition, "uranium_ore", 4, 0.15, 0.2, "raw_uranium");

        // ----- BetterEnd -----
        String betterEnd = "betterend";
        // Ender ore
        this.modIntegration(betterEnd, "ender_ore", 4, 0.15, 0.15, "ender_shard");
        // Amber ore
        this.modIntegration(betterEnd, "amber_ore", 4, 0.2, 0.15, "raw_amber");
        // Thallasium ore
        this.modIntegration(betterEnd, "thallasium_ore", 4, 0.1, 0.15, "thallasium_raw");

        // ----- BetterNether -----
        String betterNether = "betternether";
        // Cincinnasite ore
        this.modIntegration(betterNether, "cincinnasite_ore", 4, 0.15, 0.15, "cincinnasite");
        // Nether ruby ore
        this.modIntegration(betterNether, "nether_ruby_ore", 4, 0.1, 0.1, "nether_ruby");
        // Nether lapis ore
        this.modIntegration(betterNether, "nether_lapis_ore", 2, 0.25, 0.25, "lapis_pile");
        // Nether redstone ore
        this.modIntegration(betterNether, "nether_redstone_ore", 2, 0.2, 0.2, Items.REDSTONE);

        // ----- Bigger Reactors -----
        String biggerReactors = "biggerreactors";
        // Uranium ore
        this.modIntegration(biggerReactors, "uranium_ore", 4, 0.15, 0.2, "uranium_chunk");

        // ----- Create -----
        String create = "create";
        // Zinc ore
        this.modIntegration(create, "zinc_ore", 4, 0.2, 0.25, "raw_zinc");
        // Deepslate zinc ore
        this.modIntegration(create, "deepslate_zinc_ore", 4, 0.2, 0.25, "raw_zinc");

        // ----- Deep Aether -----
        String deepAether = "deep_aether";
        // Skyjade ore
        this.modIntegration(deepAether, "skyjade_ore", 4, 0.1, 0.15, "skyjade");

        // ----- Extreme Reactors -----
        String extremeReactors = "bigreactors";
        // Yellorite ore
        this.modIntegration(extremeReactors, "yellorite_ore", 4, 0.15, 0.2, "yellorium_nugget");
        // Anglesite ore
        this.modIntegration(extremeReactors, "anglesite_ore", 4, 0.1, 0.1, "anglesite_crystal");
        // Benitoite ore
        this.modIntegration(extremeReactors, "benitoite_ore", 4, 0.1, 0.1, "benitoite_crystal");

        // ----- Industrial Revolution -----
        String industrialRevolution = "indrev";
        // Tungsten ore
        this.modIntegration(industrialRevolution, "tungsten_ore", 4, 0.15, 0.2, "raw_tungsten");
        // Deepslate tungsten ore
        this.modIntegration(industrialRevolution, "deepslate_tungsten_ore", 4, 0.15, 0.2, "raw_tungsten");
        // Tin ore
        this.modIntegration(industrialRevolution, "tin_ore", 4, 0.2, 0.25, "raw_tin");
        // Deepslate tin ore
        this.modIntegration(industrialRevolution, "deepslate_tin_ore", 4, 0.2, 0.25, "raw_tin");
        // Silver ore
        this.modIntegration(industrialRevolution, "silver_ore", 4, 0.25, 0.25, "raw_silver");
        // Deepslate silver ore
        this.modIntegration(industrialRevolution, "deepslate_silver_ore", 4, 0.25, 0.25, "raw_silver");
        // Nikolite ore
        this.modIntegration(industrialRevolution, "nikolite_ore", 4, 0.3, 0.35, "nikolite_dust");
        // deepslate nikolite ore
        this.modIntegration(industrialRevolution, "deepslate_nikolite_ore", 4, 0.3, 0.35, "nikolite_dust");
        // Lead ore
        this.modIntegration(industrialRevolution, "lead_ore", 4, 0.2, 0.2, "raw_lead");
        // Deepslate lead ore
        this.modIntegration(industrialRevolution, "deepslate_lead_ore", 4, 0.2, 0.2, "raw_lead");

        // ----- Gems & Jewels -----
        String gemsNJewels = "gemsnjewels";
        // Pale diamond ore block
        this.modIntegration(gemsNJewels, "pale_diamond_ore_block", 4, 0.1, 0.1, "pale_diamond");
        // Deepslate pale diamond ore block
        this.modIntegration(gemsNJewels, "pale_diamond_deepslate_ore_block", 4, 0.1, 0.1, "pale_diamond");
        // Pale diamond nether ore block
        this.modIntegration(gemsNJewels, "pale_diamond_nether_ore_block", 4, 0.1, 0.1, "pale_diamond");
        // Dusk emerald ore block
        this.modIntegration(gemsNJewels, "dusk_emerald_ore_block", 4, 0.1, 0.15, "emerald");
        // Deepslate dusk emerald ore block
        this.modIntegration(gemsNJewels, "emerald_deepslate_ore_block", 4, 0.1, 0.15, "emerald");
        // Dusk emerald nether ore block
        this.modIntegration(gemsNJewels, "emerald_nether_ore_block", 4, 0.1, 0.15, "emerald");
        // Ruby ore block
        this.modIntegration(gemsNJewels, "ruby_ore_block", 4, 0.1, 0.1, "ruby");
        // Deepslate ruby ore block
        this.modIntegration(gemsNJewels, "ruby_deepslate_ore_block", 4, 0.1, 0.1, "ruby");
        // Ruby nether ore block
        this.modIntegration(gemsNJewels, "ruby_nether_ore_block", 4, 0.1, 0.1, "ruby");
        // Sapphire ore block
        this.modIntegration(gemsNJewels, "sapphire_ore_block", 4, 0.15, 0.15, "sapphire");
        // Deepslate sapphire ore block
        this.modIntegration(gemsNJewels, "sapphire_deepslate_ore_block", 4, 0.15, 0.15, "sapphire");
        // Sapphire nether ore block
        this.modIntegration(gemsNJewels, "sapphire_nether_ore_block", 4, 0.15, 0.15, "sapphire");
        // Amethyst ore block
        this.modIntegration(gemsNJewels, "amethyst_ore_block", 4, 0.15, 0.15, "amethyst");
        // Deepslate amethyst ore block
        this.modIntegration(gemsNJewels, "amethyst_deepslate_ore_block", 4, 0.15, 0.15, "amethyst");
        // Amethyst nether ore block
        this.modIntegration(gemsNJewels, "amethyst_nether_ore_block", 4, 0.15, 0.15, "amethyst");
        // Opal ore block
        this.modIntegration(gemsNJewels, "opal_ore_block", 4, 0.2, 0.15, "opal");
        // Bright opal ore block
        this.modIntegration(gemsNJewels, "bright_opal_ore_block", 4, 0.2, 0.15, "opal");
        // Deepslate opal ore block
        this.modIntegration(gemsNJewels, "opal_deepslate_ore_block", 4, 0.2, 0.15, "opal");
        // Opal nether ore block
        this.modIntegration(gemsNJewels, "opal_nether_ore_block", 4, 0.2, 0.15, "opal");
        // Garnet ore block
        this.modIntegration(gemsNJewels, "garnet_ore_block", 4, 0.15, 0.15, "garnet");
        // Deepslate garnet ore block
        this.modIntegration(gemsNJewels, "garnet_deepslate_ore_block", 4, 0.15, 0.15, "garnet");
        // Garnet nether ore block
        this.modIntegration(gemsNJewels, "garnet_nether_ore_block", 4, 0.15, 0.15, "garnet");
        // Topaz ore block
        this.modIntegration(gemsNJewels, "topaz_ore_block", 4, 0.1, 0.1, "topaz");
        // Deepslate topaz ore block
        this.modIntegration(gemsNJewels, "topaz_deepslate_ore_block", 4, 0.1, 0.1, "topaz");
        // Topaz nether ore block
        this.modIntegration(gemsNJewels, "topaz_nether_ore_block", 4, 0.1, 0.1, "topaz");
        // Peridot ore block
        this.modIntegration(gemsNJewels, "peridot_ore_block", 4, 0.15, 0.15, "peridot");
        // Deepslate peridot ore block
        this.modIntegration(gemsNJewels, "peridot_deepslate_ore_block", 4, 0.15, 0.15, "peridot");
        // Peridot nether ore block
        this.modIntegration(gemsNJewels, "peridot_nether_ore_block", 4, 0.15, 0.15, "peridot");
        // Aquamarine ore block
        this.modIntegration(gemsNJewels, "aquamarine_ore_block", 4, 0.15, 0.15, "aquamarine");
        // Deepslate aquamarine ore block
        this.modIntegration(gemsNJewels, "aquamarine_deepslate_ore_block", 4, 0.15, 0.15, "aquamarine");
        // Aquamarine nether ore block
        this.modIntegration(gemsNJewels, "aquamarine_nether_ore_block", 4, 0.15, 0.15, "aquamarine");
        // Zircon ore block
        this.modIntegration(gemsNJewels, "zircon_ore_block", 4, 0.15, 0.15, "zircon");
        // Deepslate zircon ore block
        this.modIntegration(gemsNJewels, "zircon_deepslate_ore_block", 4, 0.15, 0.15, "zircon");
        // Zircon nether ore block
        this.modIntegration(gemsNJewels, "zircon_nether_ore_block", 4, 0.15, 0.15, "zircon");
        // Alexandrite ore block
        this.modIntegration(gemsNJewels, "alexandrite_ore_block", 4, 0.15, 0.15, "alexandrite");
        // Deepslate alexandrite ore block
        this.modIntegration(gemsNJewels, "alexandrite_deepslate_ore_block", 4, 0.15, 0.15, "alexandrite");
        // Alexandrite nether ore block
        this.modIntegration(gemsNJewels, "alexandrite_nether_ore_block", 4, 0.15, 0.15, "alexandrite");
        // Tanzanite ore block
        this.modIntegration(gemsNJewels, "tanzanite_ore_block", 4, 0.2, 0.15, "tanzanite");
        // Deepslate tanzanite ore block
        this.modIntegration(gemsNJewels, "tanzanite_deepslate_ore_block", 4, 0.2, 0.15, "tanzanite");
        // Tanzanite nether ore block
        this.modIntegration(gemsNJewels, "tanzanite_nether_ore_block", 4, 0.2, 0.15, "tanzanite");
        // Tourmaline ore block
        this.modIntegration(gemsNJewels, "tourmaline_ore_block", 4, 0.15, 0.15, "tourmaline");
        // Deepslate tourmaline ore block
        this.modIntegration(gemsNJewels, "tourmaline_deepslate_ore_block", 4, 0.15, 0.15, "tourmaline");
        // Tourmaline nether ore block
        this.modIntegration(gemsNJewels, "tourmaline_nether_ore_block", 4, 0.15, 0.15, "tourmaline");
        // Spinel ore block
        this.modIntegration(gemsNJewels, "spinel_ore_block", 4, 0.1, 0.1, "spinel");
        // Deepslate spinel ore block
        this.modIntegration(gemsNJewels, "spinel_deepslate_ore_block", 4, 0.1, 0.1, "spinel");
        // Spinel nether ore block
        this.modIntegration(gemsNJewels, "spinel_nether_ore_block", 4, 0.1, 0.1, "spinel");
        // Black opal ore block
        this.modIntegration(gemsNJewels, "black_opal_ore_block", 4, 0.2, 0.15, "black_opal");
        // Deepslate black opal ore block
        this.modIntegration(gemsNJewels, "black_opal_deepslate_ore_block", 4, 0.2, 0.15, "black_opal");
        // Black opal nether ore block
        this.modIntegration(gemsNJewels, "black_opal_nether_ore_block", 4, 0.2, 0.15, "black_opal");
        // Citrine ore block
        this.modIntegration(gemsNJewels, "citrine_ore_block", 4, 0.15, 0.15, "citrine");
        // Deepslate citrine ore block
        this.modIntegration(gemsNJewels, "citrine_deepslate_ore_block", 4, 0.15, 0.15, "citrine");
        // Citrine nether ore block
        this.modIntegration(gemsNJewels, "citrine_nether_ore_block", 4, 0.15, 0.15, "citrine");
        // Morganite ore block
        this.modIntegration(gemsNJewels, "morganite_ore_block", 4, 0.15, 0.15, "morganite");
        // Deepslate morganite ore block
        this.modIntegration(gemsNJewels, "morganite_deepslate_ore_block", 4, 0.15, 0.15, "morganite");
        // Morganite nether ore block
        this.modIntegration(gemsNJewels, "morganite_nether_ore_block", 4, 0.15, 0.15, "morganite");
        // Ametrine ore block
        this.modIntegration(gemsNJewels, "ametrine_ore_block", 4, 0.15, 0.15, "ametrine");
        // Deepslate ametrine ore block
        this.modIntegration(gemsNJewels, "ametrine_deepslate_ore_block", 4, 0.15, 0.15, "ametrine");
        // Ametrine nether ore block
        this.modIntegration(gemsNJewels, "ametrine_nether_ore_block", 4, 0.15, 0.15, "ametrine");
        // Kunzite ore block
        this.modIntegration(gemsNJewels, "kunzite_ore_block", 4, 0.15, 0.15, "kunzite");
        // Deepslate kunzite ore block
        this.modIntegration(gemsNJewels, "kunzite_deepslate_ore_block", 4, 0.15, 0.15, "kunzite");
        // Kunzite nether ore block
        this.modIntegration(gemsNJewels, "kunzite_nether_ore_block", 4, 0.15, 0.15, "kunzite");
        // Iolite ore block
        this.modIntegration(gemsNJewels, "iolite_ore_block", 4, 0.15, 0.15, "iolite");
        // Deepslate iolite ore block
        this.modIntegration(gemsNJewels, "iolite_deepslate_ore_block", 4, 0.15, 0.15, "iolite");
        // Iolite nether ore block
        this.modIntegration(gemsNJewels, "iolite_nether_ore_block", 4, 0.15, 0.15, "iolite");
        // Diamond nether ore block
        this.modIntegration(gemsNJewels, "diamond_nether_ore_block", 4, 0.1, 0.1, Items.DIAMOND);
        // Emerald nether ore block
        this.modIntegration(gemsNJewels, "v_emerald_nether_ore_block", 4, 0.1, 0.15, Items.EMERALD);

        // ----- Mekanism -----
        String mekanism = "mekanism";
        // Tin ore
        this.modIntegration(mekanism, "tin_ore", 4, 0.2, 0.25, "raw_tin");
        // Deepslate tin ore
        this.modIntegration(mekanism, "deepslate_tin_ore", 4, 0.2, 0.25, "raw_tin");
        // Osmium ore
        this.modIntegration(mekanism, "osmium_ore", 4, 0.15, 0.2, "raw_osmium");
        // Deepslate osmium ore
        this.modIntegration(mekanism, "deepslate_osmium_ore", 4, 0.15, 0.2, "raw_osmium");
        // Uranium ore
        this.modIntegration(mekanism, "uranium_ore", 4, 0.15, 0.2, "raw_uranium");
        // Deepslate uranium ore
        this.modIntegration(mekanism, "deepslate_uranium_ore", 4, 0.15, 0.2, "raw_uranium");
        // Fluorite ore
        this.modIntegration(mekanism, "fluorite_ore", 4, 0.3, 0.4, "fluorite_gem");
        // Deepslate fluorite ore
        this.modIntegration(mekanism, "deepslate_fluorite_ore", 4, 0.3, 0.4, "fluorite_gem");
        // Lead ore
        this.modIntegration(mekanism, "lead_ore", 4, 0.2, 0.2, "raw_lead");
        // Deepslate lead ore
        this.modIntegration(mekanism, "deepslate_lead_ore", 4, 0.2, 0.2, "raw_lead");

        // More Gems [FABRIC]
        String moreGems = "more_gems";
        // Citrine ore
        this.modIntegration(moreGems, "citrine_ore", 4, 0.15, 0.15, "citrine");
        // Tourmaline ore
        this.modIntegration(moreGems, "tourmaline_ore", 4, 0.15, 0.15, "tourmaline");
        // Kunzite ore
        this.modIntegration(moreGems, "kunzite_ore", 4, 0.15, 0.15, "kunzite");
        // Nether kunzite ore
        this.modIntegration(moreGems, "kunzite_ore_nether", 4, 0.15, 0.15, "kunzite");
        // Topas ore
        this.modIntegration(moreGems, "topas_ore", 4, 0.15, 0.15, "topas");
        // Alexandrite ore
        this.modIntegration(moreGems, "alexandrite_ore", 4, 0.15, 0.15, "alexandrite");
        // Nether Alexandrite ore
        this.modIntegration(moreGems, "alexandrite_ore_nether", 4, 0.15, 0.15, "alexandrite");
        // Corundum ore
        this.modIntegration(moreGems, "corundum_ore", 4, 0.15, 0.15, "corundum");
        // Nether corundum ore
        this.modIntegration(moreGems, "corundum_ore_nether", 4, 0.15, 0.15, "corundum");
        // Sapphire ore
        this.modIntegration(moreGems, "sapphire_ore", 4, 0.15, 0.15, "sapphire");
        // Deepslate sapphire ore
        this.modIntegration(moreGems, "sapphire_ore_deepslate", 4, 0.15, 0.15, "sapphire");
        // Spinal ore
        this.modIntegration(moreGems, "spinal_ore", 4, 0.15, 0.15, "spinal");
        // Deepslate spinal ore
        this.modIntegration(moreGems, "spinal_ore_deepslate", 4, 0.15, 0.15, "spinal");
        // Carbonado ore
        this.modIntegration(moreGems, "carbonado_ore", 4, 0.15, 0.15, "carbonado");
        // Deepslate carbonado ore
        this.modIntegration(moreGems, "carbonado_ore_deepslate", 4, 0.15, 0.15, "carbonado");
        // Moissanite ore
        this.modIntegration(moreGems, "moissanite_ore", 4, 0.1, 0.1, "moissanite");

        // ----- Mystical Agriculture -----
        String mysticalAgriculture = "mysticalagriculture";
        // Prosperity ore
        this.modIntegration(mysticalAgriculture, "prosperity_ore", 4, 0.2, 0.2, "prosperity_shard");
        // Deepslate prosperity ore
        this.modIntegration(mysticalAgriculture, "deepslate_prosperity_ore", 4, 0.2, 0.2, "prosperity_shard");
        // Inferium ore
        this.modIntegration(mysticalAgriculture, "inferium_ore", 4, 0.3, 0.4, "inferium_essence");
        // Deepslate inferium ore
        this.modIntegration(mysticalAgriculture, "deepslate_inferium_ore", 4, 0.3, 0.4, "inferium_essence");
        // Soulium ore
        this.modIntegration(mysticalAgriculture, "soulium_ore", 4, 0.2, 0.2, "soulium_dust");

        // ----- Mythic Metals -----
        String mythicMetals = "mythicmetals";
        // Adamantite ore
        this.modIntegration(mythicMetals, "adamantite_ore", 4, 0.2, 0.2, "raw_adamantite");
        // Deepslate adamantite ore
        this.modIntegration(mythicMetals, "deepslate_adamantite_ore", 4, 0.2, 0.2, "raw_adamantite");
        // Aquarium ore
        this.modIntegration(mythicMetals, "aquarium_ore", 4, 0.2, 0.2, "raw_aquarium");
        // Banglum ore
        this.modIntegration(mythicMetals, "banglum_ore", 4, 0.2, 0.2, "raw_banglum");
        // Nether banglum ore
        this.modIntegration(mythicMetals, "nether_banglum_ore", 4, 0.2, 0.2, "raw_banglum");
        // Carmot ore
        this.modIntegration(mythicMetals, "carmot_ore", 4, 0.2, 0.2, "raw_carmot");
        // Deepslate carmot ore
        this.modIntegration(mythicMetals, "deepslate_carmot_ore", 4, 0.2, 0.2, "raw_carmot");
        // Kyber ore
        this.modIntegration(mythicMetals, "kyber_ore", 4, 0.2, 0.2, "raw_kyber");
        // Calcite kyber ore
        this.modIntegration(mythicMetals, "calcite_kyber_ore", 4, 0.2, 0.2, "raw_kyber");
        // Manganese ore
        this.modIntegration(mythicMetals, "manganese_ore", 4, 0.2, 0.2, "raw_manganese");
        // Morkite ore
        this.modIntegration(mythicMetals, "morkite_ore", 4, 0.2, 0.2, "morkite");
        // Deepslate morkite ore
        this.modIntegration(mythicMetals, "deepslate_morkite_ore", 4, 0.2, 0.2, "morkite");
        // Midas gold ore
        this.modIntegration(mythicMetals, "midas_gold_ore", 4, 0.2, 0.2, "raw_midas_gold");
        // Mythril ore
        this.modIntegration(mythicMetals, "mythril_ore", 4, 0.2, 0.2, "raw_mythril");
        // Deepslate mythril ore
        this.modIntegration(mythicMetals, "deepslate_mythril_ore", 4, 0.2, 0.2, "raw_mythril");
        // Orichalcum ore
        this.modIntegration(mythicMetals, "orichalcum_ore", 4, 0.2, 0.2, "raw_orichalcum");
        // Tuff orichalcum ore
        this.modIntegration(mythicMetals, "tuff_orichalcum_ore", 4, 0.2, 0.2, "raw_orichalcum");
        // Smooth basalt orichalcum ore
        this.modIntegration(mythicMetals, "smooth_basalt_orichalcum_ore", 4, 0.2, 0.2, "raw_orichalcum");
        // Deepslate orichalcum ore
        this.modIntegration(mythicMetals, "deepslate_orichalcum_ore", 4, 0.2, 0.2, "raw_orichalcum");
        // Osmium ore
        this.modIntegration(mythicMetals, "osmium_ore", 4, 0.15, 0.2, "raw_osmium");
        // Palladium ore
        this.modIntegration(mythicMetals, "palladium_ore", 4, 0.2, 0.2, "raw_palladium");
        // Platinum ore
        this.modIntegration(mythicMetals, "platinum_ore", 4, 0.2, 0.2, "raw_platinum");
        // Prometheum ore
        this.modIntegration(mythicMetals, "prometheum_ore", 4, 0.2, 0.2, "raw_prometheum");
        // Deepslate prometheum ore
        this.modIntegration(mythicMetals, "deepslate_prometheum_ore", 4, 0.2, 0.2, "raw_prometheum");
        // Quadrillum ore
        this.modIntegration(mythicMetals, "quadrillum_ore", 4, 0.2, 0.2, "raw_quadrillum");
        // Runite ore
        this.modIntegration(mythicMetals, "runite_ore", 4, 0.2, 0.2, "raw_runite");
        // Deepslate runite ore
        this.modIntegration(mythicMetals, "deepslate_runite_ore", 4, 0.2, 0.2, "raw_runite");
        // Silver ore
        this.modIntegration(mythicMetals, "silver_ore", 4, 0.25, 0.25, "raw_silver");
        // Starrite ore
        this.modIntegration(mythicMetals, "starrite_ore", 4, 0.2, 0.2, "starrite");
        // Calcite starrite ore
        this.modIntegration(mythicMetals, "calcite_starrite_ore", 4, 0.2, 0.2, "starrite");
        // End stone starrite ore
        this.modIntegration(mythicMetals, "end_stone_starrite_ore", 4, 0.2, 0.2, "starrite");
        // Stormyx ore
        this.modIntegration(mythicMetals, "stormyx_ore", 4, 0.2, 0.2, "raw_stormyx");
        // Blackstone stormyx ore
        this.modIntegration(mythicMetals, "blackstone_stormyx_ore", 4, 0.2, 0.2, "raw_stormyx");
        // Tin ore
        this.modIntegration(mythicMetals, "tin_ore", 4, 0.2, 0.25, "raw_tin");
        // Unobtanium ore
        this.modIntegration(mythicMetals, "unobtainium_ore", 4, 0.1, 0.05, "unobtainium");
        // Deepslate unobtanium ore
        this.modIntegration(mythicMetals, "deepslate_unobtainium_ore", 4, 0.1, 0.05, "unobtainium");

        // ----- Pigsteel -----
        String pigsteel = "pigsteel";
        // Pigsteel ore
        this.modIntegration(pigsteel, "pigsteel_ore", 3, 0.3, 0.35, "pigsteel_nugget");
        // Stone pigsteel ore
        this.modIntegration(pigsteel, "stone_pigsteel_ore", 3, 0.3, 0.35, "pigsteel_nugget");
        // Deepslate pigsteel ore
        this.modIntegration(pigsteel, "deepslate_pigsteel_ore", 3, 0.3, 0.35, "pigsteel_nugget");

        // ----- Powah -----
        String powah = "powah";
        // Unraninite ore poor
        this.modIntegration(powah, "uraninite_ore_poor", 2, 0.15, 0.2, "uraninite");
        // Unraninite ore
        this.modIntegration(powah, "uraninite_ore", 3, 0.15, 0.2, "uraninite", 2);
        // Unraninite ore dense
        this.modIntegration(powah, "uraninite_ore_dense", 4, 0.15, 0.2, "uraninite", 3);
        // Deepslate unraninite ore poor
        this.modIntegration(powah, "deepslate_uraninite_ore_poor", 2, 0.15, 0.2, "uraninite");
        // Deepslate unraninite ore
        this.modIntegration(powah, "deepslate_uraninite_ore", 3, 0.15, 0.2, "uraninite", 2);
        // Deepslate unraninite ore dense
        this.modIntegration(powah, "deepslate_uraninite_ore_dense", 4, 0.15, 0.2, "uraninite", 3);

        // ----- RFTools -----
        String rftools = "rftoolsbase";
        // Dimensional shard ore
        this.modIntegration(rftools, "dimensionalshard_overworld", 4, 0.15, 0.2, "dimensionalshard");
        this.modIntegration(rftools, "dimensionalshard_nether", 4, 0.15, 0.2, "dimensionalshard");
        this.modIntegration(rftools, "dimensionalshard_end", 4, 0.15, 0.2, "dimensionalshard");

        // ----- Spelunkery -----
        String spelunkery = "spelunkery";
        // Coal ore
        this.modIntegration(spelunkery, "andesite_coal_ore", 4, 0.3, 0.4, Items.COAL);
        this.modIntegration(spelunkery, "diorite_coal_ore", 4, 0.3, 0.4, Items.COAL);
        this.modIntegration(spelunkery, "granite_coal_ore", 4, 0.3, 0.4, Items.COAL);
        this.modIntegration(spelunkery, "tuff_coal_ore", 4, 0.3, 0.4, Items.COAL);
        // Iron ore
        this.modIntegration(spelunkery, "andesite_iron_ore", 4, 0.2, 0.25, Items.RAW_IRON);
        this.modIntegration(spelunkery, "diorite_iron_ore", 4, 0.2, 0.25, Items.RAW_IRON);
        this.modIntegration(spelunkery, "granite_iron_ore", 4, 0.2, 0.25, Items.RAW_IRON);
        this.modIntegration(spelunkery, "tuff_iron_ore", 4, 0.2, 0.25, Items.RAW_IRON);
        // Copper ore
        this.modIntegration(spelunkery, "andesite_copper_ore", 4, 0.2, 0.3, Items.RAW_COPPER);
        this.modIntegration(spelunkery, "diorite_copper_ore", 4, 0.2, 0.3, Items.RAW_COPPER);
        this.modIntegration(spelunkery, "granite_copper_ore", 4, 0.2, 0.3, Items.RAW_COPPER);
        this.modIntegration(spelunkery, "tuff_copper_ore", 4, 0.2, 0.3, Items.RAW_COPPER);
        // Gold ore
        this.modIntegration(spelunkery, "minecraft:nether_gold_ore", 4, 0.3, 0.5, "raw_gold_nugget");
        this.modIntegration(spelunkery, "andesite_gold_ore", 4, 0.2, 0.2, Items.RAW_GOLD);
        this.modIntegration(spelunkery, "diorite_gold_ore", 4, 0.2, 0.2, Items.RAW_GOLD);
        this.modIntegration(spelunkery, "granite_gold_ore", 4, 0.2, 0.2, Items.RAW_GOLD);
        this.modIntegration(spelunkery, "tuff_gold_ore", 4, 0.2, 0.2, Items.RAW_GOLD);
        // Redstone ore
        this.modIntegration(spelunkery, "minecraft:redstone_ore", 4, 0.3, 0.3, "rough_cinnabar");
        this.modIntegration(spelunkery, "minecraft:deepslate_redstone_ore", 4, 0.3, 0.3, "rough_cinnabar");
        this.modIntegration(spelunkery, "andesite_redstone_ore", 4, 0.3, 0.3, "rough_cinnabar");
        this.modIntegration(spelunkery, "diorite_redstone_ore", 4, 0.3, 0.3, "rough_cinnabar");
        this.modIntegration(spelunkery, "granite_redstone_ore", 4, 0.3, 0.3, "rough_cinnabar");
        this.modIntegration(spelunkery, "tuff_redstone_ore", 4, 0.3, 0.3, "rough_cinnabar");
        this.modIntegration(spelunkery, "calcite_redstone_ore", 4, 0.3, 0.3, "rough_cinnabar");
        // Emerald ore
        this.modIntegration(spelunkery, "minecraft:emerald_ore", 4, 0.1, 0.15, "rough_emerald");
        this.modIntegration(spelunkery, "minecraft:deepslate_emerald_ore", 4, 0.1, 0.15, "rough_emerald");
        this.modIntegration(spelunkery, "andesite_emerald_ore", 4, 0.1, 0.15, "rough_emerald");
        this.modIntegration(spelunkery, "diorite_emerald_ore", 4, 0.1, 0.15, "rough_emerald");
        this.modIntegration(spelunkery, "granite_emerald_ore", 4, 0.1, 0.15, "rough_emerald");
        this.modIntegration(spelunkery, "tuff_emerald_ore", 4, 0.1, 0.15, "rough_emerald");
        // Lapis ore
        this.modIntegration(spelunkery, "minecraft:lapis_ore", 4, 0.3, 0.3, "rough_lazurite");
        this.modIntegration(spelunkery, "minecraft:deepslate_lapis_ore", 4, 0.3, 0.3, "rough_lazurite");
        this.modIntegration(spelunkery, "andesite_lapis_ore", 4, 0.3, 0.3, "rough_lazurite");
        this.modIntegration(spelunkery, "diorite_lapis_ore", 4, 0.3, 0.3, "rough_lazurite");
        this.modIntegration(spelunkery, "granite_lapis_ore", 4, 0.3, 0.3, "rough_lazurite");
        this.modIntegration(spelunkery, "tuff_lapis_ore", 4, 0.3, 0.3, "rough_lazurite");
        this.modIntegration(spelunkery, "sandstone_lapis_ore", 4, 0.3, 0.3, "rough_lazurite");
        // Diamond ore
        this.modIntegration(spelunkery, "minecraft:diamond_ore", 4, 0.1, 0.1, "rough_diamond");
        this.modIntegration(spelunkery, "minecraft:deepslate_diamond_ore", 4, 0.1, 0.1, "rough_diamond");
        this.modIntegration(spelunkery, "andesite_diamond_ore", 4, 0.1, 0.1, "rough_diamond");
        this.modIntegration(spelunkery, "diorite_diamond_ore", 4, 0.1, 0.1, "rough_diamond");
        this.modIntegration(spelunkery, "granite_diamond_ore", 4, 0.1, 0.1, "rough_diamond");
        this.modIntegration(spelunkery, "tuff_diamond_ore", 4, 0.1, 0.1, "rough_diamond");
        this.modIntegration(spelunkery, "smooth_basalt_diamond_ore", 4, 0.1, 0.1, "rough_diamond");

        // ----- The Aether -----
        String aether = "aether";
        // Ambrosium ore
        this.modIntegration(aether, "ambrosium_ore", 3, 0.15, 0.15, "ambrosium_shard");
        // Zanite ore
        this.modIntegration(aether, "zanite_ore", 3, 0.15, 0.15, "zanite_gemstone");
        // Gravitite ore
        this.modIntegration(aether, "gravitite_ore", 4, 0.1, 0.1, "enchanted_gravitite");

        // ----- Thermal Foundation -----
        String thermalFoundation = "thermal";
        // Niter ore
        this.modIntegration(thermalFoundation, "niter_ore", 2, 0.15, 0.2, "niter");
        // Deepslate niter ore
        this.modIntegration(thermalFoundation, "deepslate_niter_ore", 2, 0.15, 0.2, "niter");
        // Sulfur ore
        this.modIntegration(thermalFoundation, "sulfur_ore", 2, 0.15, 0.2, "sulfur");
        // Deepslate sulfur ore
        this.modIntegration(thermalFoundation, "deepslate_sulfur_ore", 2, 0.15, 0.2, "sulfur");
        // Tin ore
        this.modIntegration(thermalFoundation, "tin_ore", 4, 0.2, 0.25, "raw_tin");
        // Deepslate tin ore
        this.modIntegration(thermalFoundation, "deepslate_tin_ore", 4, 0.2, 0.25, "raw_tin");
        // Lead ore
        this.modIntegration(thermalFoundation, "lead_ore", 4, 0.2, 0.2, "raw_lead");
        // Deepslate lead ore
        this.modIntegration(thermalFoundation, "deepslate_lead_ore", 4, 0.2, 0.2, "raw_lead");
        // Silver ore
        this.modIntegration(thermalFoundation, "silver_ore", 4, 0.25, 0.25, "raw_silver");
        // Deepslate silver ore
        this.modIntegration(thermalFoundation, "deepslate_silver_ore", 4, 0.25, 0.25, "raw_silver");
        // Nickel ore
        this.modIntegration(thermalFoundation, "nickel_ore", 4, 0.2, 0.2, "raw_nickel");
        // Deepslate nickel ore
        this.modIntegration(thermalFoundation, "deepslate_nickel_ore", 4, 0.2, 0.2, "raw_nickel");
    }
}
