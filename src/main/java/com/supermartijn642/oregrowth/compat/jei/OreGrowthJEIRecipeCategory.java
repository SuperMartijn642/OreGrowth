package com.supermartijn642.oregrowth.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
public class OreGrowthJEIRecipeCategory implements IRecipeCategory<OreGrowthRecipe> {

    private final IDrawable background;
    private final IDrawable arrow;
    private final IDrawable slotBackground;
    private final IDrawable icon;
    private final IIngredientManager ingredientManager;

    public OreGrowthJEIRecipeCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager){
        this.background = guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath(OreGrowth.MODID, "textures/screen/jei_category_background.png"), 0, 8, 111, 56);
        this.arrow = guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath(OreGrowth.MODID, "textures/screen/jei_category_background.png"), 111, 0, 32, 15);
        this.slotBackground = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(OreGrowth.COMPLETE_ORE_GROWTH_BLOCK));
        this.ingredientManager = ingredientManager;
    }

    @Override
    public RecipeType<OreGrowthRecipe> getRecipeType(){
        return OreGrowthJEIPlugin.ORE_GROWTH_RECIPE_TYPE;
    }

    @Override
    public Component getTitle(){
        return TextComponents.translation("oregrowth.jei_category.title").get();
    }

    @Override
    public IDrawable getBackground(){
        return this.background;
    }

    @Override
    public IDrawable getIcon(){
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder layoutBuilder, OreGrowthRecipe recipe, IFocusGroup focusGroup){
        // Add the ore growth block as catalyst, just so it is easier to look up all ore growth recipes
        layoutBuilder.addInvisibleIngredients(RecipeIngredientRole.CATALYST)
            .addItemStack(OreGrowth.ORE_GROWTH_ITEM.getDefaultInstance())
            .addItemStack(OreGrowth.COMPLETE_ORE_GROWTH_ITEM.getDefaultInstance());
        // Outputs
        int outputs = Math.min(recipe.getRecipeViewerDrops().size(), 6);
        int columns = outputs > 1 ? 2 : 1;
        int rows = (outputs + 1) / 2;
        for(int i = 0; i < outputs; i++){
            OreGrowthRecipe.RecipeViewerDrop drop = recipe.getRecipeViewerDrops().get(i);
            int x = 93 - columns * 9 + (i % columns) * 18;
            int y = 29 - rows * 9 + i / columns * 18;
            List<Component> tooltips = new ArrayList<>(3);
            tooltips.add(TextComponents.empty().get());
            Component minGrowth = TextComponents.number((int)Math.round((double)drop.minStage() / recipe.stages() * 100)).color(ChatFormatting.GOLD).string("%").color(ChatFormatting.GOLD).get();
            Component maxGrowth = TextComponents.number((int)Math.round((double)drop.maxStage() / recipe.stages() * 100)).color(ChatFormatting.GOLD).string("%").color(ChatFormatting.GOLD).get();
            if(drop.maxStage() > 1 || drop.maxStage() < recipe.stages()){
                if(drop.minStage() == drop.maxStage())
                    tooltips.add(TextComponents.translation("oregrowth.jei_category.growth", minGrowth).get());
                else
                    tooltips.add(TextComponents.translation("oregrowth.jei_category.growth.range", minGrowth, maxGrowth).get());
            }
            if(drop.chance() < 1)
                tooltips.add(TextComponents.translation("oregrowth.jei_category.chance", TextComponents.number(drop.chance() * 100).color(ChatFormatting.GOLD).string("%").color(ChatFormatting.GOLD).get()).get());
            if(!drop.tooltip().isEmpty()){
                tooltips.add(TextComponents.translation("oregrowth.jei_category.conditions").get());
                tooltips.addAll(drop.tooltip());
            }
            layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                .setBackground(this.slotBackground, -1, -1)
                .addRichTooltipCallback((slotView, list) -> list.addAll(tooltips))
                .addItemStack(drop.result());
        }
        // Base block
        IIngredientRenderer<ItemStack> originalRenderer = this.ingredientManager.getIngredientRenderer(VanillaTypes.ITEM_STACK);
        layoutBuilder.addSlot(RecipeIngredientRole.CATALYST, columns == 1 ? 11 : 2, 24)
            .setSlotName("base")
            .addItemStacks(recipe.bases(BuiltInRegistries.BLOCK).stream().map(Block::asItem).map(Item::getDefaultInstance).toList())
            .setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
                @Override
                public void render(GuiGraphics guiGraphics, ItemStack stack){
                }

                @Override
                public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag){
                    originalRenderer.getTooltip(tooltip, ingredient, tooltipFlag);
                }

                @SuppressWarnings("removal")
                @Override
                public List<Component> getTooltip(ItemStack stack, TooltipFlag flag){
                    return List.of();
                }

                @Override
                public Font getFontRenderer(Minecraft minecraft, ItemStack ingredient){
                    return originalRenderer.getFontRenderer(minecraft, ingredient);
                }

                @Override
                public int getWidth(){
                    return 30;
                }

                @Override
                public int getHeight(){
                    return 30;
                }
            });
    }

    @Override
    public void draw(OreGrowthRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY){
        guiGraphics.pose().popMatrix();
        if(slotsView.getSlotViews(RecipeIngredientRole.OUTPUT).size() <= 1)
            guiGraphics.pose().translate(9, 0);

        // Arrow
        this.arrow.draw(guiGraphics, 37, 20);

        // Pickaxe
        guiGraphics.renderFakeItem(Items.DIAMOND_PICKAXE.getDefaultInstance(), 43, 18);

        // Base block
        Block base = slotsView.findSlotByName("base")
            .flatMap(IRecipeSlotView::getDisplayedItemStack)
            .map(ItemStack::getItem)
            .filter(BlockItem.class::isInstance)
            .map(item -> ((BlockItem)item).getBlock())
            .orElse(null);
        if(base != null)
            GuiGraphicsHelper.of(guiGraphics).submitCustomRendering(
                22, 39, 40, 40, // TODO
                poseStack -> renderModel(poseStack, base.defaultBlockState(), 22, 39, 0)
            );

        // Ore growth block
        if(base != null){
            int stage = (int)(System.currentTimeMillis() / 1200 % recipe.stages() + 1);
            BlockState state = OreGrowth.ORE_GROWTH_BLOCK.defaultBlockState().setValue(OreGrowthBlock.STAGE, stage);
            BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
            if(model instanceof OreGrowthBlockBakedModel)
                GuiGraphicsHelper.of(guiGraphics).submitCustomRendering(
                    22, 39, 40, 40, // TODO
                    poseStack -> ((OreGrowthBlockBakedModel)model).withContext(base, () -> renderModel(poseStack, state, 22, 23, 10))
                );
            else
                GuiGraphicsHelper.of(guiGraphics).submitCustomRendering(
                    22, 39, 40, 40, // TODO
                    poseStack -> renderModel(poseStack, state, 22, 23, 10)
                );
        }

        guiGraphics.pose().popMatrix();
    }

    private static void renderModel(PoseStack poseStack, BlockState state, int x, int y, int offset){
        poseStack.translate(x + 21, y + 16, 150 + offset);
        poseStack.scale(1.85f, 1.85f, 1.85f);
        poseStack.scale(16, -16, 16);
        BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(state);

        poseStack.mulPose(new Quaternionf().rotationXYZ(30 * ((float)Math.PI / 180), 225 * ((float)Math.PI / 180), 0 * ((float)Math.PI / 180)));
        poseStack.scale(0.625f, 0.625f, 0.625f);
        ModelBlockRenderer.renderModel(poseStack.last(), RenderUtils.getMainBufferSource().getBuffer(Sheets.translucentItemSheet()), model, 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }
}
