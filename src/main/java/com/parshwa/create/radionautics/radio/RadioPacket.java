package com.parshwa.create.radionautics.radio;

import java.util.UUID;

public record RadioPacket(UUID senderId, String frequency, byte[] payload) {
}
