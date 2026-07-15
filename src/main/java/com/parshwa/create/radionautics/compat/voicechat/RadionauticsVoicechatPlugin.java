package com.parshwa.create.radionautics.compat.voicechat;

import com.parshwa.create.radionautics.radio.RadioNetwork;
import com.parshwa.create.radionautics.radio.RadioPacketEndpoint;
import com.parshwa.create.radionautics.radio.RadioMediaType;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ForgeVoicechatPlugin
public class RadionauticsVoicechatPlugin implements VoicechatPlugin {
    private static final int SAMPLE_RATE = 48_000;
    private static final int MAX_PCM_BYTES = SAMPLE_RATE * 2 * 300; // five minutes
    private static final Map<UUID, Recording> BY_RADIO = new ConcurrentHashMap<>();
    private static volatile VoicechatApi api;

    @Override
    public String getPluginId() {
        return "create_radionautics";
    }

    @Override
    public void initialize(VoicechatApi voicechatApi) {
        api = voicechatApi;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, RadionauticsVoicechatPlugin::onMicrophonePacket);
    }

    public static boolean start(RadioPacketEndpoint endpoint, UUID playerId, String frequency) {
        VoicechatApi currentApi = api;
        if (currentApi == null || BY_RADIO.containsKey(endpoint.radioId())) return false;
        BY_RADIO.put(endpoint.radioId(), new Recording(endpoint, playerId, frequency, currentApi.createDecoder()));
        return true;
    }

    public static int stopAndTransmit(RadioPacketEndpoint endpoint) {
        Recording recording = BY_RADIO.remove(endpoint.radioId());
        if (recording == null) return -1;
        byte[] pcm;
        synchronized (recording) {
            recording.decoder.close();
            pcm = recording.pcm.toByteArray();
        }
        double duration = pcm.length / (double) (SAMPLE_RATE * 2);
        return RadioNetwork.broadcastMedia(endpoint, recording.frequency, pcm, RadioMediaType.AUDIO,
                "pcm_s16le_48000_mono", 0, 0, duration);
    }

    public static boolean cancel(RadioPacketEndpoint endpoint) {
        Recording recording = BY_RADIO.remove(endpoint.radioId());
        if (recording == null) return false;
        recording.decoder.close();
        return true;
    }

    private static void onMicrophonePacket(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) return;
        UUID playerId = event.getSenderConnection().getPlayer().getUuid();
        byte[] opus = event.getPacket().getOpusEncodedData();
        if (opus == null || opus.length == 0) return;
        for (Recording recording : BY_RADIO.values()) {
            if (!recording.playerId.equals(playerId)) continue;
            synchronized (recording) {
                if (recording.pcm.size() >= MAX_PCM_BYTES) continue;
                short[] samples = recording.decoder.decode(opus);
                int remainingSamples = Math.min(samples.length, (MAX_PCM_BYTES - recording.pcm.size()) / 2);
                for (int i = 0; i < remainingSamples; i++) {
                    short sample = samples[i];
                    recording.pcm.write(sample & 0xFF);
                    recording.pcm.write((sample >>> 8) & 0xFF);
                }
            }
        }
    }

    private static final class Recording {
        private final RadioPacketEndpoint endpoint;
        private final UUID playerId;
        private final String frequency;
        private final OpusDecoder decoder;
        private final ByteArrayOutputStream pcm = new ByteArrayOutputStream();

        private Recording(RadioPacketEndpoint endpoint, UUID playerId, String frequency, OpusDecoder decoder) {
            this.endpoint = endpoint;
            this.playerId = playerId;
            this.frequency = frequency;
            this.decoder = decoder;
        }
    }
}
