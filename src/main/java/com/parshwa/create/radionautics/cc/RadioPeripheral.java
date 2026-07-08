package com.parshwa.create.radionautics.cc;

import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import com.parshwa.create.radionautics.radio.RadioCrypto;
import com.parshwa.create.radionautics.radio.RadioPacket;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.Nullable;

public class RadioPeripheral implements IPeripheral, RadioAntennaBlockEntity.PacketListener {
    private final RadioAntennaBlockEntity antenna;
    private final List<IComputerAccess> computers = new CopyOnWriteArrayList<>();

    public RadioPeripheral(RadioAntennaBlockEntity antenna) {
        this.antenna = antenna;
    }

    @Override
    public String getType() {
        return "radionautics_radio";
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        antenna.addPacketListener(this);
    }

    @Override
    public void detach(IComputerAccess computer) {
        computers.remove(computer);
        if (computers.isEmpty()) {
            antenna.removePacketListener(this);
        }
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof RadioPeripheral radio && radio.antenna == antenna;
    }

    @Override
    public void onPacket(RadioPacket packet) {
        String payload = new String(packet.payload(), StandardCharsets.UTF_8);
        for (IComputerAccess computer : computers) {
            computer.queueEvent("radio_message", packet.frequency(), packet.senderId().toString(), payload);
        }
    }

    @LuaFunction
    public final String tier() {
        return antenna.tier().serializedName();
    }

    @LuaFunction
    public final int range() {
        return antenna.tier().rangeBlocks();
    }

    @LuaFunction
    public final int maxBinds() {
        return antenna.tier().maxBoundFrequencies();
    }

    @LuaFunction
    public final boolean udpBind(String frequency) throws LuaException {
        requireFrequency(frequency);
        return antenna.bindFrequency(frequency);
    }

    @LuaFunction
    public final boolean tcpListen(String frequency) throws LuaException {
        requireFrequency(frequency);
        return antenna.bindFrequency(frequency);
    }

    @LuaFunction
    public final boolean close(String frequency) throws LuaException {
        requireFrequency(frequency);
        return antenna.unbindFrequency(frequency);
    }

    @LuaFunction
    public final List<String> boundFrequencies() {
        return new ArrayList<>(antenna.boundFrequencies());
    }

    @LuaFunction
    public final int send(String frequency, String payload) throws LuaException {
        requireFrequency(frequency);
        if (payload == null) {
            throw new LuaException("payload cannot be nil");
        }
        return antenna.send(frequency, payload.getBytes(StandardCharsets.UTF_8));
    }

    @LuaFunction
    public final Object[] receive() {
        RadioPacket packet = antenna.pollPacket();
        if (packet == null) {
            return null;
        }
        return new Object[] {
                packet.frequency(),
                packet.senderId().toString(),
                new String(packet.payload(), StandardCharsets.UTF_8)
        };
    }

    @LuaFunction
    public final int queuedMessages() {
        return antenna.queuedPacketCount();
    }

    @LuaFunction
    public final String cryptoEncrypt(String type, String data, String key) throws LuaException {
        if (data == null) {
            throw new LuaException("data cannot be nil");
        }
        try {
            byte[] encrypted = RadioCrypto.encrypt(type, data.getBytes(StandardCharsets.UTF_8), key);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (RuntimeException exception) {
            throw new LuaException(exception.getMessage());
        }
    }

    @LuaFunction
    public final String cryptoDecrypt(String type, String data, String key) throws LuaException {
        if (data == null) {
            throw new LuaException("data cannot be nil");
        }
        try {
            byte[] encrypted = Base64.getDecoder().decode(data);
            return new String(RadioCrypto.decrypt(type, encrypted, key), StandardCharsets.UTF_8);
        } catch (RuntimeException exception) {
            throw new LuaException(exception.getMessage());
        }
    }

    @LuaFunction
    public final String cryptoEncode(String type, String data) throws LuaException {
        if (!"base64".equalsIgnoreCase(type)) {
            throw new LuaException("only base64 encoding is supported");
        }
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    @LuaFunction
    public final String cryptoDecode(String type, String data) throws LuaException {
        if (!"base64".equalsIgnoreCase(type)) {
            throw new LuaException("only base64 decoding is supported");
        }
        return new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    }

    @LuaFunction
    public final void sableContinousLoad(boolean enabled) {
        antenna.setContinousLoad(enabled);
    }

    @LuaFunction
    public final boolean sableIsContinousLoading() {
        return antenna.continousLoad();
    }

    @LuaFunction
    public final Map<String, Double> sableShipCenterPos() {
        var pos = antenna.shipCenterPos();
        return Map.of("x", pos.x, "y", pos.y, "z", pos.z);
    }

    @LuaFunction
    public final Map<String, Double> sableShipRotation() {
        var rotation = antenna.shipRotation();
        return Map.of("x", rotation.x(), "y", rotation.y(), "z", rotation.z(), "w", rotation.w());
    }

    @LuaFunction
    public final Map<String, Double> sableShipQuaternion() {
        return sableShipRotation();
    }

    private static void requireFrequency(String frequency) throws LuaException {
        if (frequency == null || frequency.isBlank()) {
            throw new LuaException("frequency cannot be blank");
        }
    }
}
