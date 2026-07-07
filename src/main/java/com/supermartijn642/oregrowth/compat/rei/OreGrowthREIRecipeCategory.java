package com.supermartijn642.oregrowth.compat.rei;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import it.unimi.dsi.fastutil.ints.IntList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created 26/08/2024 by SuperMartijn642
 */
public class OreGrowthREIRecipeCategory implements DisplayCategory<OreGrowthREIDisplay> {

    public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(OreGrowth.MODID, "textures/screen/jei_category_background.png");
    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    @Override
    public CategoryIdentifier<? extends OreGrowthREIDisplay> getCategoryIdentifier(){
        return OreGrowthREIPlugin.ORE_GROWTH_CATEGORY;
    }

    @Override
    public Component getTitle(){
        return TextComponents.translation("oregrowth.jei_category.title").get();
    }

    @Override
    public Renderer getIcon(){
        return EntryStacks.of(OreGrowth.COMPLETE_ORE_GROWTH_ITEM);
    }

    @Override
    public int getDisplayWidth(OreGrowthREIDisplay display){
        return 111 + 10;
    }

    @Override
    public int getDisplayHeight(){
        return 56 + 10;
    }

    @Override
    public List<Widget> setupDisplay(OreGrowthREIDisplay display, Rectangle bounds){
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        int startX = bounds.x + 5, startY = bounds.y + 5;

        // Outputs
        OreGrowthRecipe recipe = display.getRecipe();
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
            widgets.add(
                Widgets.createSlot(new Point(startX + x, startY + y))
                    .entries(List.of(EntryStacks.of(drop.result()).tooltip(tooltips)))
                    .markOutput()
            );
        }
        if(outputs <= 1)
            startX += 9;

        // Arrow
        widgets.add(Widgets.wrapRenderer(new Rectangle(startX + 37, startY + 20, 32, 15), (graphics, bounds1, mouseX, mouseY, delta) ->
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, bounds1.x, bounds1.y, 111, 0, 32, 15, 256, 256)
        ));

        // Pickaxe
        widgets.add(
            Widgets.createSlot(new Point(startX + 43, startY + 18))
                .entry(EntryStacks.of(Items.DIAMOND_PICKAXE))
                .notInteractable()
                .disableBackground()
                .disableHighlight()
                .disableTooltips()
        );

        // Base block
        Function<EntryStack<ItemStack>,Block> baseGetter = entry -> Optional.ofNullable(entry)
            .map(EntryStack::getValue)
            .map(ItemStack::getItem)
            .filter(BlockItem.class::isInstance)
            .map(item -> ((BlockItem)item).getBlock())
            .orElse(null);
        Slot baseSlot = Widgets.createSlot(new Rectangle(startX + 2, startY + 24, 32, 32))
            .entries(
                recipe.bases(BuiltInRegistries.BLOCK).stream()
                    .map(EntryStacks::of)
                    .map(entry -> {
                        EntryRenderer<ItemStack> originalRenderer = entry.getRenderer();
                        return entry.withRenderer(new EntryRenderer<>() {
                            @Override
                            public void render(EntryStack<ItemStack> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta){
                                Block base = baseGetter.apply(entry);
                                if(base != null){
                                    graphics.pose().pushMatrix();
                                    graphics.pose().translate(bounds.x, bounds.y);
                                    GuiGraphicsHelper.of(graphics).nextStratum();
                                    renderBlock(GuiGraphicsHelper.of(graphics), base.defaultBlockState(), -2, -2, 0);
                                    graphics.pose().popMatrix();
                                }
                            }

                            @Override
                            public @Nullable Tooltip getTooltip(EntryStack<ItemStack> entry, TooltipContext context){
                                return originalRenderer.getTooltip(entry, context);
                            }
                        });
                    })
                    .toList()
            )
            .disableBackground()
            .markInput();
        widgets.add(baseSlot);

        // Ore growth block
        widgets.add(Widgets.wrapRenderer(new Rectangle(startX + 2, startY + 8, 32, 32), (graphics, bounds1, mouseX, mouseY, delta) -> {
            Block base = baseGetter.apply(baseSlot.getCurrentEntry().cast());
            if(base != null){
                graphics.pose().pushMatrix();
                graphics.pose().translate(bounds1.x, bounds1.y);
                int stage = (int)(System.currentTimeMillis() / 1200 % recipe.stages() + 1);
                BlockState state = OreGrowth.ORE_GROWTH_BLOCK.defaultBlockState().setValue(OreGrowthBlock.STAGE, stage);
                BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(state);
                if(model instanceof OreGrowthBlockBakedModel)
                    ((OreGrowthBlockBakedModel)model).withContext(base, () -> renderBlock(GuiGraphicsHelper.of(graphics), state, -1, -2, 10));
                else
                    renderBlock(GuiGraphicsHelper.of(graphics), state, -1, -2, 10);
                graphics.pose().popMatrix();
            }
        }));

        return widgets;
    }

    private static void renderBlock(GuiGraphicsHelper graphics, BlockState state, int x, int y, int offset){
        // Create block render state
        BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(state);
        BlockModelRenderState blockRenderState = new BlockModelRenderState();
        long seed = state.getSeed(BlockPos.ZERO);
        List<BlockStateModelPart> parts = blockRenderState.setupModel(IDENTITY_MATRIX, model.hasMaterialFlag(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, BakedQuad.FLAG_TRANSLUCENT));
        RANDOM_SOURCE.setSeed(seed);
        model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, RANDOM_SOURCE, parts);
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
