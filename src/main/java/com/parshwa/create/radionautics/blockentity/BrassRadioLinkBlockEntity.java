package com.parshwa.create.radionautics.blockentity;

import com.parshwa.create.radionautics.radio.RadioCrypto;
import com.parshwa.create.radionautics.radio.RadioEndpoint;
import com.parshwa.create.radionautics.radio.RadioNetwork;
import com.parshwa.create.radionautics.block.BrassRadioLinkBlock;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BrassRadioLinkBlockEntity extends BlockEntity implements RadioEndpoint {
    private static final String ENCRYPTED_PREFIX = "ENC:";
    private static final String CLEAR_PREFIX = "RAW:";
    private UUID radioId = UUID.randomUUID();
    private String frequency = "145.500";
    private boolean receiver;
    private boolean encrypted;
    private String encryptionType = "none";
    private String encryptionKey = "";
    private String linkIdentifier = "LINK";
    private int lastInputStrength = -1;
    private int outputStrength;

    public BrassRadioLinkBlockEntity(BlockPos pos, BlockState blockState) {
        super(RadioBlockEntities.BRASS_RADIO_LINK.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BrassRadioLinkBlockEntity link) {
        if (level.isClientSide) {
            return;
        }

        RadioNetwork.registerLink(link);
        if (link.receiver) {
            return;
        }

        int strength = level.getBestNeighborSignal(pos);
        if (strength != link.lastInputStrength) {
            link.lastInputStrength = strength;
            link.transmitStrength(strength);
            link.setChanged();
        }
    }

    public String frequency() {
        return frequency;
    }

    public boolean isReceiver() {
        return receiver;
    }

    public int outputStrength() {
        return outputStrength;
    }

    public boolean encrypted() {
        return encrypted;
    }

    public String encryptionType() {
        return encryptionType;
    }

    public String encryptionKey() {
        return encryptionKey;
    }

    public String linkIdentifier() {
        return linkIdentifier;
    }

    public void setReceiver(boolean receiver) {
        this.receiver = receiver;
        syncBlockState();
        setChanged();
    }

    public void configure(String frequency, boolean receiver, boolean encrypted, String encryptionType, String encryptionKey, String linkIdentifier) {
        this.frequency = RadioAntennaBlockEntity.normalizeFrequency(frequency);
        this.receiver = receiver;
        this.encrypted = encrypted;
        this.encryptionType = encryptionType == null || encryptionType.isBlank() ? "none" : encryptionType;
        this.encryptionKey = encryptionKey == null ? "" : encryptionKey;
        this.linkIdentifier = linkIdentifier == null || linkIdentifier.isBlank() ? "LINK" : linkIdentifier;
        syncBlockState();
        setChanged();
        sendData();
    }

    public void receiveRadioStrength(String payload, int fallbackStrength) {
        int strength = fallbackStrength;
        if (encrypted) {
            if (!payload.startsWith(ENCRYPTED_PREFIX)) {
                return;
            }
            try {
                String decrypted = RadioCrypto.decryptToString(encryptionType, payload.substring(ENCRYPTED_PREFIX.length()), encryptionKey);
                String prefix = linkIdentifier + "_";
                if (!decrypted.startsWith(prefix)) {
                    return;
                }
                strength = Integer.parseInt(decrypted.substring(prefix.length()));
            } catch (RuntimeException exception) {
                return;
            }
        } else if (payload.startsWith(ENCRYPTED_PREFIX)) {
            return;
        } else if (payload.startsWith(CLEAR_PREFIX)) {
            try {
                strength = Integer.parseInt(payload.substring(CLEAR_PREFIX.length()));
            } catch (NumberFormatException exception) {
                return;
            }
        }
        outputStrength = Math.max(0, Math.min(15, strength));
        syncBlockState();
        setChanged();
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    private void transmitStrength(int strength) {
        syncBlockState();
        String payload = CLEAR_PREFIX + strength;
        if (encrypted) {
            payload = ENCRYPTED_PREFIX + RadioCrypto.encryptToString(encryptionType, linkIdentifier + "_" + strength, encryptionKey);
        }
        RadioNetwork.broadcastRedstone(this, frequency, payload, strength);
    }

    private void syncBlockState() {
        if (level == null || !(getBlockState().getBlock() instanceof BrassRadioLinkBlock)) {
            return;
        }
        boolean powered = receiver ? outputStrength > 0 : lastInputStrength > 0;
        BlockState state = getBlockState();
        BlockState updated = state
                .setValue(BrassRadioLinkBlock.RECEIVER, receiver)
                .setValue(BrassRadioLinkBlock.POWERED, powered);
        if (updated != state) {
            level.setBlock(worldPosition, updated, 3);
        }
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
        return Vec3.atCenterOf(worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        RadioNetwork.unregisterLink(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveRadioData(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadRadioData(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveRadioData(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void saveRadioData(CompoundTag tag) {
        tag.putUUID("RadioId", radioId);
        tag.putString("Frequency", frequency);
        tag.putBoolean("Receiver", receiver);
        tag.putBoolean("Encrypted", encrypted);
        tag.putString("EncryptionType", encryptionType);
        tag.putString("EncryptionKey", encryptionKey);
        tag.putString("LinkIdentifier", linkIdentifier);
        tag.putInt("LastInputStrength", lastInputStrength);
        tag.putInt("OutputStrength", outputStrength);
    }

    private void loadRadioData(CompoundTag tag) {
        if (tag.hasUUID("RadioId")) {
            radioId = tag.getUUID("RadioId");
        }
        frequency = tag.getString("Frequency").isBlank() ? "145.500" : tag.getString("Frequency");
        receiver = tag.getBoolean("Receiver");
        encrypted = tag.getBoolean("Encrypted");
        encryptionType = tag.getString("EncryptionType").isBlank() ? "none" : tag.getString("EncryptionType");
        encryptionKey = tag.getString("EncryptionKey");
        linkIdentifier = tag.getString("LinkIdentifier").isBlank() ? "LINK" : tag.getString("LinkIdentifier");
        lastInputStrength = tag.getInt("LastInputStrength");
        outputStrength = tag.getInt("OutputStrength");
    }

    private void sendData() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
