package com.parshwa.create.radionautics.radio;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class RadioNetwork {
    private static final List<RadioPacketEndpoint> ANTENNAS = new CopyOnWriteArrayList<>();
    private static final List<RadioRedstoneLink> LINKS = new CopyOnWriteArrayList<>();

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

    public static int broadcastPacket(RadioPacketEndpoint sender, String frequency, byte[] payload) {
        cleanup();
        int delivered = 0;
        RadioPacket packet = new RadioPacket(sender.radioId(), frequency, payload);
        RadioWebUBridge.record(packet, sender);
        for (RadioPacketEndpoint receiver : ANTENNAS) {
            if (receiver == sender || receiver.isRemoved()) {
                continue;
            }
            if (!receiver.isFrequencyBound(frequency)) {
                continue;
            }
            if (!canReach(sender, receiver, sender.tier(), receiver.tier())) {
                continue;
            }
            receiver.receivePacket(packet);
            delivered++;
        }
        return delivered;
    }

    public static void broadcastRedstone(RadioRedstoneLink sender, String frequency, int strength) {
        cleanup();
        for (RadioRedstoneLink receiver : LINKS) {
            if (receiver == sender || receiver.isRemoved()) {
                continue;
            }
            if (!receiver.isReceiver() || !receiver.frequency().equals(frequency)) {
                continue;
            }
            receiver.receiveRadioStrength(strength);
        }
    }

    public static void clear() {
        ANTENNAS.clear();
        LINKS.clear();
        RadioWebUBridge.clear();
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
    }
}
