package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.TextureAtlases;
import com.supermartijn642.core.util.Pair;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthBlockBakedModel implements BakedModel, FabricBakedModel {

    private static final Direction[] MODEL_DIRECTIONS = {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null};
    private static final RenderMaterial DEFAULT_MATERIAL = RendererAccess.INSTANCE.getRenderer().materialFinder().find();
    private static final RenderMaterial DEFAULT_MATERIAL_NO_AO = RendererAccess.INSTANCE.getRenderer().materialFinder().disableAo(0, true).find();

    private final BakedModel original;
    private final Mesh mesh;
    private final TextureAtlasSprite[] meshSprites;
    private final Map<Block,Pair<TextureAtlasSprite,RenderMaterial>> blockMaterialCache = new HashMap<>();
    private final Map<Block,Pair<TextureAtlasSprite,RenderMaterial>> itemMaterialCache = new HashMap<>();

    private Block baseBlockContext;

    public OreGrowthBlockBakedModel(BakedModel original){
        this.original = original;

        // Create a mesh from the original model's quads
        Renderer renderer = Objects.requireNonNull(RendererAccess.INSTANCE.getRenderer());
        MeshBuilder mesh = renderer.meshBuilder();
        QuadEmitter emitter = mesh.getEmitter();
        RenderMaterial material = original.useAmbientOcclusion() ? DEFAULT_MATERIAL : DEFAULT_MATERIAL_NO_AO;
        RandomSource random = RandomSource.create();
        List<TextureAtlasSprite> sprites = new ArrayList<>();
        for(Direction cullFace : MODEL_DIRECTIONS){
            List<BakedQuad> quads = original.getQuads(null, cullFace, random);
            for(BakedQuad quad : quads){
                emitter.fromVanilla(quad, material, cullFace);
                int spriteIndex = sprites.indexOf(quad.getSprite());
                if(spriteIndex == -1){
                    spriteIndex = sprites.size();
                    sprites.add(quad.getSprite());
                }
                emitter.tag(spriteIndex);
                emitter.emit();
            }
        }
        this.mesh = mesh.build();
        this.meshSprites = sprites.toArray(TextureAtlasSprite[]::new);
    }

    public void withContext(Block baseBlock, Runnable runnable){
        this.baseBlockContext = baseBlock;
        runnable.run();
        this.baseBlockContext = null;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context){
        // Get the base block
        Block base;
        if(this.baseBlockContext == null){
            BlockPos basePos = pos.relative(state.getValue(OreGrowthBlock.FACE));
            base = blockView.getBlockState(basePos).getBlock();
        }else
            base = this.baseBlockContext;

        // Emit the quads
        this.emitQuads(base, this.blockMaterialCache, model -> {
            BlockState baseState = base.defaultBlockState();
            BlockPos basePos = pos.relative(state.getValue(OreGrowthBlock.FACE));
            ((FabricBakedModel)model).emitBlockQuads(blockView, baseState, basePos, randomSupplier, context);
        }, context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context){
        // Get the base block
        Block base = this.baseBlockContext;
        if(base == null){
            context.meshConsumer().accept(this.mesh);
            return;
        }

        // Emit the quads
        this.emitQuads(base, this.itemMaterialCache, model -> ((FabricBakedModel)model).emitItemQuads(new ItemStack(base), randomSupplier, context), context);
    }

    private void emitQuads(Block base, Map<Block,Pair<TextureAtlasSprite,RenderMaterial>> materialCache, Consumer<BakedModel> modelEmitter, RenderContext context){
        // Get the texture and material to use for the base block
        Pair<TextureAtlasSprite,RenderMaterial> material;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized(materialCache){
            material = materialCache.get(base);
        }

        // Compute the material if it isn't in the cache yet
        if(material == null){
            material = findMaterial(base, modelEmitter, context);
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
            context.meshConsumer().accept(this.mesh);
            return;
        }

        // Push a transform which changes the quad's uv and material
        TextureAtlasSprite newSprite = material.left();
        RenderMaterial newMaterial = material.right();
        context.pushTransform(quad -> {
            TextureAtlasSprite originalSprite = this.meshSprites[quad.tag()];
            for(int i = 0; i < 4; i++){
                quad.sprite(i, 0,
                    newSprite.getU0() + (quad.spriteU(i, 0) - originalSprite.getU0()) / (originalSprite.getU1() - originalSprite.getU0()) * (newSprite.getU1() - newSprite.getU0()),
                    newSprite.getV0() + (quad.spriteV(i, 0) - originalSprite.getV0()) / (originalSprite.getV1() - originalSprite.getV0()) * (newSprite.getV1() - newSprite.getV0())
                );
                quad.material(newMaterial);
            }
            return true;
        });

        // Output the mesh
        context.meshConsumer().accept(this.mesh);
        context.popTransform();
    }

    private static Pair<TextureAtlasSprite,RenderMaterial> findMaterial(Block baseBlock, Consumer<BakedModel> modelEmitter, RenderContext context){
        BlockState baseState = baseBlock.defaultBlockState();
        BakedModel baseModel = ClientUtils.getBlockRenderer().getBlockModel(baseState);

        // Keep track of how many times a sprite occurs along with the material used
        Map<TextureAtlasSprite,Pair<Integer,RenderMaterial>> materials = new HashMap<>();

        // Push a transform to capture each quad
        SpriteFinder spriteFinder = SpriteFinder.get(ClientUtils.getMinecraft().getModelManager().getAtlas(TextureAtlases.getBlocks()));
        context.pushTransform(quad -> {
            TextureAtlasSprite sprite = spriteFinder.find(quad, 0);
            if(sprite != null)
                materials.compute(sprite, (s, pair) -> pair == null ? Pair.of(1, quad.material()) : pair.mapLeft(i -> i + 1));
            // Cancel all quads
            return false;
        });

        // Render the base block's model
        modelEmitter.accept(baseModel);
        context.popTransform();

        // If no quads were emitted, return null
        if(materials.isEmpty())
            return null;

        // Get the sprite which occurred most
        Pair<TextureAtlasSprite,RenderMaterial> material = null;
        int count = 0;
        for(Map.Entry<TextureAtlasSprite,Pair<Integer,RenderMaterial>> entry : materials.entrySet()){
            if(entry.getValue().left() > count){
                material = Pair.of(entry.getKey(), entry.getValue().right());
                count = entry.getValue().left();
            }
        }
        return material;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random){
        return this.original.getQuads(state, side, random);
    }

    @Override
    public boolean isVanillaAdapter(){
        return false;
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
        return this.original.getOverrides();
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
