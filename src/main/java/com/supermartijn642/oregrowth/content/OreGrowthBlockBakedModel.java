package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.util.Pair;
import com.supermartijn642.oregrowth.OreGrowth;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
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
    private final Mesh mesh;
    private final TextureAtlasSprite[] meshSprites;
    private final Map<Block,MaterialEntry> blockMaterialCache = new HashMap<>();
    private final Map<Block,MaterialEntry> itemMaterialCache = new HashMap<>();

    private Block baseBlockContext;

    public OreGrowthBlockBakedModel(BlockStateModel original){
        this.original = original;

        // Create a mesh from the original model's quads
        Renderer renderer = Renderer.get();
        MutableMesh mesh = renderer.mutableMesh();
        QuadEmitter emitter = mesh.emitter();
        RandomSource random = RandomSource.create();
        List<TextureAtlasSprite> sprites = new ArrayList<>();
        for(BlockModelPart part : original.collectParts(random)){
            for(Direction cullFace : MODEL_DIRECTIONS){
                List<BakedQuad> quads = part.getQuads(cullFace);
                for(BakedQuad quad : quads){
                    emitter.fromBakedQuad(quad);
                    emitter.cullFace(cullFace);
                    int spriteIndex = sprites.indexOf(quad.sprite());
                    if(spriteIndex == -1){
                        spriteIndex = sprites.size();
                        sprites.add(quad.sprite());
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
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest){
        // Get the base block
        Block base = this.getBase(blockView, pos, state);
        if(base == null){
            this.mesh.outputTo(emitter);
            return;
        }

        // Emit the quads
        this.emitQuads(base, this.blockMaterialCache, (model, output) -> {
            BlockState baseState = base.defaultBlockState();
            BlockPos basePos = state.is(OreGrowth.ORE_GROWTH_BLOCK) ? pos.relative(state.getValue(OreGrowthBlock.FACE)) : pos;
            model.emitQuads(output, blockView, basePos, baseState, random, side -> false);
        }, emitter);
    }

    public void emitItemQuads(ItemStackRenderState.LayerRenderState renderLayer, RandomSource random){
        // Get the base block
        Block base = this.baseBlockContext;
        if(base == null){
            this.mesh.outputTo(renderLayer.emitter());
            return;
        }

        // Emit the quads
        this.emitQuads(base, this.itemMaterialCache, (model, output) -> {
            random.setSeed(42);
            model.emitQuads(output, EmptyLevelView.INSTANCE, BlockPos.ZERO, base.defaultBlockState(), random, direction -> false);
        }, renderLayer.emitter());
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
        BlockStateModel baseModel = ClientUtils.getBlockRenderer().getBlockModel(baseState);

        // Keep track of how many times a sprite occurs along with the material used
        Map<TextureAtlasSprite,Pair<Integer,MaterialEntry>> materials = new HashMap<>();

        // Create a dummy mesh to emit the model to
        MutableMesh dummyMesh = Renderer.get().mutableMesh();
        QuadEmitter emitter = dummyMesh.emitter();

        // Push a transform to capture each quad
        SpriteFinder spriteFinder = ClientUtils.getMinecraft().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).spriteFinder();
        emitter.pushTransform(quad -> {
            TextureAtlasSprite sprite = spriteFinder.find(quad);
            if(sprite != null)
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
    public @Nullable Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random){// Get the base block
        return Pair.of(this, this.getBase(blockView, pos, state));
    }

    @Override
    public TextureAtlasSprite particleSprite(BlockAndTintGetter blockView, BlockPos pos, BlockState state){
        Block base = this.getBase(blockView, pos, state);
        BlockState baseState = base.defaultBlockState();
        if(baseState.isAir())
            return this.original.particleSprite(blockView, pos, state);
        BlockStateModel baseModel = ClientUtils.getBlockRenderer().getBlockModel(baseState);
        return baseModel.particleSprite(blockView, pos, state);
    }

    @Override
    public void collectParts(RandomSource randomSource, List<BlockModelPart> list){
        this.original.collectParts(randomSource, list);
    }

    @Override
    public TextureAtlasSprite particleIcon(){
        return this.original.particleIcon();
    }

    private record MaterialEntry(TextureAtlasSprite sprite, boolean shading, boolean emissive, TriState ambientOcclusion) {
    }
}
