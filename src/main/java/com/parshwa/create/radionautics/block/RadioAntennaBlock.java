package com.parshwa.create.radionautics.block;

import com.mojang.serialization.MapCodec;
import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import net.minecraft.core.BlockPos;
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
