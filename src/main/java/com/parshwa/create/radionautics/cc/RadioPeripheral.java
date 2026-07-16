package com.parshwa.create.radionautics.cc;

import com.parshwa.create.radionautics.radio.RadioCrypto;
import com.parshwa.create.radionautics.radio.RadioNetwork;
import com.parshwa.create.radionautics.radio.RadioPacket;
import com.parshwa.create.radionautics.radio.RadioPacketEndpoint;
import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.Nullable;
import net.neoforged.fml.ModList;
import java.util.UUID;

public class RadioPeripheral implements IPeripheral, RadioPacketEndpoint.PacketListener {
    protected final RadioPacketEndpoint antenna;
    protected final List<IComputerAccess> computers = new CopyOnWriteArrayList<>();

    public RadioPeripheral(RadioPacketEndpoint antenna) {
        this.antenna = antenna;
    }

    @Override
    public String getType() {
        return "radionautics_radio";
    }

    @Override
    public void attach(IComputerAccess computer) {
        computers.add(computer);
        RadioNetwork.registerAntenna(antenna);
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
        if (packet.mediaType() != com.parshwa.create.radionautics.radio.RadioMediaType.MESSAGE) {
            queueMediaEvent(packet, false);
            return;
        }
        String payload = new String(packet.payload(), StandardCharsets.UTF_8);
        for (IComputerAccess computer : computers) {
            computer.queueEvent("radio_message", packet.frequency(), packet.senderId().toString(), payload);
        }
    }

    protected void queueMediaEvent(RadioPacket packet, boolean includeDistance) {
        String event = switch (packet.mediaType()) {
            case AUDIO -> "radio_audio";
            case IMAGE -> "radio_image";
            case VIDEO -> "radio_video";
            default -> "radio_message";
        };
        Object data = packet.mediaType() == com.parshwa.create.radionautics.radio.RadioMediaType.AUDIO
                ? packet.mediaId().toString()
                : ByteBuffer.wrap(packet.payload()).asReadOnlyBuffer();
        for (IComputerAccess computer : computers) {
            if (includeDistance) {
                computer.queueEvent(event, packet.frequency(), packet.senderId().toString(), data,
                        packet.format(), packet.width(), packet.height(), packet.durationSeconds(), packet.distanceTo(antenna));
            } else {
                computer.queueEvent(event, packet.frequency(), packet.senderId().toString(), data,
                        packet.format(), packet.width(), packet.height(), packet.durationSeconds());
            }
        }
    }

    @LuaFunction
    public final List<Map<String, Object>> mediaList() {
        boolean audit = antenna instanceof com.parshwa.create.radionautics.radio.CreativeRadioMonitor;
        List<Map<String, Object>> result = new ArrayList<>();
        for (var item : com.parshwa.create.radionautics.radio.RadioMediaStore.list(antenna.radioId(), audit)) {
            result.add(mediaDetails(item));
        }
        return result;
    }

    @LuaFunction
    public final Map<String, Object> mediaInfo(String mediaId) throws LuaException {
        UUID id = requireMediaId(mediaId);
        boolean audit = antenna instanceof com.parshwa.create.radionautics.radio.CreativeRadioMonitor;
        var item = com.parshwa.create.radionautics.radio.RadioMediaStore.get(id, antenna.radioId(), audit);
        if (item == null) throw new LuaException("media not found, expired, or access denied");
        return mediaDetails(item);
    }

    @LuaFunction
    public final ByteBuffer mediaRead(String mediaId, int offset, int length) throws LuaException {
        UUID id = requireMediaId(mediaId);
        boolean audit = antenna instanceof com.parshwa.create.radionautics.radio.CreativeRadioMonitor;
        ByteBuffer data = com.parshwa.create.radionautics.radio.RadioMediaStore.read(
                id, antenna.radioId(), audit, offset, length);
        if (data == null) throw new LuaException("media not found, expired, or access denied");
        return data;
    }

    private static UUID requireMediaId(String value) throws LuaException {
        try {
            return UUID.fromString(value);
        } catch (RuntimeException exception) {
            throw new LuaException("invalid media ID");
        }
    }

