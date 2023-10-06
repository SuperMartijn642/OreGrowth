package com.supermartijn642.oregrowth;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import net.fabricmc.api.ClientModInitializer;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(OreGrowth.MODID);
        handler.registerBlockModelOverwrite(() -> OreGrowth.ORE_GROWTH_BLOCK, OreGrowthBlockBakedModel::new);
    }
}
