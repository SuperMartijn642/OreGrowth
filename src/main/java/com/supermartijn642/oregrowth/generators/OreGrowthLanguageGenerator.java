package com.supermartijn642.oregrowth.generators;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.oregrowth.OreGrowth;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthLanguageGenerator extends LanguageGenerator {

    public OreGrowthLanguageGenerator(ResourceCache cache){
        super(OreGrowth.MODID, cache, "en_us");
    }

    @Override
    public void generate(){
        this.block(OreGrowth.ORE_GROWTH_BLOCK, "Ore Cluster");
        this.translation("oregrowth.ore_growth_block.adjusted_name", "%s Crystal");
        this.translation("oregrowth.ore_growth_block.growth_hint", "Growth: %s");
        this.translation("oregrowth.jei_category.title", "Crystal Growth");
        this.translation("oregrowth.jei_category.growth", "Growth: %s");
        this.translation("oregrowth.jei_category.growth.range", "Growth: %s - %s");
        this.translation("oregrowth.jei_category.chance", "Chance: %s");
        this.translation("oregrowth.jei_category.conditions", "Requires:");
        this.translation("oregrowth.jei_category.conditions.bullet", " - ");
        this.translation("oregrowth.jei_category.conditions.bullet.spacing", "  ");
        this.translation("oregrowth.jei_category.conditions.none_of", "None of:");
        this.translation("oregrowth.jei_category.conditions.all_of", "All of:");
        this.translation("oregrowth.jei_category.conditions.any_of", "Any of:");
        this.translation("oregrowth.jei_category.conditions.never", "Never");
        this.translation("oregrowth.jei_category.conditions.not", "Not:");
        this.translation("oregrowth.jei_category.conditions.raining", "It is raining");
        this.translation("oregrowth.jei_category.conditions.raining.not", "It is not raining");
        this.translation("oregrowth.jei_category.conditions.thundering", "It is storming");
        this.translation("oregrowth.jei_category.conditions.thundering.not", "It is not storming");
        this.translation("oregrowth.jei_category.conditions.match_tool", "Mined with ");
        this.translation("oregrowth.jei_category.conditions.match_tool.two_items", "%s and %s");
        this.translation("oregrowth.jei_category.conditions.match_tool.more_items", "%s, and %s");
        this.translation("oregrowth.jei_category.conditions.match_tool.tag", "item from tag '%s'");
        this.translation("oregrowth.jei_category.conditions.match_tool.enchanted", " enchanted with %s");
        this.translation("config.jade.plugin_oregrowth.ore_growth_hint", "Show crystal growth progress");
        this.translation("config.jade.plugin_oregrowth.ore_growth_icon", "Replace crystal icon");
    }
}
