package com.supermartijn642.oregrowth;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthBlockItemModel;
import net.minecraft.client.renderer.item.BlockModelWrapper;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthClient {

    public static OreGrowthBlockBakedModel itemModel;

    public static void initializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(OreGrowth.MODID);
        handler.registerBlockModelOverwrite(() -> OreGrowth.ORE_GROWTH_BLOCK, model -> itemModel = new OreGrowthBlockBakedModel(model));
        handler.registerItemModelOverwrite(() -> OreGrowth.ORE_GROWTH_ITEM, model -> model instanceof BlockModelWrapper ? new OreGrowthBlockItemModel((BlockModelWrapper)model) : model);
    }
}
