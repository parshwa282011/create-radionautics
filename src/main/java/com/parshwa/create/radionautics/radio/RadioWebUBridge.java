package com.parshwa.create.radionautics.radio;

import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.world.level.Level;

public final class RadioWebUBridge {
    private static final Map<String, LastMessage> LAST_MESSAGES = new ConcurrentHashMap<>();
    private static final AtomicLong MESSAGE_COUNTER = new AtomicLong();

    private RadioWebUBridge() {
    }

    public static void record(RadioPacket packet, RadioPacketEndpoint receiver) {
        Level level = receiver.level();
        long dayTime = level == null ? -1L : Math.floorMod(level.getDayTime(), 24000L);
        String payload = new String(packet.payload(), StandardCharsets.UTF_8);
        recordManual(new LastMessage(
                packet.frequency(),
                payload,
                stableHash(payload),
                parseDouble(payload),
                payload.length(),
                MESSAGE_COUNTER.incrementAndGet(),
                dayTime,
                System.currentTimeMillis()));
    }

    public static LastMessage lastMessage(String frequency) {
        return LAST_MESSAGES.get(RadioAntennaBlockEntity.normalizeFrequency(frequency));
    }

    public static void recordManual(LastMessage message) {
        LAST_MESSAGES.compute(RadioAntennaBlockEntity.normalizeFrequency(message.frequency()), (frequency, existing) -> {
            if (existing == null || message.receivedUtcMillis() >= existing.receivedUtcMillis()) {
                return message;
            }
            return existing;
        });
    }

    public static void clear() {
        LAST_MESSAGES.clear();
        MESSAGE_COUNTER.set(0L);
    }

    public static int charCode(String value, int index) {
        if (value == null || index < 1 || index > value.length()) {
            return 0;
        }
        return value.charAt(index - 1);
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (RuntimeException exception) {
            return Double.NaN;
        }
    }

    private static double stableHash(String value) {
        long hash = 1125899906842597L;
        for (int i = 0; i < value.length(); i++) {
            hash = 31L * hash + value.charAt(i);
        }
        return hash;
    }

    public record LastMessage(
            String frequency,
            String payload,
            double hash,
            double parsedNumber,
            int length,
            long messageId,
            long receivedDayTime,
            long receivedUtcMillis) {
    }
}
