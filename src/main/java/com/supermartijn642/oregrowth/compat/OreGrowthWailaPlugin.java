package com.supermartijn642.oregrowth.compat;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.OreGrowthClient;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;

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
                tooltip.remove(JadeIds.CORE_OBJECT_NAME);
                tooltip.add(0, IThemeHelper.get().title(TextComponents.translation("oregrowth.ore_growth_block.adjusted_name", base.getName()).get()));

                // Add the growth tooltip
                OreGrowthRecipe recipe = OreGrowthRecipeManager.get(true).getRecipeFor(base);
                if(recipe != null){
                    float growth = (float)accessor.getBlockState().getValue(OreGrowthBlock.STAGE) / recipe.stages() * 100;
                    MutableComponent growthText = TextComponents.string(String.format("%.0f%%", growth)).color(growth < 100 ? ChatFormatting.WHITE : ChatFormatting.GREEN).get();
                    tooltip.add(TextComponents.translation("oregrowth.ore_growth_block.growth_hint", growthText).get());
                }
            }

            @Override
            public Identifier getUid(){
                return Identifier.fromNamespaceAndPath(OreGrowth.MODID, "ore_growth_hint");
            }
        }, OreGrowthBlock.class);
        registration.registerBlockIcon(new IBlockComponentProvider() {
            @Override
            public Element getIcon(BlockAccessor accessor, IPluginConfig config, Element currentIcon){
                BlockState state = accessor.getBlockState();
                Direction facing = state.getValue(OreGrowthBlock.FACE);
                Block base = accessor.getLevel().getBlockState(accessor.getPosition().relative(facing)).getBlock();
                return new Element() {
                    {
                        this.width = currentIcon.getWidth();
                        this.height = currentIcon.getHeight();
                    }

                    @Override
                    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
                        currentIcon.setX(this.getX());
                        currentIcon.setY(this.getY());
                        OreGrowthClient.itemModel.withContext(base, () -> currentIcon.render(guiGraphics, mouseX, mouseY, partialTicks));
                    }

                    @Override
                    public @Nullable Component getNarration(){
                        return currentIcon.getNarration();
                    }
                };
            }

            @Override
            public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config){
            }

            @Override
            public Identifier getUid(){
                return Identifier.fromNamespaceAndPath(OreGrowth.MODID, "ore_growth_icon");
            }
        }, OreGrowthBlock.class);
    }
}
