package com.supermartijn642.oregrowth;

import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthBlockItemModel;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthClient implements ClientModInitializer {

    public static OreGrowthBlockBakedModel itemModel;

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(OreGrowth.MODID);
        handler.registerBlockStateModelOverwrite(() -> OreGrowth.ORE_GROWTH_BLOCK, model -> itemModel = new OreGrowthBlockBakedModel(model));
        handler.registerBlockStateModelOverwrite(() -> OreGrowth.COMPLETE_ORE_GROWTH_BLOCK, OreGrowthBlockBakedModel::new);
        handler.registerItemModelOverwrite(() -> OreGrowth.ORE_GROWTH_ITEM, model -> model instanceof CuboidItemModelWrapper ? new OreGrowthBlockItemModel((CuboidItemModelWrapper)model) : model);
        handler.registerItemModelOverwrite(() -> OreGrowth.COMPLETE_ORE_GROWTH_ITEM, model -> model instanceof CuboidItemModelWrapper ? new OreGrowthBlockItemModel((CuboidItemModelWrapper)model) : model);
    }
}
