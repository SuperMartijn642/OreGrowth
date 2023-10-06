package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeGenerator;
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
    public void generate(){
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
    }
}
