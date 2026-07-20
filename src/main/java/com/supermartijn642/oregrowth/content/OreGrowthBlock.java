package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.OreGrowthConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthBlock extends BaseBlock implements SimpleWaterloggedBlock {

    public static void trySpawnOreGrowth(BlockStateBase base, OreGrowthRecipe recipe, ServerLevel level, BlockPos pos, RandomSource random){
        if(random.nextFloat() > recipe.spawnChance() * OreGrowthConfig.spawnChanceScalar.get())
            return;

        Direction side = Direction.values()[random.nextInt(Direction.values().length)];
        BlockPos growthPos = pos.relative(side);
        BlockState currentState = level.getBlockState(growthPos);
        if(!currentState.isAir() && !currentState.is(Blocks.WATER))
            return;

        Block block = recipe.stages() > 1 ? OreGrowth.ORE_GROWTH_BLOCK : OreGrowth.COMPLETE_ORE_GROWTH_BLOCK;
        BlockState state = propertiesForBase(block.defaultBlockState(), base)
            .setValue(FACE, side.getOpposite())
            .setValue(WATERLOGGED, currentState.getFluidState().getType() == Fluids.WATER);
        level.setBlockAndUpdate(
            growthPos,
            state
        );
    }

    public static final int MAX_STAGES = 4;
    public static IntegerProperty STAGE = IntegerProperty.create("stage", 1, MAX_STAGES);
    public static EnumProperty<Direction> FACE = BlockStateProperties.FACING;
    public static BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static BooleanProperty REQUIRES_TOOL_FOR_DROPS = BooleanProperty.create("requires_tool_for_drops");
    public static EnumProperty<HarvestTool> HARVEST_TOOL = EnumProperty.create("harvest_tool", HarvestTool.class);
    public static EnumProperty<ToolTier> TOOL_TIER = EnumProperty.create("tool_tier", ToolTier.class);

    private static final BlockShape[] SHAPES = {
        BlockShape.createBlockShape(6.5, 0, 6.5, 9.5, 5, 9.5),
        BlockShape.createBlockShape(5.8, 0, 5.8, 10.2, 7, 10.2),
        BlockShape.createBlockShape(5.2, 0, 5.2, 10.8, 9, 10.8),
        BlockShape.createBlockShape(5.1, 0, 5.1, 10.9, 11, 10.9)
    };
    private static final BlockShape[] SHAPES_ROTATED = new BlockShape[SHAPES.length * 6];

    static{
        for(int stage = 0; stage < MAX_STAGES; stage++){
            BlockShape shape = SHAPES[stage];
            for(Direction face : Direction.values()){
                SHAPES_ROTATED[stage * 6 + face.ordinal()] =
                    face == Direction.UP ? shape.rotate(Direction.Axis.X).rotate(Direction.Axis.X)
                        : face == Direction.NORTH ? shape.rotate(Direction.Axis.X).rotate(Direction.Axis.Y).rotate(Direction.Axis.Y)
                        : face == Direction.EAST ? shape.rotate(Direction.Axis.X).rotate(Direction.Axis.Y).rotate(Direction.Axis.Y).rotate(Direction.Axis.Y)
                        : face == Direction.SOUTH ? shape.rotate(Direction.Axis.X)
                        : face == Direction.WEST ? shape.rotate(Direction.Axis.X).rotate(Direction.Axis.Y)
                        : shape;
            }
        }
    }

    private static BlockState propertiesForBase(BlockState state, BlockStateBase base){
        HarvestTool harvestTool = base.is(BlockTags.MINEABLE_WITH_PICKAXE) ? HarvestTool.PICKAXE
            : base.is(BlockTags.MINEABLE_WITH_AXE) ? HarvestTool.AXE
            : base.is(BlockTags.MINEABLE_WITH_SHOVEL) ? HarvestTool.SHOVEL
            : base.is(BlockTags.MINEABLE_WITH_HOE) ? HarvestTool.HOE
            : HarvestTool.NONE;
        ToolTier toolTier = base.is(BlockTags.NEEDS_DIAMOND_TOOL) ? ToolTier.DIAMOND
            : base.is(BlockTags.NEEDS_IRON_TOOL) ? ToolTier.IRON
            : base.is(BlockTags.NEEDS_STONE_TOOL) ? ToolTier.STONE
            : ToolTier.NONE;
        if(toolTier == ToolTier.NONE){
            if(base.is(BlockTags.INCORRECT_FOR_IRON_TOOL))
                toolTier = ToolTier.DIAMOND;
            else if(base.is(BlockTags.INCORRECT_FOR_STONE_TOOL))
                toolTier = ToolTier.IRON;
            else if(base.is(BlockTags.INCORRECT_FOR_WOODEN_TOOL))
                toolTier = ToolTier.STONE;
        }
        return state
            .setValue(REQUIRES_TOOL_FOR_DROPS, base.requiresCorrectToolForDrops())
            .setValue(HARVEST_TOOL, harvestTool)
            .setValue(TOOL_TIER, toolTier);
    }

    private static BlockState copyProperties(BlockState from, Block to){
        return to.defaultBlockState()
            .setValue(STAGE, from.getValue(STAGE))
            .setValue(FACE, from.getValue(FACE))
            .setValue(WATERLOGGED, from.getValue(WATERLOGGED))
            .setValue(REQUIRES_TOOL_FOR_DROPS, from.getValue(REQUIRES_TOOL_FOR_DROPS))
            .setValue(HARVEST_TOOL, from.getValue(HARVEST_TOOL))
            .setValue(TOOL_TIER, from.getValue(TOOL_TIER));
    }

    public OreGrowthBlock(){
        super(false, BlockProperties.create().noLootTable().randomTicks().destroyTime(0.5f).explosionResistance(0.5f).sound(SoundType.STONE));
        this.registerDefaultState(
            this.defaultBlockState()
                .setValue(STAGE, 1)
                .setValue(FACE, Direction.DOWN)
                .setValue(WATERLOGGED, false)
                .setValue(REQUIRES_TOOL_FOR_DROPS, false)
                .setValue(HARVEST_TOOL, HarvestTool.NONE)
                .setValue(TOOL_TIER, ToolTier.NONE)
        );
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random){
        BlockState base = level.getBlockState(pos.relative(state.getValue(FACE)));
        OreGrowthRecipe recipe = OreGrowthRecipeManager.get(level.isClientSide).getRecipeFor(base.getBlock());
        if(recipe == null){
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }
        int stage = state.getValue(STAGE);
        if(this == OreGrowth.ORE_GROWTH_BLOCK){ // Not-fully-grown
            if(stage == recipe.stages()){
                level.setBlockAndUpdate(pos, copyProperties(propertiesForBase(state, base), OreGrowth.COMPLETE_ORE_GROWTH_BLOCK));
                return;
            }
            if(stage < recipe.stages() && random.nextFloat() < recipe.growthChance() * OreGrowthConfig.growthChanceScalar.get()){
                if(stage + 1 == recipe.stages())
                    state = copyProperties(state, OreGrowth.COMPLETE_ORE_GROWTH_BLOCK);
                level.setBlockAndUpdate(pos, propertiesForBase(state, base).setValue(STAGE, stage + 1));
            }
        }else if(stage < recipe.stages()) // Fully grown
            level.setBlockAndUpdate(pos, copyProperties(propertiesForBase(state, base), OreGrowth.ORE_GROWTH_BLOCK));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder){
        // Find the base block
        Level level = builder.getLevel();
        if(level == null)
            return Collections.emptyList();
        Vec3 origin = builder.getParameter(LootContextParams.ORIGIN);
        BlockPos pos = new BlockPos((int)Math.floor(origin.x), (int)Math.floor(origin.y), (int)Math.floor(origin.z));
        Direction facing = state.getValue(FACE);
        BlockState base = level.getBlockState(pos.relative(facing));

        // Find the recipe for the base block and generate the drops
        OreGrowthRecipe recipe = OreGrowthRecipeManager.get(level.isClientSide).getRecipeFor(base.getBlock());
        if(recipe == null)
            return Collections.emptyList();
        LootParams lootParams = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
        return recipe.generateDrops(state, state.getValue(STAGE), lootParams);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player){
        pos = pos.relative(state.getValue(FACE));
        return level.getBlockState(pos).canHarvestBlock(level, pos, player);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos){
        pos = pos.relative(state.getValue(FACE));
        return level.getBlockState(pos).getDestroyProgress(player, level, pos);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity){
        Direction facing = state.getValue(FACE);
        Block base = level.getBlockState(pos.relative(facing)).getBlock();
        return base.getSoundType(state, level, pos, entity);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state){
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos){
        Block base = level.getBlockState(pos.relative(state.getValue(FACE))).getBlock();
        OreGrowthRecipe recipe = OreGrowthRecipeManager.get(level.isClientSide).getRecipeFor(base);
        if(recipe == null)
            return 0;
        int stage = state.getValue(STAGE);
        return (int)Math.floor((double)stage / recipe.stages() * 15);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        BlockShape shape = SHAPES_ROTATED[(state.getValue(STAGE) - 1) * 6 + state.getValue(FACE).ordinal()];
        Vec3 offset = this.getOffset(pos, state);
        return shape.offset(offset.x, offset.y, offset.z).getUnderlying();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(STAGE, FACE, WATERLOGGED, REQUIRES_TOOL_FOR_DROPS, HARVEST_TOOL, TOOL_TIER);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        Direction face = context.getClickedFace().getOpposite();
        Level level = context.getLevel();
        BlockState base = level.getBlockState(context.getClickedPos().relative(face));
        OreGrowthRecipe recipe = OreGrowthRecipeManager.get(level.isClientSide).getRecipeFor(base.getBlock());
        if(recipe == null)
            return null;
        BlockState state = propertiesForBase(this.defaultBlockState(), base)
            .setValue(FACE, face)
            .setValue(WATERLOGGED, level.getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        if(this == OreGrowth.COMPLETE_ORE_GROWTH_BLOCK)
            state = state.setValue(STAGE, recipe.stages());
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random){
        if(state.getBlock() != this || !this.canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();
        if(state.getValue(WATERLOGGED))
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return propertiesForBase(state, level.getBlockState(pos.relative(state.getValue(FACE))));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos){
        Direction facing = state.getValue(FACE);
        return OreGrowthRecipeManager.get(level.isClientSide()).getRecipeFor(level.getBlockState(pos.relative(facing)).getBlock()) != null;
    }

    @Override
    public FluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public Vec3 getOffset(BlockPos pos, BlockStateBase state){
        Direction facing = state.getValue(FACE);
        long seed = Mth.getSeed(pos.getX(), pos.getY(), pos.getZ());
        float maxOffset = 0.2f, maxSink = 0.08f;
        double xOffset = facing.getAxis() == Direction.Axis.X ?
            facing.getStepX() * ((seed & 15) / 15f) * maxSink :
            (((seed & 15) / 15f) - 0.5) * maxOffset;
        double yOffset = facing.getAxis() == Direction.Axis.Y ?
            facing.getStepY() * ((seed >> 4 & 15) / 15f) * maxSink :
            (((seed >> 4 & 15) / 15f) - 0.5) * maxOffset;
        double zOffset = facing.getAxis() == Direction.Axis.Z ?
            facing.getStepZ() * ((seed >> 8 & 15) / 15f) * maxSink :
            (((seed >> 8 & 15) / 15f) - 0.5) * maxOffset;
        return new Vec3(
            xOffset,
            yOffset,
            zOffset
        );
    }

    public boolean requiresCorrectToolForDrops(BlockStateBase state){
        return state.getValue(REQUIRES_TOOL_FOR_DROPS);
    }

    public boolean is(BlockStateBase state, TagKey<Block> tag){
        return tag.equals(state.getValue(HARVEST_TOOL).tag) || state.getValue(TOOL_TIER).tags.contains(tag);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state){
        return PushReaction.DESTROY;
    }

    public enum HarvestTool implements StringRepresentable {
        NONE(null),
        AXE(BlockTags.MINEABLE_WITH_AXE),
        HOE(BlockTags.MINEABLE_WITH_HOE),
        PICKAXE(BlockTags.MINEABLE_WITH_PICKAXE),
        SHOVEL(BlockTags.MINEABLE_WITH_SHOVEL);

        private final TagKey<Block> tag;

        HarvestTool(TagKey<Block> tag){
            this.tag = tag;
        }

        @Override
        public String getSerializedName(){
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public enum ToolTier implements StringRepresentable {
        NONE(),
        STONE(BlockTags.NEEDS_STONE_TOOL, BlockTags.INCORRECT_FOR_WOODEN_TOOL, BlockTags.INCORRECT_FOR_GOLD_TOOL),
        IRON(BlockTags.NEEDS_IRON_TOOL, BlockTags.INCORRECT_FOR_WOODEN_TOOL, BlockTags.INCORRECT_FOR_GOLD_TOOL, BlockTags.INCORRECT_FOR_STONE_TOOL),
        DIAMOND(BlockTags.NEEDS_DIAMOND_TOOL, BlockTags.INCORRECT_FOR_WOODEN_TOOL, BlockTags.INCORRECT_FOR_GOLD_TOOL, BlockTags.INCORRECT_FOR_STONE_TOOL, BlockTags.INCORRECT_FOR_IRON_TOOL);

        private final Set<TagKey<Block>> tags;

        ToolTier(TagKey<Block>... tags){
            this.tags = Set.of(tags);
        }

        @Override
        public String getSerializedName(){
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
