package com.supermartijn642.oregrowth.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
@WailaPlugin
public class OreGrowthWailaPlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration){
        IWailaPlugin.super.register(registration);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration){
        registration.registerBlockComponent(new IBlockComponentProvider() {
            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config){
                Direction facing = accessor.getBlockState().getValue(OreGrowthBlock.FACE);
                Block base = accessor.getLevel().getBlockState(accessor.getPosition().relative(facing)).getBlock();

                // Replace the block name
                tooltip.remove(Identifiers.CORE_OBJECT_NAME);
                //noinspection UnstableApiUsage
                tooltip.add(0, config.getWailaConfig().getFormatting().title(TextComponents.translation("oregrowth.ore_growth_block.adjusted_name", base.getName()).get()));

                // Add the growth tooltip
                OreGrowthRecipe recipe = OreGrowthRecipeManager.getRecipeFor(base);
                if(recipe != null){
                    float growth = (float)accessor.getBlockState().getValue(OreGrowthBlock.STAGE) / recipe.stages() * 100;
                    MutableComponent growthText = TextComponents.string(String.format("%.0f%%", growth)).color(growth < 100 ? ChatFormatting.WHITE : ChatFormatting.GREEN).get();
                    tooltip.add(TextComponents.translation("oregrowth.ore_growth_block.growth_hint", growthText).get());
                }
            }

            @Override
            public ResourceLocation getUid(){
                return new ResourceLocation(OreGrowth.MODID, "ore_growth_hint");
            }
        }, OreGrowthBlock.class);
        registration.registerBlockIcon(new IBlockComponentProvider() {
            @Override
            public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon){
                BlockState state = accessor.getBlockState();
                Direction facing = state.getValue(OreGrowthBlock.FACE);
                Block base = accessor.getLevel().getBlockState(accessor.getPosition().relative(facing)).getBlock();
                return new Element() {
                    @Override
                    public Vec2 getSize(){
                        return currentIcon.getSize();
                    }

                    @Override
                    public void render(PoseStack poseStack, float x, float y, float maxX, float maxY){
                        BakedModel model = ClientUtils.getItemRenderer().getItemModelShaper().getItemModel(OreGrowth.ORE_GROWTH_BLOCK.asItem());
                        if(model instanceof OreGrowthBlockBakedModel)
                            ((OreGrowthBlockBakedModel)model).withContext(base, () -> currentIcon.render(poseStack, x, y, maxX, maxY));
                        else
                            currentIcon.render(poseStack, x, y, maxX, maxY);
                    }

                    @Override
                    public @Nullable Component getMessage(){
                        return currentIcon.getMessage();
                    }
                };
            }

            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config){
            }

            @Override
            public ResourceLocation getUid(){
                return new ResourceLocation(OreGrowth.MODID, "ore_growth_icon");
            }
        }, OreGrowthBlock.class);
    }
}
