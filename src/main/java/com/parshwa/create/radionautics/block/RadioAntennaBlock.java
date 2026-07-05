package com.parshwa.create.radionautics.block;

import com.mojang.serialization.MapCodec;
import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RadioAntennaBlock extends BaseEntityBlock {
    public static final MapCodec<RadioAntennaBlock> CODEC = simpleCodec(properties -> new RadioAntennaBlock(AntennaTier.ANDESITE, properties));
    private static final VoxelShape SHAPE = Shapes.or(
            box(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D),
            box(7.0D, 2.0D, 7.0D, 9.0D, 15.0D, 9.0D),
            box(4.0D, 9.0D, 7.0D, 12.0D, 10.0D, 9.0D));
    private final AntennaTier tier;

    public RadioAntennaBlock(AntennaTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public AntennaTier tier() {
        return tier;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadioAntennaBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RadioBlockEntities.RADIO_ANTENNA.get(), RadioAntennaBlockEntity::tick);
    }
}
