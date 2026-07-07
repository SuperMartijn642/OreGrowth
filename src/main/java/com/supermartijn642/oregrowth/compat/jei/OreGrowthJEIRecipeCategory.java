package com.supermartijn642.oregrowth.compat.jei;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import it.unimi.dsi.fastutil.ints.IntList;
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
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.runtime.IIngredientManager;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 05/10/2023 by SuperMartijn642
 */
public class OreGrowthJEIRecipeCategory implements IRecipeCategory<OreGrowthRecipe> {

    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    private final IDrawable arrow;
    private final IDrawable slotBackground;
    private final IDrawable icon;
    private final IIngredientManager ingredientManager;

    public OreGrowthJEIRecipeCategory(IGuiHelper guiHelper, IIngredientManager ingredientManager){
        this.arrow = guiHelper.createDrawable(Identifier.fromNamespaceAndPath(OreGrowth.MODID, "textures/screen/jei_category_background.png"), 111, 0, 32, 15);
        this.slotBackground = guiHelper.getSlotDrawable();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(OreGrowth.COMPLETE_ORE_GROWTH_BLOCK));
        this.ingredientManager = ingredientManager;
    }

    @Override
    public IRecipeType<OreGrowthRecipe> getRecipeType(){
        return OreGrowthJEIPlugin.ORE_GROWTH_RECIPE_TYPE;
    }

    @Override
    public Component getTitle(){
        return TextComponents.translation("oregrowth.jei_category.title").get();
    }

    @Override
    public int getWidth(){
        return 111;
    }

    @Override
    public int getHeight(){
        return 56;
    }

    @Override
    public IDrawable getIcon(){
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder layoutBuilder, OreGrowthRecipe recipe, IFocusGroup focusGroup){
        // Add the ore growth block as catalyst, just so it is easier to look up all ore growth recipes
        layoutBuilder.addInvisibleIngredients(RecipeIngredientRole.CRAFTING_STATION)
            .add(OreGrowth.ORE_GROWTH_ITEM)
            .add(OreGrowth.COMPLETE_ORE_GROWTH_ITEM);
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
                .add(drop.result());
        }
        // Base block
        IIngredientRenderer<ItemStack> originalRenderer = this.ingredientManager.getIngredientRenderer(VanillaTypes.ITEM_STACK);
        layoutBuilder.addSlot(RecipeIngredientRole.CRAFTING_STATION, columns == 1 ? 11 : 2, 24)
            .setSlotName("base")
            .addItemStacks(recipe.bases(BuiltInRegistries.BLOCK).stream().map(Block::asItem).map(Item::getDefaultInstance).toList())
            .setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
                @Override
                public void render(GuiGraphicsExtractor guiGraphics, ItemStack stack){
                }

                @Override
                public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag){
                    originalRenderer.getTooltip(tooltip, ingredient, tooltipFlag);
                }

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
    public void draw(OreGrowthRecipe recipe, IRecipeSlotsView slotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY){
        guiGraphics.pose().pushMatrix();
        if(slotsView.getSlotViews(RecipeIngredientRole.OUTPUT).size() <= 1)
            guiGraphics.pose().translate(9, 0);

        // Arrow
        this.arrow.draw(guiGraphics, 37, 20);

        // Pickaxe
        guiGraphics.fakeItem(Items.DIAMOND_PICKAXE.getDefaultInstance(), 43, 18);

        // Base block
        Block base = slotsView.findSlotByName("base")
            .flatMap(IRecipeSlotView::getDisplayedItemStack)
            .map(ItemStack::getItem)
            .filter(BlockItem.class::isInstance)
            .map(item -> ((BlockItem)item).getBlock())
            .orElse(null);
        GuiGraphicsHelper.of(guiGraphics).nextStratum();
        if(base != null)
            renderBlock(GuiGraphicsHelper.of(guiGraphics), base.defaultBlockState(), 0, 22, 0);

        // Ore growth block
        if(base != null){
            int stage = (int)(System.currentTimeMillis() / 1200 % recipe.stages() + 1);
            BlockState state = OreGrowth.ORE_GROWTH_BLOCK.defaultBlockState().setValue(OreGrowthBlock.STAGE, stage);
            BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(state);
            if(model instanceof OreGrowthBlockBakedModel)
                ((OreGrowthBlockBakedModel)model).withContext(base, () -> renderBlock(GuiGraphicsHelper.of(guiGraphics), state, 0, 6, 10));
            else
                renderBlock(GuiGraphicsHelper.of(guiGraphics), state, 0, 6, 10);
        }

        guiGraphics.pose().popMatrix();
    }

    private static void renderBlock(GuiGraphicsHelper graphics, BlockState state, int x, int y, int offset){
        // Create block render state
        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(state);
        BlockModelRenderState blockRenderState = new BlockModelRenderState();
        long seed = state.getSeed(BlockPos.ZERO);
        RANDOM_SOURCE.setSeed(seed);
        QuadEmitter emitter = blockRenderState.setupMesh(IDENTITY_MATRIX, model.hasMaterialFlag(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, RANDOM_SOURCE, BakedQuad.FLAG_TRANSLUCENT));
        RANDOM_SOURCE.setSeed(seed);
        model.emitQuads(emitter, BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, RANDOM_SOURCE, _ -> false);
        IntList tintLayers = blockRenderState.tintLayers();
        for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(state))
            tintLayers.add(tintSource.color(state));

        // Submit block model
        graphics.submitFeatures(
            x, y, 40, 40,
            (poseStack, output) -> {
                poseStack.pushPose();
                poseStack.translate(30, 25, 150 + offset);
                poseStack.scale(1.85f, 1.85f, 1.85f);
                poseStack.scale(16, -16, 16);
                poseStack.mulPose(new Quaternionf().rotationXYZ(30 * ((float)Math.PI / 180), 225 * ((float)Math.PI / 180), 0 * ((float)Math.PI / 180)));
                poseStack.scale(0.625f, 0.625f, 0.625f);
                blockRenderState.submit(poseStack, output, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        );
    }
}
