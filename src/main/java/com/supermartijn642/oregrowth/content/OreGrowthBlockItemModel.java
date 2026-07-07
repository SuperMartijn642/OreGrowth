package com.supermartijn642.oregrowth.content;

import com.supermartijn642.oregrowth.OreGrowthClient;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Created 06/07/2025 by SuperMartijn642
 */
public class OreGrowthBlockItemModel implements ItemModel {

    private final RandomSource random = RandomSource.create();

    private final List<ItemTintSource> tints;
    private final Supplier<Vector3fc[]> extents;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;

    public OreGrowthBlockItemModel(CuboidItemModelWrapper original){
        this.tints = original.tints;
        this.extents = original.extents;
        this.properties = original.properties;
        this.transformation = original.transformation;
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

        if(!this.tints.isEmpty()){
            IntList tintLayers = layer.tintLayers();
            for(ItemTintSource tintSource : this.tints){
                int tint = tintSource.calculate(stack, level, owner == null ? null : owner.asLivingEntity());
                tintLayers.add(tint);
                renderState.appendModelIdentityElement(tint);
            }
        }

        layer.setExtents(this.extents);
        layer.setLocalTransform(this.transformation);
        this.properties.applyToLayer(layer, displayContext);
        QuadEmitter emitter = layer.emitter();
        AtomicBoolean animated = new AtomicBoolean(false);
        emitter.pushTransform(quad -> {
            if(quad.animated())
                animated.set(true);
            return true;
        });
        OreGrowthClient.itemModel.emitItemQuads(emitter, this.random);
        emitter.popTransform();
        if(animated.get())
            renderState.setAnimated();
        Block base = OreGrowthClient.itemModel.getItemBaseBlockContext();
        if(base != null)
            renderState.appendModelIdentityElement(base);
    }
}
