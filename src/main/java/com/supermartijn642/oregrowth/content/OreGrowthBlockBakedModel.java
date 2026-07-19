package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Holder;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.oregrowth.OreGrowth;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
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
    private final Map<Block,List<BlockStateModelPart>> meshCache = new HashMap<>();

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
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts){
        // Get the base block
        Block base = this.getBase(level, pos, state);
        if(base == null){
            this.original.collectParts(level, pos, state, random, parts);
            return;
        }

        // Get the mesh from cache
        List<BlockStateModelPart> mesh;
        synchronized(this.meshCache){
            mesh = this.meshCache.get(base);
        }

        // Compute the mesh if it doesn't exist yet
        if(mesh == null){
            mesh = this.computeMesh(
                model -> {
                    List<BlockStateModelPart> l = new ArrayList<>();
                    model.collectParts(level, pos, base.defaultBlockState(), random, l);
                    return l;
                },
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

    private List<BlockStateModelPart> computeMesh(Function<BlockStateModel,List<BlockStateModelPart>> modelEmitter, Block baseBlock){
        BlockState baseState = baseBlock.defaultBlockState();
        BlockStateModel baseModel = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(baseState);

        // Find the most occurring sprite
        Map<TextureAtlasSprite,Pair<Holder<Integer>,MaterialEntry>> spriteCounts = new HashMap<>();
        for(BlockStateModelPart part : modelEmitter.apply(baseModel)){
            for(Direction cullFace : MODEL_DIRECTIONS){
                part.getQuads(cullFace)
                    .forEach(quad -> {
                        BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
                        TextureAtlasSprite sprite = materialInfo.sprite();
                        Holder<Integer> count = spriteCounts.computeIfAbsent(sprite, s -> Pair.of(new Holder<>(0), new MaterialEntry(sprite, materialInfo.shade(), materialInfo.lightEmission(), part.ambientOcclusion()))).left();
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
        for(BlockStateModelPart part : modelEmitter.apply(this.original)){
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
        TriState ambientOcclusion = material.ambientOcclusion;
        return List.of(new BlockStateModelPart() {
            @Override
            public List<BakedQuad> getQuads(@Nullable Direction cullDirection){
                return cullDirection == null ? directionLessQuads : quads.get(cullDirection);
            }

            @Override
            public Material.Baked particleMaterial(){
                return new Material.Baked(sprite, false);
            }

            @Override
            public boolean useAmbientOcclusion(){
                return ambientOcclusion != TriState.FALSE;
            }

            @Override
            public TriState ambientOcclusion(){
                return ambientOcclusion;
            }

            @Override
            public @BakedQuad.MaterialFlags int materialFlags(){
                int flags = 0;
                if(sprite.transparency().hasTranslucent())
                    flags |= BakedQuad.FLAG_TRANSLUCENT;
                if(sprite.contents().isAnimated())
                    flags |= BakedQuad.FLAG_ANIMATED;
                return flags;
            }
        });
    }

    private static BakedQuad remapQuad(BakedQuad quad, MaterialEntry material){
        TextureAtlasSprite oldSprite = quad.materialInfo().sprite();
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
            quad.direction(),
            new BakedQuad.MaterialInfo(
                newSprite,
                ChunkSectionLayer.byTransparency(newSprite.transparency()),
                newSprite.transparency().hasTranslucent() ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet(),
                quad.materialInfo().tintIndex(),
                material.shading,
                material.lightEmission,
                material.ambientOcclusion == TriState.DEFAULT ? quad.materialInfo().ambientOcclusion() : material.ambientOcclusion == TriState.TRUE
            )
        );
    }

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){// Get the base block
        Block base = this.getBase(level, pos, state);
        if(base == null)
            return this.original.createGeometryKey(level, pos, state, random);
        BlockState baseState = base.defaultBlockState();
        if(baseState.isAir())
            return this.original.createGeometryKey(level, pos, state, random);
        BlockStateModel baseModel = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(baseState);
        return Pair.of(this, baseModel.createGeometryKey(level, pos, baseState, random));
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state){
        Block base = this.getBase(level, pos, state);
        if(base == null)
            return this.original.particleMaterial(level, pos, state);
        BlockState baseState = base.defaultBlockState();
        if(baseState.isAir())
            return this.original.particleMaterial(level, pos, state);
        BlockStateModel baseModel = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(baseState);
        return baseModel.particleMaterial(level, pos, baseState);
    }

    @Override
    public Material.Baked particleMaterial(){
        return this.original.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state){
        Block base = this.getBase(level, pos, state);
        if(base == null)
            return this.original.materialFlags(level, pos, state);
        BlockState baseState = base.defaultBlockState();
        if(baseState.isAir())
            return this.original.materialFlags(level, pos, state);
        BlockStateModel baseModel = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(baseState);
        return baseModel.materialFlags(level, pos, baseState);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, @BakedQuad.MaterialFlags int flag){
        Block base = this.getBase(level, pos, state);
        if(base == null)
            return this.original.hasMaterialFlag(level, pos, state, flag);
        BlockState baseState = base.defaultBlockState();
        if(baseState.isAir())
            return this.original.hasMaterialFlag(level, pos, state, flag);
        BlockStateModel baseModel = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(baseState);
        return baseModel.hasMaterialFlag(level, pos, baseState, flag);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(){
        return this.original.materialFlags();
    }

    @Override
    public boolean hasMaterialFlag(@BakedQuad.MaterialFlags int flag){
        return this.original.hasMaterialFlag(flag);
    }

    private record MaterialEntry(TextureAtlasSprite sprite, boolean shading, int lightEmission, TriState ambientOcclusion) {
    }
}
