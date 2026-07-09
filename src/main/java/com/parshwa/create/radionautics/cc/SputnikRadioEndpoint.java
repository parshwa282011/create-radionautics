package com.parshwa.create.radionautics.cc;

import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.radio.RadioNetwork;
import com.parshwa.create.radionautics.radio.RadioPacket;
import com.parshwa.create.radionautics.radio.RadioPacketEndpoint;
import com.parshwa.create.radionautics.radio.SablePositionHelper;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class SputnikRadioEndpoint implements RadioPacketEndpoint {
    private static final Map<EndpointKey, SputnikRadioEndpoint> ENDPOINTS = new ConcurrentHashMap<>();

    private final Queue<RadioPacket> queuedPackets = new ArrayDeque<>();
    private final Set<String> boundFrequencies = new HashSet<>();
    private final List<PacketListener> packetListeners = new CopyOnWriteArrayList<>();
    private final UUID radioId = UUID.randomUUID();
    private BlockEntity blockEntity;
    private boolean continousLoad;

    private SputnikRadioEndpoint(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public static SputnikRadioEndpoint getOrCreate(BlockEntity blockEntity) {
        ENDPOINTS.entrySet().removeIf(entry -> entry.getValue().isRemoved());
        EndpointKey key = EndpointKey.of(blockEntity);
        SputnikRadioEndpoint endpoint = ENDPOINTS.computeIfAbsent(key, ignored -> new SputnikRadioEndpoint(blockEntity));
        endpoint.blockEntity = blockEntity;
        RadioNetwork.registerAntenna(endpoint);
        return endpoint;
    }

    public static void clear() {
        ENDPOINTS.clear();
    }

    @Override
    public UUID radioId() {
        return radioId;
    }

    @Override
    public AntennaTier tier() {
        return AntennaTier.ASTRONAUTICAL;
    }

    @Override
    public boolean bindFrequency(String frequency) {
        String normalized = RadioAntennaBlockEntity.normalizeFrequency(frequency);
        if (boundFrequencies.contains(normalized)) {
            return true;
        }
        if (boundFrequencies.size() >= tier().maxBoundFrequencies()) {
            return false;
        }
        boundFrequencies.add(normalized);
        blockEntity.setChanged();
        return true;
    }

    @Override
    public boolean unbindFrequency(String frequency) {
        boolean removed = boundFrequencies.remove(RadioAntennaBlockEntity.normalizeFrequency(frequency));
        if (removed) {
            blockEntity.setChanged();
        }
        return removed;
    }

    @Override
    public boolean isFrequencyBound(String frequency) {
        return boundFrequencies.contains(RadioAntennaBlockEntity.normalizeFrequency(frequency));
    }

    @Override
    public Set<String> boundFrequencies() {
        return Set.copyOf(boundFrequencies);
    }

    @Override
    public int send(String frequency, byte[] payload) {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide) {
            return 0;
        }
        RadioNetwork.registerAntenna(this);
        return RadioNetwork.broadcastPacket(this, RadioAntennaBlockEntity.normalizeFrequency(frequency), payload);
    }

    @Override
    public void receivePacket(RadioPacket packet) {
        queuedPackets.add(packet);
        for (PacketListener listener : packetListeners) {
            listener.onPacket(packet);
        }
        blockEntity.setChanged();
    }

    @Override
    public RadioPacket pollPacket() {
        RadioPacket packet = queuedPackets.poll();
        if (packet != null) {
            blockEntity.setChanged();
        }
        return packet;
    }

    @Override
    public int queuedPacketCount() {
        return queuedPackets.size();
    }

    @Override
    public boolean continousLoad() {
        return continousLoad;
    }

    @Override
    public void setContinousLoad(boolean continousLoad) {
        this.continousLoad = continousLoad;
        if (continousLoad) {
            SablePositionHelper.forceLoadContainingSubLevel(blockEntity);
        }
        blockEntity.setChanged();
    }

    @Override
    public Level level() {
        return blockEntity.getLevel();
    }

    @Override
    public void addPacketListener(PacketListener listener) {
        packetListeners.add(listener);
    }

    @Override
    public void removePacketListener(PacketListener listener) {
        packetListeners.remove(listener);
    }

    @Override
    public ResourceKey<Level> dimension() {
        Level level = blockEntity.getLevel();
        return level == null ? null : level.dimension();
    }

    @Override
    public BlockPos blockPos() {
        return blockEntity.getBlockPos();
    }

    @Override
    public Vec3 radioPosition() {
        return SablePositionHelper.radioPosition(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    @Override
    public Vec3 shipCenterPos() {
        return SablePositionHelper.shipCenterPosition(blockEntity);
    }

    @Override
    public org.joml.Quaterniondc shipRotation() {
        return SablePositionHelper.shipRotation(blockEntity);
    }

    @Override
    public boolean isRemoved() {
        return blockEntity.isRemoved();
    }

    private record EndpointKey(String dimension, BlockPos pos) {
        private static EndpointKey of(BlockEntity blockEntity) {
            Level level = blockEntity.getLevel();
            String dimension = level == null ? "" : level.dimension().location().toString();
            return new EndpointKey(dimension, blockEntity.getBlockPos().immutable());
        }
    }
}
