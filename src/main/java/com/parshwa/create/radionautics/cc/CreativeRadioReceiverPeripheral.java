package com.parshwa.create.radionautics.cc;

import com.parshwa.create.radionautics.radio.RadioPacket;
import com.parshwa.create.radionautics.radio.RadioPacketEndpoint;
import com.parshwa.create.radionautics.radio.RadioTrafficDecoder;
import java.util.Map;

public class CreativeRadioReceiverPeripheral extends RadioPeripheral {
    public CreativeRadioReceiverPeripheral(RadioPacketEndpoint antenna) {
        super(antenna);
    }

    @Override
    public String getType() {
        return "radionautics_creative_receiver";
    }

    @Override
    public void onPacket(RadioPacket packet) {
        if (packet.mediaType() != com.parshwa.create.radionautics.radio.RadioMediaType.MESSAGE) {
            String event = switch (packet.mediaType()) {
                case AUDIO -> "radio_audio_audit";
                case IMAGE -> "radio_image_audit";
                case VIDEO -> "radio_video_audit";
                default -> "radio_audit";
            };
            Object data = packet.mediaType() == com.parshwa.create.radionautics.radio.RadioMediaType.AUDIO
                    ? packet.mediaId().toString()
                    : java.nio.ByteBuffer.wrap(packet.payload()).asReadOnlyBuffer();
            for (var computer : computers) {
                computer.queueEvent(event, packet.frequency(), packet.senderId().toString(), data,
                        packet.format(), packet.width(), packet.height(), packet.durationSeconds(), packet.distanceTo(antenna));
            }
            return;
        }
        var decoded = RadioTrafficDecoder.decode(packet.payload());
        double distance = packet.distanceTo(antenna);
        Map<String, Double> senderPosition = Map.of(
                "x", packet.senderPosition().x,
                "y", packet.senderPosition().y,
                "z", packet.senderPosition().z);
        for (var computer : computers) {
            computer.queueEvent("radio_audit", packet.frequency(), packet.senderId().toString(),
                    decoded.text(), distance, decoded.method(), decoded.raw(), senderPosition);
        }
    }
}
