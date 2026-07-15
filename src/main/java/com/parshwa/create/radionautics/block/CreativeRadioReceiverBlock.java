package com.parshwa.create.radionautics.block;

import com.parshwa.create.radionautics.blockentity.CreativeRadioReceiverBlockEntity;
import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CreativeRadioReceiverBlock extends RadioAntennaBlock {
    public CreativeRadioReceiverBlock(Properties properties) {
        super(AntennaTier.MEGA, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CreativeRadioReceiverBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RadioBlockEntities.CREATIVE_RADIO_RECEIVER.get(), CreativeRadioReceiverBlockEntity::tick);
    }
}
