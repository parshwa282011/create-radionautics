package com.parshwa.create.radionautics.blockentity;

import com.parshwa.create.radionautics.block.RadioAntennaBlock;
import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.radio.RadioEndpoint;
import com.parshwa.create.radionautics.radio.RadioNetwork;
import com.parshwa.create.radionautics.radio.RadioPacket;
import com.parshwa.create.radionautics.radio.RadioPacketEndpoint;
import com.parshwa.create.radionautics.radio.SablePositionHelper;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;

public class RadioAntennaBlockEntity extends BlockEntity implements RadioPacketEndpoint {
    private final Queue<RadioPacket> queuedPackets = new ArrayDeque<>();
    private final Set<String> boundFrequencies = new HashSet<>();
    private final List<RadioPacketEndpoint.PacketListener> packetListeners = new CopyOnWriteArrayList<>();
    private UUID radioId = UUID.randomUUID();
    private boolean continousLoad;

    public RadioAntennaBlockEntity(BlockPos pos, BlockState blockState) {
        super(RadioBlockEntities.RADIO_ANTENNA.get(), pos, blockState);
    }

    protected RadioAntennaBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RadioAntennaBlockEntity antenna) {
        if (!level.isClientSide) {
            RadioNetwork.registerAntenna(antenna);
            com.parshwa.create.radionautics.radio.RadioMediaStore.tickCleanup();
        }
    }

    public AntennaTier tier() {
        if (getBlockState().getBlock() instanceof RadioAntennaBlock antennaBlock) {
            return antennaBlock.tier();
        }
        return AntennaTier.ANDESITE;
    }

    public UUID radioId() {
        return radioId;
    }

    public boolean bindFrequency(String frequency) {
        String normalized = normalizeFrequency(frequency);
        if (boundFrequencies.contains(normalized)) {
            return true;
        }
        if (boundFrequencies.size() >= tier().maxBoundFrequencies()) {
            return false;
        }
        boundFrequencies.add(normalized);
        setChanged();
        return true;
    }

    public boolean unbindFrequency(String frequency) {
        boolean removed = boundFrequencies.remove(normalizeFrequency(frequency));
        if (removed) {
            setChanged();
        }
        return removed;
    }

    public boolean isFrequencyBound(String frequency) {
        return boundFrequencies.contains(normalizeFrequency(frequency));
    }

    public Set<String> boundFrequencies() {
        return Set.copyOf(boundFrequencies);
    }

    public int send(String frequency, byte[] payload) {
        if (level == null || level.isClientSide) {
            return 0;
        }
        return RadioNetwork.broadcastPacket(this, normalizeFrequency(frequency), payload);
    }

    @Override
    public void receivePacket(RadioPacket packet) {
        queuedPackets.add(packet);
        notifyPacketListeners(packet);
        setChanged();
    }

    protected void notifyPacketListeners(RadioPacket packet) {
        for (RadioPacketEndpoint.PacketListener listener : packetListeners) {
            listener.onPacket(packet);
        }
    }

    public RadioPacket pollPacket() {
        RadioPacket packet = queuedPackets.poll();
        if (packet != null) {
            setChanged();
        }
        return packet;
    }

    public int queuedPacketCount() {
        return queuedPackets.size();
    }

    public boolean continousLoad() {
        return continousLoad;
    }

    public void setContinousLoad(boolean continousLoad) {
        this.continousLoad = continousLoad;
        setChanged();
    }

    public Vec3 shipCenterPos() {
        return SablePositionHelper.shipCenterPosition(this);
    }

    @Override
    public Level level() {
        return level;
    }

    @Override
    public Quaterniondc shipRotation() {
        return SablePositionHelper.shipRotation(this);
    }

    @Override
    public ResourceKey<Level> dimension() {
        return level == null ? null : level.dimension();
    }

    @Override
    public BlockPos blockPos() {
        return worldPosition;
    }

    @Override
    public Vec3 radioPosition() {
        return SablePositionHelper.radioPosition(level, worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        RadioNetwork.unregisterAntenna(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("RadioId", radioId);
        tag.putBoolean("ContinousLoad", continousLoad);
        ListTag frequencies = new ListTag();
        for (String frequency : boundFrequencies) {
            frequencies.add(StringTag.valueOf(frequency));
        }
        tag.put("BoundFrequencies", frequencies);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("RadioId")) {
            radioId = tag.getUUID("RadioId");
        }
        continousLoad = tag.getBoolean("ContinousLoad");
        boundFrequencies.clear();
        ListTag frequencies = tag.getList("BoundFrequencies", 8);
        for (int i = 0; i < frequencies.size(); i++) {
            boundFrequencies.add(frequencies.getString(i));
        }
    }

    public static String normalizeFrequency(String frequency) {
        return frequency == null ? "" : frequency.trim();
    }

    @Override
    public void addPacketListener(RadioPacketEndpoint.PacketListener listener) {
        packetListeners.add(listener);
    }

    @Override
    public void removePacketListener(RadioPacketEndpoint.PacketListener listener) {
        packetListeners.remove(listener);
    }
}
