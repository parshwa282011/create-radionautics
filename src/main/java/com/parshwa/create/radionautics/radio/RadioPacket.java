package com.parshwa.create.radionautics.radio;

import java.util.UUID;
import net.minecraft.world.phys.Vec3;

public record RadioPacket(UUID senderId, String frequency, byte[] payload, Vec3 senderPosition,
                          RadioMediaType mediaType, String format, int width, int height, double durationSeconds,
                          UUID mediaId) {
    public static RadioPacket message(UUID senderId, String frequency, byte[] payload, Vec3 senderPosition) {
        return new RadioPacket(senderId, frequency, payload, senderPosition,
                RadioMediaType.MESSAGE, "utf-8", 0, 0, 0.0D, null);
    }

    public RadioPacket withPayload(byte[] replacement) {
        return new RadioPacket(senderId, frequency, replacement, senderPosition,
                mediaType, format, width, height, durationSeconds, mediaId);
    }
    public double distanceTo(RadioEndpoint endpoint) {
        return senderPosition.distanceTo(endpoint.radioPosition());
    }
}
