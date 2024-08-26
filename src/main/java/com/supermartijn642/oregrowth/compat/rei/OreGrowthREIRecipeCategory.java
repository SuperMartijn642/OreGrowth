package com.supermartijn642.oregrowth.compat.rei;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.content.OreGrowthBlock;
import com.supermartijn642.oregrowth.content.OreGrowthBlockBakedModel;
import com.supermartijn642.oregrowth.content.OreGrowthRecipe;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created 26/08/2024 by SuperMartijn642
 */
public class OreGrowthREIRecipeCategory implements DisplayCategory<OreGrowthREIDisplay> {

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(OreGrowth.MODID, "textures/screen/jei_category_background.png");

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
        return EntryStacks.of(OreGrowth.ORE_GROWTH_ITEM);
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
        widgets.add(Widgets.wrapRenderer(new Rectangle(startX + 37, startY + 20, 32, 15), (graphics, bounds1, mouseX, mouseY, delta) -> {
            graphics.pose().pushPose();
            graphics.pose().translate(bounds1.x, bounds1.y, 0);
            graphics.blit(BACKGROUND, 0, 0, 111, 0, 32, 15);
            graphics.pose().popPose();
        }));

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
        Slot baseSlot = Widgets.createSlot(new Rectangle(startX + 2, startY + 24, 30, 30))
            .entries(
                recipe.bases(BuiltInRegistries.BLOCK.asLookup()).stream()
                    .map(EntryStacks::of)
                    .map(entry -> {
                        EntryRenderer<ItemStack> originalRenderer = entry.getRenderer();
                        return entry.withRenderer(new EntryRenderer<>() {
                            @Override
                            public void render(EntryStack<ItemStack> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta){
                                Block base = baseGetter.apply(entry);
                                if(base != null){
                                    graphics.pose().pushPose();
                                    graphics.pose().translate(bounds.x, bounds.y, -100);
                                    renderModel(graphics, base.defaultBlockState(), 6, 6, 0);
                                    graphics.pose().popPose();
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
        widgets.add(Widgets.wrapRenderer(new Rectangle(startX + 2, startY + 8, 30, 30), (graphics, bounds1, mouseX, mouseY, delta) -> {
            Block base = baseGetter.apply(baseSlot.getCurrentEntry().cast());
            if(base != null){
                graphics.pose().pushPose();
                graphics.pose().translate(bounds1.x, bounds1.y, 0);
                int stage = (int)(System.currentTimeMillis() / 1200 % recipe.stages() + 1);
                BlockState state = OreGrowth.ORE_GROWTH_BLOCK.defaultBlockState().setValue(OreGrowthBlock.STAGE, stage);
                BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
                if(model instanceof OreGrowthBlockBakedModel)
                    ((OreGrowthBlockBakedModel)model).withContext(base, () -> renderModel(graphics, state, 7, 7, 10));
                else
                    renderModel(graphics, state, 7, 7, 10);
                graphics.pose().popPose();
            }
        }));

        return widgets;
    }

    private static void renderModel(GuiGraphics guiGraphics, BlockState state, int x, int y, int offset){
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + 8, y + 8, 150 + offset);
        poseStack.scale(1.85f, 1.85f, 1.85f);
        poseStack.mulPose(new Matrix4f().scaling(1, -1, 1));
        poseStack.scale(16, 16, 16);
        BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
        boolean blockLight = !model.usesBlockLight();
        if(blockLight)
            Lighting.setupForFlatItems();

        poseStack.mulPose(new Quaternionf().rotationXYZ(30 * ((float)Math.PI / 180), 225 * ((float)Math.PI / 180), 0 * ((float)Math.PI / 180)));
        poseStack.scale(0.625f, 0.625f, 0.625f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        ClientUtils.getBlockRenderer().renderSingleBlock(state, poseStack, guiGraphics.bufferSource(), 0xF000F0, OverlayTexture.NO_OVERLAY);

        guiGraphics.flush();
        if(blockLight)
            Lighting.setupFor3DItems();
        poseStack.popPose();
    }
}
