package com.supermartijn642.oregrowth.generators;

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
        this.recipe("nether_gold_ore_growth", Blocks.NETHER_GOLD_ORE, 4, 0.3, 0.5, Items.GOLD_NUGGET);
        // Lapis ore
        this.recipe("lapis_ore_growth", Blocks.LAPIS_ORE, 4, 0.3, 0.3, Items.LAPIS_LAZULI);
        this.recipe("deepslate_lapis_ore_growth", Blocks.DEEPSLATE_LAPIS_ORE, 4, 0.3, 0.3, Items.LAPIS_LAZULI);
        // Redstone ore
        this.recipe("redstone_ore_growth", Blocks.REDSTONE_ORE, 4, 0.3, 0.3, Items.REDSTONE);
        this.recipe("deepslate_redstone_ore_growth", Blocks.DEEPSLATE_REDSTONE_ORE, 4, 0.3, 0.3, Items.REDSTONE);
        // Emerald ore
        this.recipe("emerald_ore_growth", Blocks.EMERALD_ORE, 4, 0.1, 0.15, Items.EMERALD);
        this.recipe("deepslate_emerald_ore_growth", Blocks.DEEPSLATE_EMERALD_ORE, 4, 0.15, 0.1, Items.EMERALD);
        // Diamond ore
        this.recipe("diamond_ore_growth", Blocks.DIAMOND_ORE, 4, 0.1, 0.1, Items.DIAMOND);
        this.recipe("deepslate_diamond_ore_growth", Blocks.DEEPSLATE_DIAMOND_ORE, 4, 0.1, 0.1, Items.DIAMOND);
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

        // ----- Extreme Reactors -----
        String extremeReactors = "bigreactors";
        // Yellorite ore
        this.modIntegration(extremeReactors, "yellorite_ore", 4, 0.15, 0.2, "yellorite_nugget");
        // Anglesite ore
        this.modIntegration(extremeReactors, "anglesite_ore", 4, 0.1, 0.1, "anglesite_crystal");
        // Benitoite ore
        this.modIntegration(extremeReactors, "benitoite_ore", 4, 0.1, 0.1, "benitoite_crystal");

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

        // ----- The Aether -----
        String aether = "aether";
        // Ambrosium ore
        this.modIntegration(aether, "ambrosium_ore", 3, 0.15, 0.15, "ambrosium_shard");
        // Zanite ore
        this.modIntegration(aether, "zanite_ore", 3, 0.15, 0.15, "zanite_gemstone");
        // Gravitite ore
        this.modIntegration(aether, "gravitite_ore", 4, 0.1, 0.1, "enchanted_gravitite");

        // ----- Thermal Foundation -----
        String thermalFoundation = "thermalfoundation";
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

    private static ResourceLocation location(String namespace, String location){
        return new ResourceLocation(namespace, location);
    }
}
