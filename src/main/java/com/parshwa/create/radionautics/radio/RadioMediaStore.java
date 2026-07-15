package com.parshwa.create.radionautics.radio;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

/** A deduplicated, server-runtime media cache. Audio expires ten minutes after transmission. */
public final class RadioMediaStore {
    public static final long RETENTION_MILLIS = Duration.ofMinutes(10).toMillis();
    private static final int MAX_READ_BYTES = 128 * 1024;
    private static final int MAX_AUDIO_BYTES = 32 * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 256L * 1024L * 1024L;
    private static final Map<UUID, StoredAudio> AUDIO = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_TICK_CLEANUP = new AtomicLong();

    private RadioMediaStore() {
    }

    public static UUID storeAudio(RadioPacketEndpoint sender, String frequency, byte[] data,
                                  String format, double durationSeconds) {
        cleanup();
        if (data.length > MAX_AUDIO_BYTES) {
            throw new IllegalArgumentException("audio exceeds the 32 MiB server cache limit");
        }
        evictFor(data.length);
        if (!(sender.level() instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("audio can only be cached on a server level");
        }
        try {
            UUID id = UUID.randomUUID();
            long now = System.currentTimeMillis();
            Path directory = serverLevel.getServer().getWorldPath(LevelResource.ROOT)
                    .resolve("media").resolve("create_radio");
            Files.createDirectories(directory);
            Path path = directory.resolve(id + ".pcm");
            Files.write(path, data, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            StoredAudio stored = new StoredAudio(id, sender.radioId(), frequency, path, data.length, format,
                    durationSeconds, now, now + RETENTION_MILLIS, ConcurrentHashMap.newKeySet());
            stored.allowedRadioIds().add(sender.radioId());
            AUDIO.put(id, stored);
            return id;
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("failed to write server audio cache", exception);
        }
    }

    public static void allow(UUID mediaId, UUID radioId) {
        StoredAudio stored = AUDIO.get(mediaId);
        if (stored != null) stored.allowedRadioIds().add(radioId);
    }

    public static StoredAudio get(UUID mediaId, UUID radioId, boolean auditAccess) {
        cleanup();
        StoredAudio stored = AUDIO.get(mediaId);
        if (stored == null || (!auditAccess && !stored.allowedRadioIds().contains(radioId))) return null;
        return stored;
    }

    public static List<StoredAudio> list(UUID radioId, boolean auditAccess) {
        cleanup();
        return AUDIO.values().stream()
                .filter(item -> auditAccess || item.allowedRadioIds().contains(radioId))
                .sorted(Comparator.comparingLong(StoredAudio::createdUtcMillis).reversed())
                .toList();
    }

    public static ByteBuffer read(UUID mediaId, UUID radioId, boolean auditAccess, int offset, int length) {
        StoredAudio stored = get(mediaId, radioId, auditAccess);
        if (stored == null) return null;
        int start = Math.max(0, Math.min(offset, stored.bytes()));
        int count = Math.max(0, Math.min(Math.min(length, MAX_READ_BYTES), stored.bytes() - start));
        try (FileChannel channel = FileChannel.open(stored.path(), StandardOpenOption.READ)) {
            ByteBuffer result = ByteBuffer.allocate(count);
            channel.position(start);
            while (result.hasRemaining() && channel.read(result) >= 0) {
                // Continue until the requested chunk is full or EOF is reached.
            }
            result.flip();
            return result.asReadOnlyBuffer();
        } catch (java.io.IOException exception) {
            remove(stored);
            return null;
        }
    }

    public static void cleanup() {
        long now = System.currentTimeMillis();
        for (StoredAudio item : List.copyOf(AUDIO.values())) {
            if (item.expiresUtcMillis() <= now || !Files.isRegularFile(item.path())) remove(item);
        }
    }

    public static void tickCleanup() {
        long now = System.currentTimeMillis();
        long previous = LAST_TICK_CLEANUP.get();
        if (now - previous >= 1_000L && LAST_TICK_CLEANUP.compareAndSet(previous, now)) cleanup();
    }

    private static void evictFor(int incomingBytes) {
        while (totalBytes() + incomingBytes > MAX_TOTAL_BYTES && !AUDIO.isEmpty()) {
            AUDIO.values().stream().min(Comparator.comparingLong(StoredAudio::createdUtcMillis))
                    .ifPresent(RadioMediaStore::remove);
        }
    }

    private static long totalBytes() {
        return AUDIO.values().stream().mapToLong(StoredAudio::bytes).sum();
    }

    public static void clear() {
        for (StoredAudio item : List.copyOf(AUDIO.values())) remove(item);
        LAST_TICK_CLEANUP.set(0L);
    }

    public static int clearAll(MinecraftServer server) {
        Path directory = server.getWorldPath(LevelResource.ROOT).resolve("media").resolve("create_radio");
        AUDIO.clear();
        LAST_TICK_CLEANUP.set(0L);
        if (!Files.exists(directory)) return 0;
        int deleted = 0;
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                if (path.equals(directory)) continue;
                boolean audioFile = Files.isRegularFile(path) && path.getFileName().toString().endsWith(".pcm");
                if (Files.deleteIfExists(path) && audioFile) deleted++;
            }
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("failed to clear server radio media cache", exception);
        }
        return deleted;
    }

    private static void remove(StoredAudio item) {
        AUDIO.remove(item.id(), item);
        try {
            Files.deleteIfExists(item.path());
        } catch (java.io.IOException ignored) {
        }
    }

    public record StoredAudio(UUID id, UUID senderRadioId, String frequency, Path path, int bytes, String format,
                              double durationSeconds, long createdUtcMillis, long expiresUtcMillis,
                              Set<UUID> allowedRadioIds) {
    }
}
