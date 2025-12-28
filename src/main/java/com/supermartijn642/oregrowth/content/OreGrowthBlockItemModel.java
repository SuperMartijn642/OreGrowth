package com.supermartijn642.oregrowth.content;

import com.supermartijn642.oregrowth.OreGrowthClient;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.item.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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
    private final boolean animated;

    public OreGrowthBlockItemModel(BlockModelWrapper original){
        this.tints = original.tints;
        this.extents = original.extents;
        this.properties = original.properties;
        this.animated = original.animated;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver modelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int k){
        renderState.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        if(stack.hasFoil()){
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            renderState.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
            renderState.setAnimated();
        }

        int tintCount = this.tints.size();
        int[] tints = layer.prepareTintLayers(tintCount);
        for(int i = 0; i < tintCount; i++){
            int tint = this.tints.get(i).calculate(stack, level, owner == null ? null : owner.asLivingEntity());
            tints[i] = tint;
            renderState.appendModelIdentityElement(tint);
        }

        layer.setExtents(this.extents);
        layer.setRenderType(ItemBlockRenderTypes.getRenderType(stack));
        this.properties.applyToLayer(layer, displayContext);
        OreGrowthClient.itemModel.emitItemQuads(layer, this.random);
        Block base = OreGrowthClient.itemModel.getItemBaseBlockContext();
        if(base != null)
            renderState.appendModelIdentityElement(base);
        if(this.animated)
            renderState.setAnimated();
    }
}
