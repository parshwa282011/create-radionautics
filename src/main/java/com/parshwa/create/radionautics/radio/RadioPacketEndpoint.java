package com.parshwa.create.radionautics.radio;

import java.util.Set;
import java.util.UUID;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;

public interface RadioPacketEndpoint extends RadioEndpoint {
    UUID radioId();

    AntennaTier tier();

    boolean bindFrequency(String frequency);

    boolean unbindFrequency(String frequency);

    boolean isFrequencyBound(String frequency);

    Set<String> boundFrequencies();

    int send(String frequency, byte[] payload);

    void receivePacket(RadioPacket packet);

    RadioPacket pollPacket();

    int queuedPacketCount();

    boolean continousLoad();

    void setContinousLoad(boolean continousLoad);

    Level level();

    Vec3 shipCenterPos();

    Quaterniondc shipRotation();

    void addPacketListener(PacketListener listener);

    void removePacketListener(PacketListener listener);

    interface PacketListener {
        void onPacket(RadioPacket packet);
    }
}
