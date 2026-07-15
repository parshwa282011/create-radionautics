package com.parshwa.create.radionautics.radio;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class RadioNetwork {
    private static final List<RadioPacketEndpoint> ANTENNAS = new CopyOnWriteArrayList<>();
    private static final List<RadioRedstoneLink> LINKS = new CopyOnWriteArrayList<>();
    private static final List<RadioScrambler> SCRAMBLERS = new CopyOnWriteArrayList<>();

    private RadioNetwork() {
    }

    public static void registerAntenna(RadioPacketEndpoint antenna) {
        if (!ANTENNAS.contains(antenna)) {
            ANTENNAS.add(antenna);
        }
    }

    public static void unregisterAntenna(RadioPacketEndpoint antenna) {
        ANTENNAS.remove(antenna);
    }

    public static void registerLink(RadioRedstoneLink link) {
        if (!LINKS.contains(link)) {
            LINKS.add(link);
        }
    }

    public static void unregisterLink(RadioRedstoneLink link) {
        LINKS.remove(link);
    }

    public static void registerScrambler(RadioScrambler scrambler) {
        if (!SCRAMBLERS.contains(scrambler)) {
            SCRAMBLERS.add(scrambler);
        }
    }

    public static void unregisterScrambler(RadioScrambler scrambler) {
        SCRAMBLERS.remove(scrambler);
    }

    public static int broadcastPacket(RadioPacketEndpoint sender, String frequency, byte[] payload) {
        RadioPacket cleanPacket = RadioPacket.message(sender.radioId(), frequency, payload, sender.radioPosition());
        return broadcast(sender, cleanPacket);
    }

    public static int broadcastMedia(RadioPacketEndpoint sender, String frequency, byte[] payload,
                                     RadioMediaType type, String format, int width, int height, double durationSeconds) {
        UUID mediaId = type == RadioMediaType.AUDIO
                ? RadioMediaStore.storeAudio(sender, frequency, payload, format, durationSeconds)
                : null;
        byte[] transmittedPayload = type == RadioMediaType.AUDIO ? new byte[0] : payload;
        RadioPacket packet = new RadioPacket(sender.radioId(), frequency, transmittedPayload, sender.radioPosition(),
                type, format == null ? "" : format, width, height, durationSeconds, mediaId);
        return broadcast(sender, packet);
    }

    private static int broadcast(RadioPacketEndpoint sender, RadioPacket cleanPacket) {
        String frequency = cleanPacket.frequency();
        cleanup();
        int delivered = 0;
        boolean senderScrambled = isScrambled(sender, frequency);
        if (cleanPacket.mediaType() == RadioMediaType.MESSAGE) {
            RadioWebUBridge.record(senderScrambled ? scrambledPacket(cleanPacket, sender, sender) : cleanPacket, sender);
        }
        for (RadioPacketEndpoint receiver : ANTENNAS) {
            if (receiver == sender || receiver.isRemoved()) {
                continue;
            }
            if (receiver instanceof CreativeRadioMonitor) {
                authorizeMedia(cleanPacket, receiver);
                receiver.receivePacket(cleanPacket);
                continue;
            }
            if (!receiver.isFrequencyBound(frequency)) {
                continue;
            }
            if (!canReach(sender, receiver, sender.tier(), receiver.tier())) {
                continue;
            }
            boolean receiverScrambled = senderScrambled || isScrambled(receiver, frequency);
            if (!receiverScrambled) {
                authorizeMedia(cleanPacket, receiver);
            }
            receiver.receivePacket(receiverScrambled ? scrambledPacket(cleanPacket, sender, receiver) : cleanPacket);
            delivered++;
        }
        return delivered;
    }

    public static void broadcastRedstone(RadioRedstoneLink sender, String frequency, int strength) {
        cleanup();
        if (isScrambled(sender, frequency)) {
            return;
        }
        for (RadioRedstoneLink receiver : LINKS) {
            if (receiver == sender || receiver.isRemoved()) {
                continue;
            }
            if (!receiver.isReceiver() || !receiver.frequency().equals(frequency)) {
                continue;
            }
            if (isScrambled(receiver, frequency)) {
                continue;
            }
            receiver.receiveRadioStrength(strength);
        }
    }

    public static void clear() {
        ANTENNAS.clear();
        LINKS.clear();
        SCRAMBLERS.clear();
        RadioWebUBridge.clear();
        RadioMediaStore.clear();
    }

    private static void authorizeMedia(RadioPacket packet, RadioPacketEndpoint receiver) {
        if (packet.mediaId() != null) RadioMediaStore.allow(packet.mediaId(), receiver.radioId());
    }

    private static boolean canReach(RadioEndpoint a, RadioEndpoint b, AntennaTier aTier, AntennaTier bTier) {
        if (!sameDimension(a.dimension(), b.dimension())) {
            return aTier.crossDimensional() && bTier.crossDimensional();
        }
        int limitingRange = limitingRange(aTier, bTier);
        if (limitingRange < 0) {
            return true;
        }
        return a.radioPosition().distanceToSqr(b.radioPosition()) <= (double) limitingRange * limitingRange;
    }

    private static boolean sameDimension(ResourceKey<Level> a, ResourceKey<Level> b) {
        return a != null && a.equals(b);
    }

    private static boolean isScrambled(RadioEndpoint endpoint, String frequency) {
        for (RadioScrambler scrambler : SCRAMBLERS) {
            if (scrambler.isRemoved() || !frequency.equals(scrambler.frequency()) || !sameDimension(endpoint.dimension(), scrambler.dimension())) {
                continue;
            }
            int radius = scrambler.radiusBlocks();
            if (endpoint.radioPosition().distanceToSqr(scrambler.radioPosition()) <= (double) radius * radius) {
                return true;
            }
        }
        return false;
    }

    private static RadioPacket scrambledPacket(RadioPacket packet, RadioEndpoint sender, RadioEndpoint receiver) {
        byte[] scrambled = packet.payload().clone();
        long seed = packet.senderId().getMostSignificantBits()
                ^ packet.senderId().getLeastSignificantBits()
                ^ packet.frequency().hashCode()
                ^ sender.blockPos().asLong()
                ^ receiver.blockPos().asLong();
        Random random = new Random(seed);
        for (int i = 0; i < scrambled.length; i++) {
            scrambled[i] = (byte) (scrambled[i] ^ random.nextInt(256));
        }
        return packet.withPayload(scrambled);
    }

    private static int limitingRange(AntennaTier a, AntennaTier b) {
        if (a.infinite() && b.infinite()) {
            return -1;
        }
        if (a.infinite()) {
            return b.rangeBlocks();
        }
        if (b.infinite()) {
            return a.rangeBlocks();
        }
        return Math.min(a.rangeBlocks(), b.rangeBlocks());
    }

    private static void cleanup() {
        ANTENNAS.removeIf(RadioEndpoint::isRemoved);
        LINKS.removeIf(RadioRedstoneLink::isRemoved);
        SCRAMBLERS.removeIf(RadioScrambler::isRemoved);
    }
}
