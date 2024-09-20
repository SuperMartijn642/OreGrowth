package com.supermartijn642.oregrowth.content;

import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.oregrowth.OreGrowth;
import com.supermartijn642.oregrowth.OreGrowthConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Created 04/10/2023 by SuperMartijn642
 */
public class OreGrowthBlock extends BaseBlock implements SimpleWaterloggedBlock {

    public static void trySpawnOreGrowth(OreGrowthRecipe recipe, ServerLevel level, BlockPos pos, RandomSource random){
        if(random.nextFloat() > recipe.spawnChance() * OreGrowthConfig.spawnChanceScalar.get())
            return;

        Direction side = Direction.values()[random.nextInt(Direction.values().length)];
        BlockPos growthPos = pos.relative(side);
        BlockState currentState = level.getBlockState(growthPos);
        if(!currentState.isAir() && !currentState.is(Blocks.WATER))
            return;

        level.setBlockAndUpdate(
            growthPos,
            OreGrowth.ORE_GROWTH_BLOCK.defaultBlockState()
                .setValue(FACE, side.getOpposite())
                .setValue(WATERLOGGED, currentState.getFluidState().getType() == Fluids.WATER)
        );
    }

    public static final int MAX_STAGES = 4;
    public static IntegerProperty STAGE = IntegerProperty.create("stage", 1, MAX_STAGES);
    public static EnumProperty<Direction> FACE = BlockStateProperties.FACING;
    public static BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

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

    public OreGrowthBlock(){
        super(false, BlockProperties.create().lootTable(BuiltInLootTables.EMPTY).randomTicks().destroyTime(0.5f).explosionResistance(0.5f).sound(SoundType.STONE));
        this.registerDefaultState(this.defaultBlockState().setValue(STAGE, 1).setValue(FACE, Direction.DOWN).setValue(WATERLOGGED, false));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random){
        Block base = level.getBlockState(pos.relative(state.getValue(FACE))).getBlock();
        OreGrowthRecipe recipe = OreGrowthRecipeManager.getRecipeFor(base);
        if(recipe == null){
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }
        int stage = state.getValue(STAGE);
        if(stage < recipe.stages() && random.nextFloat() < recipe.growthChance() * OreGrowthConfig.growthChanceScalar.get())
            level.setBlockAndUpdate(pos, state.setValue(STAGE, stage + 1));
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

        // Check if the base block would drop anything for the current tool
        Entity entity = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if(entity instanceof Player){
            if(!((Player)entity).hasCorrectToolForDrops(base, level, pos))
                return Collections.emptyList();
        }else if(entity instanceof LivingEntity){
            if(!((LivingEntity)entity).getMainHandItem().isCorrectToolForDrops(base))
                return Collections.emptyList();
        }else if(base.requiresCorrectToolForDrops())
            return Collections.emptyList();

        // Find the recipe for the base block and generate the drops
        OreGrowthRecipe recipe = OreGrowthRecipeManager.getRecipeFor(base.getBlock());
        if(recipe == null)
            return Collections.emptyList();
        LootParams lootParams = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
        return recipe.generateDrops(state, state.getValue(STAGE), lootParams);
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
        OreGrowthRecipe recipe = OreGrowthRecipeManager.getRecipeFor(base);
        if(recipe == null)
            return 0;
        int stage = state.getValue(STAGE);
        return (int)Math.floor((double)stage / recipe.stages() * 15);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context){
        return SHAPES_ROTATED[(state.getValue(STAGE) - 1) * 6 + state.getValue(FACE).ordinal()].getUnderlying();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(STAGE, FACE, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        BlockState state = this.defaultBlockState()
            .setValue(FACE, context.getClickedFace().getOpposite())
            .setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        return this.canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction neighborDirection, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos){
        if(!this.canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();
        if(state.getValue(WATERLOGGED))
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, neighborDirection, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos){
        Direction facing = state.getValue(FACE);
        return OreGrowthRecipeManager.getRecipeFor(level.getBlockState(pos.relative(facing)).getBlock()) != null;
    }

    @Override
    public FluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
