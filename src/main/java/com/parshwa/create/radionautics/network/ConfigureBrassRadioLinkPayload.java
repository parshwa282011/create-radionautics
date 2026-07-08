package com.parshwa.create.radionautics.network;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.radio.RadioRedstoneLink;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ConfigureBrassRadioLinkPayload(
        BlockPos pos,
        String frequency,
        boolean receiver) implements CustomPacketPayload {
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
            if (context.player().level().getBlockEntity(pos) instanceof RadioRedstoneLink link
                    && context.player().distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D) {
                link.configure(frequency, receiver);
            }
        });
    }

    private static ConfigureBrassRadioLinkPayload read(RegistryFriendlyByteBuf buffer) {
        return new ConfigureBrassRadioLinkPayload(
                buffer.readBlockPos(),
                buffer.readUtf(128),
                buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(frequency, 128);
        buffer.writeBoolean(receiver);
    }
}
