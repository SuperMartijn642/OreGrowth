package com.supermartijn642.oregrowth.compat;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
public class OreGrowthJEIRecipeCategory implements IRecipeCategory<OreGrowthRecipe> {

    private static final RandomSource RANDOM = RandomSource.create();

    private final IDrawable background;
    private final IDrawable icon;
    private final IIngredientManager ingredientManager;

    public OreGrowthJEIRecipeCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager){
        this.background = guiHelper.createDrawable(new ResourceLocation(OreGrowth.MODID, "textures/screen/jei_category_background.png"), 0, 10, 93, 52);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(OreGrowth.ORE_GROWTH_BLOCK));
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
        // Base block
        IIngredientRenderer<ItemStack> originalRenderer = this.ingredientManager.getIngredientRenderer(VanillaTypes.ITEM_STACK);
        layoutBuilder.addSlot(RecipeIngredientRole.CATALYST, 2, 22)
            .addItemStack(new ItemStack(recipe.base()))
            .setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
                @Override
                public void render(PoseStack poseStack, ItemStack stack){
                }

                @Override
                public List<Component> getTooltip(ItemStack stack, TooltipFlag flag){
                    return originalRenderer.getTooltip(stack, flag);
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
        // Output
        layoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 75, 18).addItemStack(recipe.output());
    }

    @Override
    public void draw(OreGrowthRecipe recipe, IRecipeSlotsView slotsView, PoseStack poseStack, double mouseX, double mouseY){
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.mulPoseMatrix(poseStack.last().pose());

        // Pickaxe
        RenderSystem.enableDepthTest();
        ClientUtils.getItemRenderer().renderAndDecorateFakeItem(Items.DIAMOND_PICKAXE.getDefaultInstance(), 43, 16);
        RenderSystem.disableDepthTest();

        // Base block
        renderModel(recipe.base().defaultBlockState(), 9, 29, 0, ModelData.EMPTY);

        // Ore growth block
        int stage = (int)(System.currentTimeMillis() / 1200 % recipe.stages() + 1);
        BlockState state = OreGrowth.ORE_GROWTH_BLOCK.defaultBlockState().setValue(OreGrowthBlock.STAGE, stage);
        ModelData modelData = ModelData.builder().with(OreGrowthBlockBakedModel.BASE_BLOCK_PROPERTY, recipe.base()).build();
        renderModel(state, 9, 13, 10, modelData);

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderModel(BlockState state, int x, int y, int offset, ModelData modelData){
        ClientUtils.getTextureManager().getTexture(TextureAtlases.getBlocks()).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlases.getBlocks());
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(x + 8, y + 8, 150 + offset);
        poseStack.scale(1.85f, 1.85f, 1.85f);
        poseStack.scale(1, -1, 1);
        poseStack.scale(16, 16, 16);
        RenderSystem.applyModelViewMatrix();
        BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
        boolean blockLight = !model.usesBlockLight();
        if(blockLight)
            Lighting.setupForFlatItems();

        PoseStack poseStack2 = new PoseStack();
        poseStack2.mulPose(new Quaternion(30, 225, 0, true));
        poseStack2.scale(0.625f, 0.625f, 0.625f);
        poseStack2.translate(-0.5f, -0.5f, -0.5f);
        MultiBufferSource.BufferSource bufferSource = RenderUtils.getMainBufferSource();
        RANDOM.setSeed(42);
        ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, RANDOM, modelData);
        RenderType renderType = renderTypes.contains(RenderType.translucent()) ? Sheets.translucentCullBlockSheet() : Sheets.cutoutBlockSheet();
        ClientUtils.getBlockRenderer().renderSingleBlock(state, poseStack2, bufferSource, 0xF000F0, OverlayTexture.NO_OVERLAY, modelData, renderType);

        bufferSource.endBatch();
        if(blockLight)
            Lighting.setupFor3DItems();
        poseStack.popPose();
    }
}
