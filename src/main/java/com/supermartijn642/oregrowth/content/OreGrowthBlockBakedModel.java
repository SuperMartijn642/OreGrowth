package com.supermartijn642.oregrowth.content;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import java.util.function.Function;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthBlockBakedModel implements BlockStateModel {

    public static final ModelProperty<Block> BASE_BLOCK_PROPERTY = new ModelProperty<>();

    private static final int BLOCK_VERTEX_DATA_UV_OFFSET = findUVOffset(DefaultVertexFormat.BLOCK, VertexFormatElement.Usage.UV);
    private static final Direction[] MODEL_DIRECTIONS = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null};

    private final BlockStateModel original;
    private final Map<Block,List<BlockModelPart>> meshCache = new HashMap<>();

    public OreGrowthBlockBakedModel(BlockStateModel original){
        this.original = original;
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
    public void collectParts(RandomSource random, List<BlockModelPart> parts, ModelData modelData, @Nullable ChunkSectionLayer layer){
        // Get the base block
        Block base = modelData.get(BASE_BLOCK_PROPERTY);
        if(base == null){
            this.original.collectParts(random, parts, modelData, layer);
            return;
        }

        // Get the mesh from cache
        List<BlockModelPart> mesh;
        synchronized(this.meshCache){
            mesh = this.meshCache.get(base);
        }

        // Compute the mesh if it doesn't exist yet
        if(mesh == null){
            mesh = this.computeMesh(
                model -> model.collectParts(random, ModelData.EMPTY, null),
                base
            );
            synchronized(this.meshCache){
                if(!this.meshCache.containsKey(base))
                    this.meshCache.put(base, mesh);
                else
                    mesh = this.meshCache.get(base);
            }
        }

        parts.addAll(mesh);
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts){
        this.original.collectParts(random, parts);
    }

    private List<BlockModelPart> computeMesh(Function<BlockStateModel,List<BlockModelPart>> modelEmitter, Block baseBlock){
        BlockState baseState = baseBlock.defaultBlockState();
        BlockStateModel baseModel = ClientUtils.getBlockRenderer().getBlockModel(baseState);

        // Find the most occurring sprite
        Map<TextureAtlasSprite,Pair<Holder<Integer>,MaterialEntry>> spriteCounts = new HashMap<>();
        for(BlockModelPart part : modelEmitter.apply(baseModel)){
            for(Direction cullFace : MODEL_DIRECTIONS){
                part.getQuads(cullFace)
                    .forEach(quad -> {
                        TextureAtlasSprite sprite = quad.sprite();
                        Holder<Integer> count = spriteCounts.computeIfAbsent(sprite, s -> Pair.of(new Holder<>(0), new MaterialEntry(sprite, quad.shade(), quad.lightEmission(), quad.ambientOcclusion()))).left();
                        count.set(count.get() + 1);
                    });
            }
        }
        if(spriteCounts.isEmpty())
            return modelEmitter.apply(this.original);

        // Get the sprite
        MaterialEntry material = null;
        int count = 0;
        for(Pair<Holder<Integer>,MaterialEntry> entry : spriteCounts.values()){
            if(entry.left().get() > count)
                material = entry.right();
        }

        // Collect and remap all quads
        Map<Direction,List<BakedQuad>> quads = new EnumMap<>(Direction.class);
        for(Direction direction : Direction.values()) quads.put(direction, new ArrayList<>());
        List<BakedQuad> directionLessQuads = new ArrayList<>();
        for(BlockModelPart part : modelEmitter.apply(this.original)){
            for(Direction cullDirection : MODEL_DIRECTIONS){
                for(BakedQuad quad : part.getQuads(cullDirection)){
                    if(cullDirection == null)
                        directionLessQuads.add(remapQuad(quad, material));
                    else
                        quads.get(cullDirection).add(remapQuad(quad, material));
                }
            }
        }

        // Create new model part
        TextureAtlasSprite sprite = material.sprite;
        boolean ambientOcclusion = material.ambientOcclusion;
        return List.of(new BlockModelPart() {
            @Override
            public List<BakedQuad> getQuads(@Nullable Direction cullDirection){
                return cullDirection == null ? directionLessQuads : quads.get(cullDirection);
            }

            @Override
            public TextureAtlasSprite particleIcon(){
                return sprite;
            }

            @Override
            public boolean useAmbientOcclusion(){
                return ambientOcclusion;
            }
        });
    }

    private static BakedQuad remapQuad(BakedQuad quad, MaterialEntry material){
        TextureAtlasSprite sprite = quad.sprite();
        int[] vertexData = quad.vertices();
        // Make sure we don't change the original quad
        vertexData = Arrays.copyOf(vertexData, vertexData.length);

        // Adjust the uv
        int vertexSize = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
        int vertices = vertexData.length / vertexSize;
        int uvOffset = BLOCK_VERTEX_DATA_UV_OFFSET / 4;

        TextureAtlasSprite newSprite = material.sprite;
        for(int i = 0; i < vertices; i++){
            int offset = i * vertexSize;

            // UV
            float u = Float.intBitsToFloat(vertexData[offset + uvOffset]);
            float newU = newSprite.getU0() + (u - sprite.getU0()) / (sprite.getU1() - sprite.getU0()) * (newSprite.getU1() - newSprite.getU0());
            vertexData[offset + uvOffset] = Float.floatToRawIntBits(newU);
            float v = Float.intBitsToFloat(vertexData[offset + uvOffset + 1]);
            float newV = newSprite.getV0() + (v - sprite.getV0()) / (sprite.getV1() - sprite.getV0()) * (newSprite.getV1() - newSprite.getV0());
            vertexData[offset + uvOffset + 1] = Float.floatToRawIntBits(newV);
        }

        // Create a new quad
        return new BakedQuad(vertexData, quad.tintIndex(), quad.direction(), newSprite, material.shading, material.lightEmission, material.ambientOcclusion);
    }

    private static int findUVOffset(VertexFormat vertexFormat, VertexFormatElement.Usage usage){
        VertexFormatElement element = null;
        for(int index = 0; index < vertexFormat.getElements().size(); index++){
            VertexFormatElement el = vertexFormat.getElements().get(index);
            if(el.usage() == usage){
                element = el;
                break;
            }
        }
        if(element == null)
            throw new RuntimeException("Expected vertex format to have a '" + vertexFormat + "' attribute");
        return vertexFormat.getOffset(element);
    }

    @Override
    public TextureAtlasSprite particleIcon(ModelData modelData){
        Block base = modelData.get(BASE_BLOCK_PROPERTY);
        BlockState baseState;
        if(base == null || (baseState = base.defaultBlockState()).isAir())
            return this.original.particleIcon(modelData);
        BlockStateModel baseModel = ClientUtils.getBlockRenderer().getBlockModel(baseState);
        return baseModel.particleIcon(ModelData.EMPTY);
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.original.particleIcon();
    }

    private record MaterialEntry(TextureAtlasSprite sprite, boolean shading, int lightEmission, boolean ambientOcclusion) {
    }
}
