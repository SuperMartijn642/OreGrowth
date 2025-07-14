package com.supermartijn642.oregrowth;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthClient implements ClientModInitializer {

    public static OreGrowthBlockBakedModel itemModel;

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(OreGrowth.MODID);
        handler.registerBlockModelOverwrite(() -> OreGrowth.ORE_GROWTH_BLOCK, OreGrowthBlockBakedModel::new);
        handler.registerBlockModelOverwrite(() -> OreGrowth.COMPLETE_ORE_GROWTH_BLOCK, OreGrowthBlockBakedModel::new);
        handler.registerModelOverwrite(ResourceLocation.fromNamespaceAndPath(OreGrowth.MODID, "block/ore_growth_stage_4"), OreGrowthBlockBakedModel::new);
        handler.registerModelConsumer(ResourceLocation.fromNamespaceAndPath(OreGrowth.MODID, "block/ore_growth_stage_4"), model -> itemModel = (OreGrowthBlockBakedModel)model);
    }
}
