package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthBlockBakedModel implements DynamicBlockStateModel {

    public static final Direction[] MODEL_DIRECTIONS = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null};

    private final BlockStateModel original;
    private final Map<Block,List<BlockModelPart>> meshCache = new HashMap<>();

    private Block baseBlockContext;

    public OreGrowthBlockBakedModel(BlockStateModel original){
        this.original = original;
    }

    public void withContext(Block baseBlock, Runnable runnable){
        this.baseBlockContext = baseBlock;
        runnable.run();
        this.baseBlockContext = null;
    }

    private Block getBase(BlockAndTintGetter blockView, BlockPos pos, BlockState state){
        Block base;
        if(this.baseBlockContext == null){
            if(!OreGrowth.isOreGrowthBlock(state.getBlock()))
                return null;
            BlockPos basePos = pos.relative(state.getValue(OreGrowthBlock.FACE));
            base = blockView.getBlockState(basePos).getBlock();
        }else
            base = this.baseBlockContext;
        return base;
    }

    public Block getItemBaseBlockContext(){
        return this.baseBlockContext;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts){
        // Get the base block
        Block base = this.getBase(level, pos, state);
        if(base == null){
            this.original.collectParts(level, pos, state, random, parts);
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
                model -> model.collectParts(level, pos, base.defaultBlockState(), random),
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
                        Holder<Integer> count = spriteCounts.computeIfAbsent(sprite, s -> Pair.of(new Holder<>(0), new MaterialEntry(sprite, part.getRenderType(baseState), quad.shade(), quad.lightEmission(), part.ambientOcclusion()))).left();
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
        ChunkSectionLayer layer = material.layer;
        TriState ambientOcclusion = material.ambientOcclusion;
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
            public ChunkSectionLayer getRenderType(BlockState state){
                return layer;
            }

            @Override
            public boolean useAmbientOcclusion(){
                return ambientOcclusion != TriState.FALSE;
            }

            @Override
            public TriState ambientOcclusion(){
                return ambientOcclusion;
            }
        });
    }

    private static BakedQuad remapQuad(BakedQuad quad, MaterialEntry material){
        TextureAtlasSprite oldSprite = quad.sprite();
        TextureAtlasSprite newSprite = material.sprite;
        long[] uvs = new long[4];
        for(int i = 0; i < 4; i++){
            uvs[i] = UVPair.pack(
                (UVPair.unpackU(quad.packedUV(i)) - oldSprite.getU0()) / (oldSprite.getU1() - oldSprite.getU0()) * (newSprite.getU1() - newSprite.getU0()) + newSprite.getU0(),
                (UVPair.unpackV(quad.packedUV(i)) - oldSprite.getV0()) / (oldSprite.getV1() - oldSprite.getV0()) * (newSprite.getV1() - newSprite.getV0()) + newSprite.getV0()
            );
        }
        return new BakedQuad(
            quad.position0(), quad.position1(), quad.position2(), quad.position3(),
            uvs[0], uvs[1], uvs[2], uvs[3],
            quad.tintIndex(),
            quad.direction(),
            newSprite,
            material.shading,
            material.lightEmission,
            quad.bakedNormals(),
            quad.bakedColors(),
            material.ambientOcclusion.toBoolean(quad.hasAmbientOcclusion())
        );
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random){
        return Pair.of(this, this.getBase(blockView, pos, state));
    }

    @Override
    public TextureAtlasSprite particleIcon(BlockAndTintGetter blockView, BlockPos pos, BlockState state){
        Block base = this.getBase(blockView, pos, state);
        BlockState baseState;
        if(base == null || (baseState = base.defaultBlockState()).isAir())
            return this.original.particleIcon(blockView, pos, state);
        BlockStateModel baseModel = ClientUtils.getBlockRenderer().getBlockModel(baseState);
        return baseModel.particleIcon(blockView, pos, baseState);
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.original.particleIcon();
    }

    private record MaterialEntry(TextureAtlasSprite sprite, ChunkSectionLayer layer, boolean shading, int lightEmission, TriState ambientOcclusion) {
    }
}
