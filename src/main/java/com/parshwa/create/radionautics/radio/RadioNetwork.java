package com.parshwa.create.radionautics.radio;

import com.parshwa.create.radionautics.blockentity.BrassRadioLinkBlockEntity;
import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class RadioNetwork {
    private static final List<RadioAntennaBlockEntity> ANTENNAS = new CopyOnWriteArrayList<>();
    private static final List<BrassRadioLinkBlockEntity> LINKS = new CopyOnWriteArrayList<>();

    private RadioNetwork() {
    }

    public static void registerAntenna(RadioAntennaBlockEntity antenna) {
        if (!ANTENNAS.contains(antenna)) {
            ANTENNAS.add(antenna);
        }
    }

    public static void unregisterAntenna(RadioAntennaBlockEntity antenna) {
        ANTENNAS.remove(antenna);
    }

    public static void registerLink(BrassRadioLinkBlockEntity link) {
        if (!LINKS.contains(link)) {
            LINKS.add(link);
        }
    }

    public static void unregisterLink(BrassRadioLinkBlockEntity link) {
        LINKS.remove(link);
    }

    public static int broadcastPacket(RadioAntennaBlockEntity sender, String frequency, byte[] payload) {
        cleanup();
        int delivered = 0;
        RadioPacket packet = new RadioPacket(sender.radioId(), frequency, payload);
        for (RadioAntennaBlockEntity receiver : ANTENNAS) {
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

    public static void broadcastRedstone(BrassRadioLinkBlockEntity sender, String frequency, String encryptedPayload, int fallbackStrength) {
        cleanup();
        for (BrassRadioLinkBlockEntity receiver : LINKS) {
            if (receiver == sender || receiver.isRemoved()) {
                continue;
            }
            if (!receiver.isReceiver() || !receiver.frequency().equals(frequency)) {
                continue;
            }
            if (!sameDimension(sender.dimension(), receiver.dimension())) {
                continue;
            }
            receiver.receiveRadioStrength(encryptedPayload, fallbackStrength);
        }
    }

    public static void clear() {
        ANTENNAS.clear();
        LINKS.clear();
    }

    private static boolean canReach(RadioEndpoint a, RadioEndpoint b, AntennaTier aTier, AntennaTier bTier) {
        if (!sameDimension(a.dimension(), b.dimension())) {
            return false;
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
        ANTENNAS.removeIf(RadioAntennaBlockEntity::isRemoved);
        LINKS.removeIf(BrassRadioLinkBlockEntity::isRemoved);
    }
}
