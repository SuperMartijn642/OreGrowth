package com.supermartijn642.oregrowth.content;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthBlockBakedModel implements BakedModel {

    public static final ModelProperty<Block> BASE_BLOCK_PROPERTY = new ModelProperty<>();

    private static final int BLOCK_VERTEX_DATA_UV_OFFSET = findUVOffset(DefaultVertexFormat.BLOCK, VertexFormatElement.Usage.UV);
    private static final int BLOCK_VERTEX_DATA_TINT_OFFSET = findUVOffset(DefaultVertexFormat.BLOCK, VertexFormatElement.Usage.COLOR);
    private static final Direction[] MODEL_DIRECTIONS = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null};

    private final BakedModel original;
    private final Map<Direction,Map<Block,List<BakedQuad>>> quadCache = new HashMap<>();
    private final Map<Block,List<BakedQuad>> directionlessQuadCache = new HashMap<>();
    private final ThreadLocal<Block> baseBlock = new ThreadLocal<>();

    public OreGrowthBlockBakedModel(BakedModel original){
        this.original = original;
        for(Direction direction : Direction.values())
            this.quadCache.put(direction, new HashMap<>());
    }

    public void withContext(Block baseBlock, Runnable runnable){
        this.baseBlock.set(baseBlock);
        runnable.run();
        this.baseBlock.set(null);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData data){
        if(data.has(BASE_BLOCK_PROPERTY))
            return data;

        // Get the base block
        BlockPos basePos = pos.relative(state.getValue(OreGrowthBlock.FACE));
        Block base = level.getBlockState(basePos).getBlock();
        return ModelData.builder().with(BASE_BLOCK_PROPERTY, base).build();
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random, @NotNull ModelData data, @Nullable RenderType renderType){
        // Get the base block
        Block base = this.baseBlock.get();
        if(base == null)
            base = data.get(BASE_BLOCK_PROPERTY);
        if(base == null)
            return this.original.getQuads(state, side, random, data, renderType);

        // Get the correct cache and quads
        Map<Block,List<BakedQuad>> cache = side == null ? this.directionlessQuadCache : this.quadCache.get(side);
        List<BakedQuad> quads;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized(cache){
            quads = cache.get(base);
        }

        // Compute the quads if they don't exist yet
        if(quads == null){
            quads = this.remapQuads(this.original.getQuads(state, side, random), base, random);
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized(cache){
                if(!cache.containsKey(base))
                    cache.put(base, quads);
                else
                    quads = cache.get(base);
            }
        }

        // Safety check even though this should never happen
        if(quads == null)
            throw new IllegalStateException("Tried returning null list from OreGrowthBlockBakedModel#getQuads for side '" + side + "' and base '" + base + "'!");

        return quads;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random){
        return this.getQuads(state, side, random, ModelData.EMPTY, RenderType.solid());
    }

    private List<BakedQuad> remapQuads(List<BakedQuad> originalQuads, Block baseBlock, RandomSource random){
        BlockState baseState = baseBlock.defaultBlockState();
        BakedModel baseModel = ClientUtils.getBlockRenderer().getBlockModel(baseState);

        // Find the most occurring sprite (and sprite color)
        Map<TextureAtlasSprite,Pair<Holder<Integer>,Integer>> spriteCounts = new HashMap<>();
        for(Direction cullFace : MODEL_DIRECTIONS){
            for(RenderType renderType : baseModel.getRenderTypes(baseState, random, ModelData.EMPTY)){
                baseModel.getQuads(baseState, cullFace, random, ModelData.EMPTY, renderType)
                    .forEach(quad -> {
                        TextureAtlasSprite sprite = quad.getSprite();
                        Holder<Integer> count = spriteCounts.computeIfAbsent(sprite, s -> Pair.of(new Holder<>(0), quad.getTintIndex())).left();
                        count.set(count.get() + 1);
                    });
            }
        }
        if(spriteCounts.isEmpty())
            return originalQuads;

        // Get the sprite
        TextureAtlasSprite sprite = null;
        int tint = 0;
        int count = 0;
        for(Map.Entry<TextureAtlasSprite,Pair<Holder<Integer>,Integer>> entry : spriteCounts.entrySet()){
            if(entry.getValue().left().get() > count){
                sprite = entry.getKey();
                tint = entry.getValue().right();
            }
        }

        // Remap the quads
        TextureAtlasSprite finalSprite = sprite;
        int finalTint = tint;
        return originalQuads.stream().map(quad -> this.remapQuad(quad, finalSprite, finalTint)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    protected BakedQuad remapQuad(BakedQuad quad, TextureAtlasSprite newSprite, int newTint){
        TextureAtlasSprite sprite = quad.getSprite();
        int[] vertexData = quad.getVertices();
        // Make sure we don't change the original quad
        vertexData = Arrays.copyOf(vertexData, vertexData.length);

        // Adjust the uv
        int vertexSize = DefaultVertexFormat.BLOCK.getIntegerSize();
        int vertices = vertexData.length / vertexSize;
        int uvOffset = BLOCK_VERTEX_DATA_UV_OFFSET / 4;
        int tintOffset = BLOCK_VERTEX_DATA_TINT_OFFSET / 4;

        for(int i = 0; i < vertices; i++){
            int offset = i * vertexSize;

            // UV
            float u = Float.intBitsToFloat(vertexData[offset + uvOffset]);
            float newU = newSprite.getU0() + (u - sprite.getU0()) / (sprite.getU1() - sprite.getU0()) * (newSprite.getU1() - newSprite.getU0());
            vertexData[offset + uvOffset] = Float.floatToRawIntBits(newU);
            float v = Float.intBitsToFloat(vertexData[offset + uvOffset + 1]);
            float newV = newSprite.getV0() + (v - sprite.getV0()) / (sprite.getV1() - sprite.getV0()) * (newSprite.getV1() - newSprite.getV0());
            vertexData[offset + uvOffset + 1] = Float.floatToRawIntBits(newV);

            // Tint
            vertexData[offset + tintOffset] = newTint;
        }

        // Create a new quad
        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }

    private static int[] adjustVertexDataUV(int[] vertexData, int newU, int newV, TextureAtlasSprite sprite){
        int vertexSize = DefaultVertexFormat.BLOCK.getIntegerSize();
        int vertices = vertexData.length / vertexSize;
        int uvOffset = BLOCK_VERTEX_DATA_UV_OFFSET / 4;

        for(int i = 0; i < vertices; i++){
            int offset = i * vertexSize + uvOffset;

            float width = sprite.getU1() - sprite.getU0();
            float u = Float.intBitsToFloat(vertexData[offset]) + width * newU;
            vertexData[offset] = Float.floatToRawIntBits(u);

            float height = sprite.getV1() - sprite.getV0();
            float v = Float.intBitsToFloat(vertexData[offset + 1]) + height * newV;
            vertexData[offset + 1] = Float.floatToRawIntBits(v);
        }
        return vertexData;
    }

    private static int findUVOffset(VertexFormat vertexFormat, VertexFormatElement.Usage vertexFormatElement){
        int index;
        VertexFormatElement element = null;
        for(index = 0; index < vertexFormat.getElements().size(); index++){
            VertexFormatElement el = vertexFormat.getElements().get(index);
            if(el.getUsage() == vertexFormatElement){
                element = el;
                break;
            }
        }
        if(index == vertexFormat.getElements().size() || element == null)
            throw new RuntimeException("Expected vertex format to have a '" + vertexFormat + "' attribute");
        return vertexFormat.offsets.getInt(index);
    }

    @Override
    public boolean isCustomRenderer(){
        return this.original.isCustomRenderer();
    }

    @Override
    public ItemTransforms getTransforms(){
        return this.original.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides(){
        return ItemOverrides.EMPTY;
    }

    @Override
    public boolean useAmbientOcclusion(){
        return this.original.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d(){
        return this.original.isGui3d();
    }

    @Override
    public boolean usesBlockLight(){
        return this.original.usesBlockLight();
    }

    @Override
    public TextureAtlasSprite getParticleIcon(){
        return this.original.getParticleIcon();
    }
}
