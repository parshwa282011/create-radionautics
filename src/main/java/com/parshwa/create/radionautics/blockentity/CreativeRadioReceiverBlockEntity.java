package com.parshwa.create.radionautics.blockentity;

import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.radio.CreativeRadioMonitor;
import com.parshwa.create.radionautics.radio.RadioPacket;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeRadioReceiverBlockEntity extends RadioAntennaBlockEntity implements CreativeRadioMonitor {
    public CreativeRadioReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(RadioBlockEntities.CREATIVE_RADIO_RECEIVER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CreativeRadioReceiverBlockEntity receiver) {
        RadioAntennaBlockEntity.tick(level, pos, state, receiver);
    }

    @Override public AntennaTier tier() { return AntennaTier.MEGA; }
    @Override public boolean bindFrequency(String frequency) { return true; }
    @Override public boolean unbindFrequency(String frequency) { return true; }
    @Override public boolean isFrequencyBound(String frequency) { return true; }
    @Override public Set<String> boundFrequencies() { return Set.of("*"); }

    @Override
    public void receivePacket(RadioPacket packet) {
        // An always-on audit endpoint must not build an unbounded polling queue.
        notifyPacketListeners(packet);
    }
}
