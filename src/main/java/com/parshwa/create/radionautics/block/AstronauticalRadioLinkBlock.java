package com.parshwa.create.radionautics.block;

import com.mojang.serialization.MapCodec;
import com.parshwa.create.radionautics.blockentity.AstronauticalRadioLinkBlockEntity;
import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import com.parshwa.create.radionautics.menu.BrassRadioLinkMenu;
import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AstronauticalRadioLinkBlock extends RadioAntennaBlock {
    public static final MapCodec<AstronauticalRadioLinkBlock> CODEC = simpleCodec(AstronauticalRadioLinkBlock::new);
    public static final EnumProperty<AstronauticalRadioLinkPart> PART =
            EnumProperty.create("part", AstronauticalRadioLinkPart.class);

    private static final VoxelShape LOWER_SHAPE = Shapes.or(
            box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            box(6.0D, 3.0D, 6.0D, 10.0D, 10.0D, 10.0D),
            box(7.0D, 10.0D, 7.0D, 9.0D, 16.0D, 9.0D));
    private static final VoxelShape MIDDLE_SHAPE = Shapes.or(
            box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D),
            box(3.0D, 2.0D, 7.0D, 13.0D, 4.0D, 9.0D),
            box(7.0D, 2.0D, 3.0D, 9.0D, 4.0D, 13.0D),
            box(5.0D, 14.0D, 7.0D, 11.0D, 16.0D, 9.0D),
            box(7.0D, 14.0D, 5.0D, 9.0D, 16.0D, 11.0D));
    private static final VoxelShape UPPER_SHAPE = Shapes.or(
            box(7.0D, 0.0D, 7.0D, 9.0D, 13.0D, 9.0D),
            box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D));

    public AstronauticalRadioLinkBlock(Properties properties) {
        super(AntennaTier.ASTRONAUTICAL, properties);
        registerDefaultState(stateDefinition.any().setValue(PART, AstronauticalRadioLinkPart.LOWER));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(PART));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(PART));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() >= level.getMaxBuildHeight() - 2) {
            return null;
        }
        if (!level.getBlockState(pos.above()).canBeReplaced(context)
                || !level.getBlockState(pos.above(2)).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState().setValue(PART, AstronauticalRadioLinkPart.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(PART, AstronauticalRadioLinkPart.MIDDLE), 3);
        level.setBlock(pos.above(2), state.setValue(PART, AstronauticalRadioLinkPart.UPPER), 3);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        AstronauticalRadioLinkPart part = state.getValue(PART);
        if (facing.getAxis() == Direction.Axis.Y && !validNeighbor(part, facing, facingState)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        AstronauticalRadioLinkPart part = state.getValue(PART);
        if (part == AstronauticalRadioLinkPart.LOWER) {
            return true;
        }

        BlockState below = level.getBlockState(pos.below());
        if (!below.is(this)) {
            return false;
        }
        AstronauticalRadioLinkPart belowPart = below.getValue(PART);
        return part == AstronauticalRadioLinkPart.MIDDLE
                ? belowPart == AstronauticalRadioLinkPart.LOWER
                : belowPart == AstronauticalRadioLinkPart.MIDDLE;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos basePos = basePos(pos, state.getValue(PART));
            if (!player.isCreative() && state.getValue(PART) != AstronauticalRadioLinkPart.LOWER) {
                dropResources(defaultBlockState().setValue(PART, AstronauticalRadioLinkPart.LOWER), level, basePos, null, player, player.getMainHandItem());
            }
            for (int i = 0; i < 3; i++) {
                BlockPos partPos = basePos.above(i);
                if (!partPos.equals(pos) && level.getBlockState(partPos).is(this)) {
                    level.setBlock(partPos, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        if (state.getValue(PART) == AstronauticalRadioLinkPart.LOWER) {
            super.playerDestroy(level, player, pos, state, blockEntity, stack);
        } else {
            super.playerDestroy(level, player, pos, Blocks.AIR.defaultBlockState(), blockEntity, stack);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == AstronauticalRadioLinkPart.LOWER
                ? new AstronauticalRadioLinkBlockEntity(pos, state)
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(PART) != AstronauticalRadioLinkPart.LOWER) {
            return null;
        }
        return createTickerHelper(type, RadioBlockEntities.ASTRONAUTICAL_RADIO_LINK.get(), AstronauticalRadioLinkBlockEntity::tick);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos basePos = basePos(pos, state.getValue(PART));
        if (level.getBlockEntity(basePos) instanceof AstronauticalRadioLinkBlockEntity link && link.isReceiver()) {
            return link.outputStrength();
        }
        return 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos basePos = basePos(pos, state.getValue(PART));
        if (level.getBlockEntity(basePos) instanceof AstronauticalRadioLinkBlockEntity && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) -> new BrassRadioLinkMenu(containerId, inventory, basePos),
                    Component.translatable("container.create_radio.astronautical_radio_link")),
                    buffer -> {
                        buffer.writeBlockPos(basePos);
                        BrassRadioLinkMenu.Snapshot.from(serverPlayer.getInventory(), basePos).write(buffer);
                    });
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    private static boolean validNeighbor(AstronauticalRadioLinkPart part, Direction facing, BlockState facingState) {
        if ((part == AstronauticalRadioLinkPart.LOWER && facing != Direction.UP)
                || (part == AstronauticalRadioLinkPart.UPPER && facing != Direction.DOWN)) {
            return true;
        }
        if (!(facingState.getBlock() instanceof AstronauticalRadioLinkBlock)) {
            return false;
        }
        AstronauticalRadioLinkPart neighborPart = facingState.getValue(PART);
        return switch (part) {
            case LOWER -> facing == Direction.UP && neighborPart == AstronauticalRadioLinkPart.MIDDLE;
            case MIDDLE -> (facing == Direction.DOWN && neighborPart == AstronauticalRadioLinkPart.LOWER)
                    || (facing == Direction.UP && neighborPart == AstronauticalRadioLinkPart.UPPER);
            case UPPER -> facing == Direction.DOWN && neighborPart == AstronauticalRadioLinkPart.MIDDLE;
        };
    }

    private static BlockPos basePos(BlockPos pos, AstronauticalRadioLinkPart part) {
        return switch (part) {
            case LOWER -> pos;
            case MIDDLE -> pos.below();
            case UPPER -> pos.below(2);
        };
    }

    private static VoxelShape shapeFor(AstronauticalRadioLinkPart part) {
        return switch (part) {
            case LOWER -> LOWER_SHAPE;
            case MIDDLE -> MIDDLE_SHAPE;
            case UPPER -> UPPER_SHAPE;
        };
    }
}
