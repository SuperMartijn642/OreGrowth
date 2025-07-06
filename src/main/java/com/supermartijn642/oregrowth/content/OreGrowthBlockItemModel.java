package com.supermartijn642.oregrowth.content;

import com.supermartijn642.oregrowth.OreGrowthClient;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.item.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created 06/07/2025 by SuperMartijn642
 */
public class OreGrowthBlockItemModel implements ItemModel {

    private final RandomSource random = RandomSource.create();

    private final List<ItemTintSource> tints;
    private final Supplier<Vector3f[]> extents;
    private final ModelRenderProperties properties;

    public OreGrowthBlockItemModel(BlockModelWrapper original){
        this.tints = original.tints;
        this.extents = original.extents;
        this.properties = original.properties;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int k){
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        if(stack.hasFoil())
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);

        int tintCount = this.tints.size();
        int[] tints = layer.prepareTintLayers(tintCount);
        for(int i = 0; i < tintCount; i++)
            tints[i] = this.tints.get(i).calculate(stack, level, entity);

        layer.setExtents(this.extents);
        layer.setRenderType(ItemBlockRenderTypes.getRenderType(stack));
        this.properties.applyToLayer(layer, displayContext);
        OreGrowthClient.itemModel.emitItemQuads(layer, this.random);
    }
}