    private static Map<String, Object> mediaDetails(com.parshwa.create.radionautics.radio.RadioMediaStore.StoredAudio item) {
        return Map.of(
                "id", item.id().toString(),
                "senderId", item.senderRadioId().toString(),
                "frequency", item.frequency(),
                "format", item.format(),
                "bytes", item.bytes(),
                "duration", item.durationSeconds(),
                "createdUtc", item.createdUtcMillis(),
                "expiresUtc", item.expiresUtcMillis());
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
    public final int sendAudio(String frequency, ByteBuffer data, String format, double durationSeconds) throws LuaException {
        return sendMedia(frequency, data, com.parshwa.create.radionautics.radio.RadioMediaType.AUDIO,
                format == null || format.isBlank() ? "pcm_s16le_48000_mono" : format, 0, 0, durationSeconds);
    }

    @LuaFunction
    public final int sendImage(String frequency, ByteBuffer data, String palette, int width, int height) throws LuaException {
        if (width <= 0 || height <= 0) throw new LuaException("image dimensions must be positive");
        return sendMedia(frequency, data, com.parshwa.create.radionautics.radio.RadioMediaType.IMAGE,
                palette == null ? "" : palette, width, height, 0.0D);
    }

    @LuaFunction
    public final List<String> prepareImage(ByteBuffer pixels, String palette, int width, int height,
                                           int targetWidth, int targetHeight, boolean dither) throws LuaException {
        requireExposure();
        try {
            return com.parshwa.create.radionautics.compat.exposure.ExposureImageProcessor.prepareMonitor(
                    antenna.level(), pixels, palette, width, height, targetWidth, targetHeight, dither);
        } catch (RuntimeException exception) {
            throw new LuaException(exception.getMessage());
        }
    }

    @LuaFunction
    public final List<String> preparePrintedImage(ByteBuffer pixels, String palette, int width, int height,
                                                  int targetWidth, int targetHeight) throws LuaException {
        requireExposure();
        try {
            return com.parshwa.create.radionautics.compat.exposure.ExposureImageProcessor.preparePrint(
                    antenna.level(), pixels, palette, width, height, targetWidth, targetHeight);
        } catch (RuntimeException exception) {
            throw new LuaException(exception.getMessage());
        }
    }

    private static void requireExposure() throws LuaException {
        if (!ModList.get().isLoaded("exposure")) throw new LuaException("Exposure is not installed");
    }

    @LuaFunction
    public final int sendVideo(String frequency, ByteBuffer data, String format, int width, int height, double durationSeconds) throws LuaException {
        if (width <= 0 || height <= 0) throw new LuaException("video dimensions must be positive");
        return sendMedia(frequency, data, com.parshwa.create.radionautics.radio.RadioMediaType.VIDEO,
                format == null || format.isBlank() ? "rvid" : format, width, height, durationSeconds);
    }

    @LuaFunction
    public final boolean startVoiceRecording(String frequency, String playerUuid) throws LuaException {
        requireFrequency(frequency);
        if (!ModList.get().isLoaded("voicechat_api") && !ModList.get().isLoaded("voicechat")) {
            throw new LuaException("Simple Voice Chat is not installed");
        }
        try {
            return com.parshwa.create.radionautics.compat.voicechat.RadionauticsVoicechatPlugin.start(
                    antenna, UUID.fromString(playerUuid), RadioAntennaBlockEntity.normalizeFrequency(frequency));
        } catch (IllegalArgumentException exception) {
            throw new LuaException("playerUuid must be a valid UUID");
        }
    }

    @LuaFunction
    public final int stopVoiceRecording() throws LuaException {
        if (!ModList.get().isLoaded("voicechat_api") && !ModList.get().isLoaded("voicechat")) {
            throw new LuaException("Simple Voice Chat is not installed");
        }
        int result = com.parshwa.create.radionautics.compat.voicechat.RadionauticsVoicechatPlugin.stopAndTransmit(antenna);
        if (result < 0) throw new LuaException("this radio is not recording");
        return result;
    }

    @LuaFunction
    public final boolean cancelVoiceRecording() throws LuaException {
        if (!ModList.get().isLoaded("voicechat_api") && !ModList.get().isLoaded("voicechat")) {
            throw new LuaException("Simple Voice Chat is not installed");
        }
        return com.parshwa.create.radionautics.compat.voicechat.RadionauticsVoicechatPlugin.cancel(antenna);
    }

    private int sendMedia(String frequency, ByteBuffer data, com.parshwa.create.radionautics.radio.RadioMediaType type,
                          String format, int width, int height, double durationSeconds) throws LuaException {
        requireFrequency(frequency);
        if (data == null) throw new LuaException("media data cannot be nil");
        ByteBuffer copy = data.slice();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return RadioNetwork.broadcastMedia(antenna, RadioAntennaBlockEntity.normalizeFrequency(frequency), bytes,
                type, format, width, height, Math.max(0.0D, durationSeconds));
    }

    @LuaFunction
    public Object[] receive() {
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
            String encoded = Base64.getEncoder().encodeToString(encrypted);
            com.parshwa.create.radionautics.radio.RadioTrafficDecoder.remember(encoded, data);
            return encoded;
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
