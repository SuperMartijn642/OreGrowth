package com.supermartijn642.oregrowth.compat;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.registry.Registries;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import com.supermartijn642.oregrowth.content.OreGrowthRecipeManager;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.elements.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Function;

/**
 * Created 06/10/2023 by SuperMartijn642
 */
public class OreGrowthTOPPlugin implements Function<ITheOneProbe,Void> {

    @Override
    public Void apply(ITheOneProbe theOneProbe){
        theOneProbe.registerProvider(new IProbeInfoProvider() {
            @Override
            public ResourceLocation getID(){
                return new ResourceLocation(OreGrowth.MODID, "ore_growth_hint");
            }

            @Override
            public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState state, IProbeHitData hitData){
                if(!state.is(OreGrowth.ORE_GROWTH_BLOCK))
                    return;

                Direction facing = state.getValue(OreGrowthBlock.FACE);
                Block base = level.getBlockState(hitData.getPos().relative(facing)).getBlock();

                // Replace the block name and icon
                if(probeInfo.getElements().size() > 0){
                    IElement element = probeInfo.getElements().get(0);
                    List<IElement> elements;
                    if(element instanceof ElementHorizontal && (elements = ((ElementHorizontal)element).getElements()).size() == 2){
                        // Replace the icon
                        if(elements.get(0) instanceof ElementItemStack){
                            ElementItemStack oldElement = (ElementItemStack)elements.remove(0);
                            elements.add(0, new WrappedItemStackElement(oldElement, probeInfo.defaultItemStyle(), base));
                        }
                        // Replace the name
                        if(elements.get(1) instanceof ElementVertical vertical && vertical.getElements().get(0) instanceof ElementItemLabel){
                            vertical.getElements().remove(0);
                            vertical.getElements().add(0, new ElementText(TextComponents.translation("oregrowth.ore_growth_block.adjusted_name", base.getName()).get()));
                        }
                    }
                }

                // Add the growth tooltip
                OreGrowthRecipe recipe = OreGrowthRecipeManager.getRecipeFor(base);
                if(recipe != null){
                    float growth = (float)state.getValue(OreGrowthBlock.STAGE) / recipe.stages() * 100;
                    MutableComponent growthText = TextComponents.string(growth < 100 ? TextStyleClass.WARNING.toString() : TextStyleClass.OK.toString()).string(String.format("%.0f%%", growth)).get();
                    probeInfo.text(TextComponents.string(TextStyleClass.LABEL.toString()).translation("oregrowth.ore_growth_block.growth_hint", growthText).get());
                }
            }
        });
        theOneProbe.registerElementFactory(new IElementFactory() {
            @Override
            public IElement createElement(FriendlyByteBuf buffer){
                return new WrappedItemStackElement(buffer);
            }

            @Override
            public ResourceLocation getId(){
                return new ResourceLocation(OreGrowth.MODID, "ore_growth_icon");
            }
        });
        return null;
    }

    private static class WrappedItemStackElement extends ElementItemStack {

        private final ElementItemStack wrapped;
        private final Block base;

        public WrappedItemStackElement(ElementItemStack wrapped, IItemStyle itemStyle, Block base){
            super(ItemStack.EMPTY, itemStyle);
            this.wrapped = wrapped;
            this.base = base;
        }

        @Override
        public ResourceLocation getID(){
            return new ResourceLocation(OreGrowth.MODID, "ore_growth_icon");
        }

        public WrappedItemStackElement(FriendlyByteBuf buf){
            super(buf);
            this.wrapped = null;
            this.base = Registries.BLOCKS.getValue(buf.readResourceLocation());
            if(this.base == null)
                throw new RuntimeException("Received invalid base block!");
        }

        @Override
        public void render(GuiGraphics graphics, int x, int y){
            BakedModel model = ClientUtils.getItemRenderer().getItemModelShaper().getItemModel(OreGrowth.ORE_GROWTH_BLOCK.asItem());
            if(model instanceof OreGrowthBlockBakedModel)
                ((OreGrowthBlockBakedModel)model).withContext(this.base, () -> super.render(graphics, x, y));
            else
                super.render(graphics, x, y);
        }

        @Override
        public void toBytes(FriendlyByteBuf buf){
            if(this.wrapped != null)
                this.wrapped.toBytes(buf);
            else
                super.toBytes(buf);
            buf.writeResourceLocation(Registries.BLOCKS.getIdentifier(this.base));
        }
    }
}
