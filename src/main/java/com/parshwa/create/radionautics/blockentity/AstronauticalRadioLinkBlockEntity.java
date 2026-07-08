package com.parshwa.create.radionautics.blockentity;

import com.parshwa.create.radionautics.radio.AntennaTier;
import com.parshwa.create.radionautics.radio.RadioNetwork;
import com.parshwa.create.radionautics.radio.RadioRedstoneLink;
import com.parshwa.create.radionautics.radio.SablePositionHelper;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AstronauticalRadioLinkBlockEntity extends RadioAntennaBlockEntity implements RadioRedstoneLink {
    private UUID redstoneRadioId = UUID.randomUUID();
    private String frequency = "145.500";
    private boolean receiver;
    private int lastInputStrength = -1;
    private int outputStrength;
    private boolean sableForceLoadRequested;

    public AstronauticalRadioLinkBlockEntity(BlockPos pos, BlockState blockState) {
        super(RadioBlockEntities.ASTRONAUTICAL_RADIO_LINK.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AstronauticalRadioLinkBlockEntity link) {
        if (level.isClientSide) {
            return;
        }

        RadioNetwork.registerAntenna(link);
        RadioNetwork.registerLink(link);
        if (!link.sableForceLoadRequested) {
            link.sableForceLoadRequested = SablePositionHelper.forceLoadContainingSubLevel(link);
        }
        if (link.receiver) {
            return;
        }

        int strength = level.getBestNeighborSignal(pos);
        boolean changed = strength != link.lastInputStrength;
        link.lastInputStrength = strength;
        link.transmitStrength(strength);
        if (changed) {
            link.setChanged();
        }
    }

    @Override
    public String frequency() {
        return frequency;
    }

    @Override
    public boolean isReceiver() {
        return receiver;
    }

    @Override
    public int outputStrength() {
        return outputStrength;
    }

    @Override
    public void configure(String frequency, boolean receiver) {
        this.frequency = RadioAntennaBlockEntity.normalizeFrequency(frequency);
        this.receiver = receiver;
        setChanged();
        sendData();
    }

    @Override
    public void receiveRadioStrength(int strength) {
        outputStrength = Math.max(0, Math.min(15, strength));
        setChanged();
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public AntennaTier radioTier() {
        return AntennaTier.ASTRONAUTICAL;
    }

    private void transmitStrength(int strength) {
        RadioNetwork.broadcastRedstone(this, frequency, strength);
    }

    @Override
    public ResourceKey<Level> dimension() {
        return level == null ? null : level.dimension();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        RadioNetwork.unregisterLink(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveRadioLinkData(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadRadioLinkData(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveRadioLinkData(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void saveRadioLinkData(CompoundTag tag) {
        tag.putUUID("RedstoneRadioId", redstoneRadioId);
        tag.putString("LinkFrequency", frequency);
        tag.putBoolean("LinkReceiver", receiver);
        tag.putInt("LinkLastInputStrength", lastInputStrength);
        tag.putInt("LinkOutputStrength", outputStrength);
    }

    private void loadRadioLinkData(CompoundTag tag) {
        if (tag.hasUUID("RedstoneRadioId")) {
            redstoneRadioId = tag.getUUID("RedstoneRadioId");
        }
        frequency = tag.getString("LinkFrequency").isBlank() ? "145.500" : tag.getString("LinkFrequency");
        receiver = tag.getBoolean("LinkReceiver");
        lastInputStrength = tag.getInt("LinkLastInputStrength");
        outputStrength = tag.getInt("LinkOutputStrength");
    }

    private void sendData() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
