package com.parshwa.create.radionautics.cc;

import com.parshwa.create.radionautics.radio.RadioPacketEndpoint;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import com.parshwa.create.radionautics.radio.RadioPacket;

public class MegaRadioPeripheral extends RadioPeripheral {
    private final RadioPacketEndpoint antenna;

    public MegaRadioPeripheral(RadioPacketEndpoint antenna) {
        super(antenna);
        this.antenna = antenna;
    }

    @Override
    public String getType() {
        return "radionautics_mega_radio";
    }

    @Override
    public void onPacket(RadioPacket packet) {
        if (packet.mediaType() != com.parshwa.create.radionautics.radio.RadioMediaType.MESSAGE) {
            queueMediaEvent(packet, true);
            return;
        }
        String payload = new String(packet.payload(), StandardCharsets.UTF_8);
        double distance = packet.distanceTo(antenna);
        for (var computer : computers) {
            computer.queueEvent("radio_message", packet.frequency(), packet.senderId().toString(), payload, distance);
        }
    }

    @Override
    @LuaFunction
    public Object[] receive() {
        RadioPacket packet = antenna.pollPacket();
        if (packet == null) {
            return null;
        }
        return new Object[] {
                packet.frequency(),
                packet.senderId().toString(),
                new String(packet.payload(), StandardCharsets.UTF_8),
                packet.distanceTo(antenna)
        };
    }

    @LuaFunction
    public final int sendMany(Map<?, ?> frequencies, String payload) throws LuaException {
        if (frequencies == null || frequencies.isEmpty()) {
            throw new LuaException("frequencies cannot be empty");
        }
        if (payload == null) {
            throw new LuaException("payload cannot be nil");
        }
        int delivered = 0;
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        for (Object value : frequencies.values()) {
            if (value instanceof String frequency && !frequency.isBlank()) {
                delivered += antenna.send(frequency, bytes);
            }
        }
        return delivered;
    }
}
