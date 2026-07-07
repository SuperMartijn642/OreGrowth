package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.oregrowth.OreGrowth;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthBlockBakedModel implements BlockStateModel {

    private static final Direction[] MODEL_DIRECTIONS = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null};

    private final BlockStateModel original;
    private Mesh mesh;
    private TextureAtlasSprite[] meshSprites;
    private final Map<Block,MaterialEntry> blockMaterialCache = new HashMap<>();
    private final Map<Block,MaterialEntry> itemMaterialCache = new HashMap<>();

    private Block baseBlockContext;

    public OreGrowthBlockBakedModel(BlockStateModel original){
        this.original = original;
    }

    private void createMesh(){
        if(this.mesh != null)
            return;
        // Create a mesh from the original model's quads
        Renderer renderer = Renderer.get();
        MutableMesh mesh = renderer.mutableMesh();
        QuadEmitter emitter = mesh.emitter();
        RandomSource random = RandomSource.create();
        List<TextureAtlasSprite> sprites = new ArrayList<>();
        SpriteFinder spriteFinder = ClientUtils.getMinecraft().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).spriteFinder();
        List<BlockStateModelPart> parts = new ArrayList<>();
        this.original.collectParts(random, parts);
        for(BlockStateModelPart part : parts){
            for(Direction cullFace : MODEL_DIRECTIONS){
                List<BakedQuad> quads = part.getQuads(cullFace);
                for(BakedQuad quad : quads){
                    emitter.fromBakedQuad(quad);
                    emitter.cullFace(cullFace);
                    TextureAtlasSprite sprite = spriteFinder.find(emitter);
                    int spriteIndex = sprites.indexOf(sprite);
                    if(spriteIndex == -1){
                        spriteIndex = sprites.size();
                        sprites.add(sprite);
                    }
                    emitter.tag(spriteIndex);
                    emitter.emit();
                }
            }
        }
        this.mesh = mesh.immutableCopy();
        this.meshSprites = sprites.toArray(TextureAtlasSprite[]::new);
    }

    public void withContext(Block baseBlock, Runnable runnable){
        this.baseBlockContext = baseBlock;
        runnable.run();
        this.baseBlockContext = null;
    }

    private Block getBase(BlockAndTintGetter blockView, BlockPos pos, BlockState state){
        Block base;
        if(this.baseBlockContext == null){
            if(!(state.getBlock() instanceof OreGrowthBlock))
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
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest){
        this.createMesh();

        // Get the base block
        Block base = this.getBase(level, pos, state);
        if(base == null){
            this.mesh.outputTo(emitter);
            return;
        }

        // Emit the quads
        this.emitQuads(base, this.blockMaterialCache, (model, output) -> {
            BlockState baseState = base.defaultBlockState();
            BlockPos basePos = state.is(OreGrowth.ORE_GROWTH_BLOCK) ? pos.relative(state.getValue(OreGrowthBlock.FACE)) : pos;
            model.emitQuads(output, level, basePos, baseState, random, side -> false);
        }, emitter);
    }

    public void emitItemQuads(QuadEmitter emitter, RandomSource random){
        this.createMesh();

        // Get the base block
        Block base = this.baseBlockContext;
        if(base == null){
            this.mesh.outputTo(emitter);
            return;
        }

        // Emit the quads
        this.emitQuads(base, this.itemMaterialCache, (model, output) -> {
            random.setSeed(42);
            model.emitQuads(output, EmptyLevelView.INSTANCE, BlockPos.ZERO, base.defaultBlockState(), random, direction -> false);
        }, emitter);
    }

    private void emitQuads(Block base, Map<Block,MaterialEntry> materialCache, BiConsumer<BlockStateModel,QuadEmitter> modelEmitter, QuadEmitter emitter){
        // Get the texture and material to use for the base block
        MaterialEntry material;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized(materialCache){
            material = materialCache.get(base);
        }

        // Compute the material if it isn't in the cache yet
        if(material == null){
            material = findMaterial(base, modelEmitter);
            // Update the cache
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized(materialCache){
                if(!materialCache.containsKey(base))
                    materialCache.put(base, material);
                else
                    material = materialCache.get(base);
            }
        }

        // If the material is null, just output the mesh
        if(material == null){
            this.mesh.outputTo(emitter);
            return;
        }

        // Push a transform which changes the quad's uv and material
        TextureAtlasSprite newSprite = material.sprite;
        boolean shading = material.shading;
        boolean emissive = material.emissive;
        TriState ambientOcclusion = material.ambientOcclusion;
        emitter.pushTransform(quad -> {
            TextureAtlasSprite originalSprite = this.meshSprites[quad.tag()];
            for(int i = 0; i < 4; i++){
                quad.uv(i,
                    newSprite.getU0() + (quad.u(i) - originalSprite.getU0()) / (originalSprite.getU1() - originalSprite.getU0()) * (newSprite.getU1() - newSprite.getU0()),
                    newSprite.getV0() + (quad.v(i) - originalSprite.getV0()) / (originalSprite.getV1() - originalSprite.getV0()) * (newSprite.getV1() - newSprite.getV0())
                );
                quad.diffuseShade(shading);
                quad.emissive(emissive);
                quad.ambientOcclusion(ambientOcclusion);
            }
            return true;
        });

        // Output the mesh
        this.mesh.outputTo(emitter);
        emitter.popTransform();
    }

    private static MaterialEntry findMaterial(Block baseBlock, BiConsumer<BlockStateModel,QuadEmitter> modelEmitter){
        BlockState baseState = baseBlock.defaultBlockState();
        BlockStateModel baseModel = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(baseState);

        // Keep track of how many times a sprite occurs along with the material used
        Map<TextureAtlasSprite,Pair<Integer,MaterialEntry>> materials = new HashMap<>();

        // Create a dummy mesh to emit the model to
        MutableMesh dummyMesh = Renderer.get().mutableMesh();
        QuadEmitter emitter = dummyMesh.emitter();

        // Push a transform to capture each quad
        SpriteFinder spriteFinder = ClientUtils.getMinecraft().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).spriteFinder();
        emitter.pushTransform(quad -> {
            TextureAtlasSprite sprite = spriteFinder.find(quad);
            materials.compute(sprite, (s, pair) -> pair == null ? Pair.of(1, new MaterialEntry(s, quad.diffuseShade(), quad.emissive(), quad.ambientOcclusion())) : pair.mapLeft(i -> i + 1));
            // Cancel all quads
            return false;
        });

        // Render the base block's model
        modelEmitter.accept(baseModel, emitter);
        emitter.popTransform();

        // If no quads were emitted, return null
        if(materials.isEmpty())
            return null;

        // Get the sprite which occurred most
        MaterialEntry material = null;
        int count = 0;
        for(Map.Entry<TextureAtlasSprite,Pair<Integer,MaterialEntry>> entry : materials.entrySet()){
            if(entry.getValue().left() > count){
                material = entry.getValue().right();
                count = entry.getValue().left();
            }
        }
        return material;
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
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts){
        this.original.collectParts(random, parts);
    }

    @Override
    public Material.Baked particleMaterial(){
        return this.original.particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random){
        Block base = this.getBase(level, pos, state);
        if(base == null)
            return this.original.materialFlags(level, pos, state, random);
        BlockState baseState = base.defaultBlockState();
        if(baseState.isAir())
            return this.original.materialFlags(level, pos, state, random);
        BlockStateModel baseModel = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(baseState);
        return baseModel.materialFlags(level, pos, baseState, random);
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, @BakedQuad.MaterialFlags int flag){
        Block base = this.getBase(level, pos, state);
        if(base == null)
            return this.original.hasMaterialFlag(level, pos, state, random, flag);
        BlockState baseState = base.defaultBlockState();
        if(baseState.isAir())
            return this.original.hasMaterialFlag(level, pos, state, random, flag);
        BlockStateModel baseModel = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(baseState);
        return baseModel.hasMaterialFlag(level, pos, baseState, random, flag);
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(){
        return this.original.materialFlags();
    }

    @Override
    public boolean hasMaterialFlag(@BakedQuad.MaterialFlags int flag){
        return this.original.hasMaterialFlag(flag);
    }

    private record MaterialEntry(TextureAtlasSprite sprite, boolean shading, boolean emissive, TriState ambientOcclusion) {
    }
}
