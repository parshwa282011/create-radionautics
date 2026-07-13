package com.parshwa.create.radionautics.blockentity;

import com.parshwa.create.radionautics.block.ScramblerBlock;
import com.parshwa.create.radionautics.radio.RadioNetwork;
import com.parshwa.create.radionautics.radio.RadioScrambler;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
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

public class ScramblerBlockEntity extends BlockEntity implements RadioScrambler {
    private String frequency = "145.500";

    public ScramblerBlockEntity(BlockPos pos, BlockState blockState) {
        super(RadioBlockEntities.SCRAMBLER.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ScramblerBlockEntity scrambler) {
        if (!level.isClientSide) {
            RadioNetwork.registerScrambler(scrambler);
        }
    }

    @Override
    public String frequency() {
        return frequency;
    }

    @Override
    public void configureFrequency(String frequency) {
        this.frequency = RadioAntennaBlockEntity.normalizeFrequency(frequency);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public int radiusBlocks() {
        if (getBlockState().getBlock() instanceof ScramblerBlock scramblerBlock) {
            return scramblerBlock.tier().radiusBlocks();
        }
        return 100;
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
        RadioNetwork.unregisterScrambler(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Frequency", frequency);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        frequency = tag.getString("Frequency").isBlank() ? "145.500" : tag.getString("Frequency");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("Frequency", frequency);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
