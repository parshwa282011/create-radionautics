package com.parshwa.create.radionautics.block;

import com.mojang.serialization.MapCodec;
import com.parshwa.create.radionautics.blockentity.ScramblerBlockEntity;
import com.parshwa.create.radionautics.menu.BrassRadioLinkMenu;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScramblerBlock extends BaseEntityBlock {
    public static final MapCodec<ScramblerBlock> CODEC = simpleCodec(properties -> new ScramblerBlock(ScramblerTier.COPPER, properties));
    private static final VoxelShape SHAPE = Shapes.or(
            box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D),
            box(4.0D, 3.0D, 4.0D, 12.0D, 11.0D, 12.0D),
            box(6.0D, 11.0D, 6.0D, 10.0D, 15.0D, 10.0D));
    private final ScramblerTier tier;

    public ScramblerBlock(ScramblerTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public ScramblerTier tier() {
        return tier;
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
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof ScramblerBlockEntity && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) -> new BrassRadioLinkMenu(containerId, inventory, pos),
                    Component.translatable("container.create_radio.scrambler")),
                    buffer -> {
                        buffer.writeBlockPos(pos);
                        BrassRadioLinkMenu.Snapshot.from(serverPlayer.getInventory(), pos).write(buffer);
                    });
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ScramblerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RadioBlockEntities.SCRAMBLER.get(), ScramblerBlockEntity::tick);
    }
}
