package com.parshwa.create.radionautics.block;

import com.mojang.serialization.MapCodec;
import com.parshwa.create.radionautics.blockentity.BrassRadioLinkBlockEntity;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BrassRadioLinkBlock extends BaseEntityBlock {
    public static final MapCodec<BrassRadioLinkBlock> CODEC = simpleCodec(BrassRadioLinkBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty RECEIVER = BooleanProperty.create("receiver");
    private static final VoxelShape UP_SHAPE = Shapes.or(
            box(2.0D, 0.0D, 2.0D, 14.0D, 3.5D, 14.0D),
            box(0.0D, 1.0D, 3.0D, 3.0D, 11.0D, 6.0D));
    private static final VoxelShape DOWN_SHAPE = Shapes.or(
            box(2.0D, 12.5D, 2.0D, 14.0D, 16.0D, 14.0D),
            box(0.0D, 5.0D, 3.0D, 3.0D, 15.0D, 6.0D));
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            box(3.0D, 1.0D, 0.0D, 13.0D, 15.0D, 4.0D),
            box(3.0D, 3.0D, 0.0D, 6.0D, 5.0D, 10.0D));
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
            box(3.0D, 1.0D, 12.0D, 13.0D, 15.0D, 16.0D),
            box(3.0D, 3.0D, 6.0D, 6.0D, 5.0D, 16.0D));
    private static final VoxelShape WEST_SHAPE = Shapes.or(
            box(0.0D, 1.0D, 3.0D, 4.0D, 15.0D, 13.0D),
            box(0.0D, 3.0D, 3.0D, 10.0D, 5.0D, 6.0D));
    private static final VoxelShape EAST_SHAPE = Shapes.or(
            box(12.0D, 1.0D, 3.0D, 16.0D, 15.0D, 13.0D),
            box(6.0D, 3.0D, 3.0D, 16.0D, 5.0D, 6.0D));

    public BrassRadioLinkBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(POWERED, false)
                .setValue(RECEIVER, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    private static VoxelShape shapeFor(Direction facing) {
        return switch (facing) {
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            case UP -> UP_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(POWERED, false)
                .setValue(RECEIVER, false);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof BrassRadioLinkBlockEntity link && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) -> new com.parshwa.create.radionautics.menu.BrassRadioLinkMenu(containerId, inventory, pos),
                    Component.translatable("container.create_radio.brass_radio_link")),
                    buffer -> {
                        buffer.writeBlockPos(pos);
                        com.parshwa.create.radionautics.menu.BrassRadioLinkMenu.Snapshot.from(serverPlayer.getInventory(), pos).write(buffer);
                    });
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, RECEIVER);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof BrassRadioLinkBlockEntity link && link.isReceiver()) {
            return link.outputStrength();
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrassRadioLinkBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RadioBlockEntities.BRASS_RADIO_LINK.get(), BrassRadioLinkBlockEntity::tick);
    }
}
