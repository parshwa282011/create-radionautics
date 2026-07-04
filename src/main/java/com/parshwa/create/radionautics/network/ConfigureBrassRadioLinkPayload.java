package com.parshwa.create.radionautics.network;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.blockentity.BrassRadioLinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConfigureBrassRadioLinkPayload(
        BlockPos pos,
        String frequency,
        boolean receiver,
        boolean encrypted,
        String encryptionType,
        String encryptionKey,
        String linkIdentifier) implements CustomPacketPayload {
    public static final Type<ConfigureBrassRadioLinkPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateRadionautics.MOD_ID, "configure_brass_radio_link"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureBrassRadioLinkPayload> STREAM_CODEC =
            StreamCodec.ofMember(ConfigureBrassRadioLinkPayload::write, ConfigureBrassRadioLinkPayload::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(pos) instanceof BrassRadioLinkBlockEntity link
                    && context.player().distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D) {
                link.configure(frequency, receiver, encrypted, encryptionType, encryptionKey, linkIdentifier);
            }
        });
    }

    private static ConfigureBrassRadioLinkPayload read(RegistryFriendlyByteBuf buffer) {
        return new ConfigureBrassRadioLinkPayload(
                buffer.readBlockPos(),
                buffer.readUtf(128),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readUtf(32),
                buffer.readUtf(256),
                buffer.readUtf(64));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(frequency, 128);
        buffer.writeBoolean(receiver);
        buffer.writeBoolean(encrypted);
        buffer.writeUtf(encryptionType, 32);
        buffer.writeUtf(encryptionKey, 256);
        buffer.writeUtf(linkIdentifier, 64);
    }
}
