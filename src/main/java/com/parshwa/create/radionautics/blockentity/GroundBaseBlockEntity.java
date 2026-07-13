package com.parshwa.create.radionautics.blockentity;

import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.radio.RadioPacket;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import com.parshwa.create.radionautics.registry.RadioBlocks;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GroundBaseBlockEntity extends RadioAntennaBlockEntity {
    public GroundBaseBlockEntity(BlockPos pos, BlockState blockState) {
        super(RadioBlockEntities.GROUND_BASE.get(), pos, blockState);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, GroundBaseBlockEntity base) {
        RadioAntennaBlockEntity.tick(level, pos, state, base);
    }

    @Override
    public AntennaTier tier() {
        return AntennaTier.MEGA;
    }

    @Override
    public boolean bindFrequency(String frequency) {
        return true;
    }

    @Override
    public boolean unbindFrequency(String frequency) {
        return true;
    }

    @Override
    public boolean isFrequencyBound(String frequency) {
        return isStructureComplete();
    }

    @Override
    public Set<String> boundFrequencies() {
        return Set.of("*");
    }

    @Override
    public int send(String frequency, byte[] payload) {
        return isStructureComplete() ? super.send(frequency, payload) : 0;
    }

    @Override
    public void receivePacket(RadioPacket packet) {
        if (isStructureComplete()) {
            super.receivePacket(packet);
        }
    }

    public boolean isStructureComplete() {
        if (level == null) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (!level.getBlockState(worldPosition.offset(dx, 0, dz)).is(RadioBlocks.GROUND_BASE_FRAME.get())) {
                    return false;
                }
            }
        }

        for (int y = 1; y <= 3; y++) {
            if (!level.getBlockState(worldPosition.above(y)).is(RadioBlocks.GROUND_BASE_MAST.get())) {
                return false;
            }
        }

        if (!level.getBlockState(worldPosition.offset(1, 3, 0)).is(RadioBlocks.GROUND_BASE_RECEIVER.get())) {
            return false;
        }
        if (!level.getBlockState(worldPosition.offset(-1, 3, 0)).is(RadioBlocks.GROUND_BASE_RECEIVER.get())) {
            return false;
        }
        if (!level.getBlockState(worldPosition.offset(0, 3, 1)).is(RadioBlocks.GROUND_BASE_RECEIVER.get())) {
            return false;
        }
        if (!level.getBlockState(worldPosition.offset(0, 3, -1)).is(RadioBlocks.GROUND_BASE_RECEIVER.get())) {
            return false;
        }

        return level.getBlockState(worldPosition.above(4)).is(RadioBlocks.GROUND_BASE_CAP.get());
    }
}
