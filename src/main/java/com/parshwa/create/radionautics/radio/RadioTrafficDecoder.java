package com.parshwa.create.radionautics.radio;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** Server-side audit cache used only by the creative traffic receiver. */
public final class RadioTrafficDecoder {
    private static final int MAX_REMEMBERED = 4096;
    private static final Map<String, String> KNOWN_PLAINTEXT = new LinkedHashMap<>(128, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > MAX_REMEMBERED;
        }
    };

    private RadioTrafficDecoder() {
    }

    public static synchronized void remember(String transmittedText, String plaintext) {
        KNOWN_PLAINTEXT.put(transmittedText, plaintext);
    }

    public static synchronized Decoded decode(byte[] payload) {
        String raw = new String(payload, StandardCharsets.UTF_8);
        String remembered = KNOWN_PLAINTEXT.get(raw);
        if (remembered != null) {
            return new Decoded(raw, remembered, "radionautics_crypto");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(raw);
            String text = new String(decoded, StandardCharsets.UTF_8);
            if (isReadable(text)) {
                return new Decoded(raw, text, "base64");
            }
        } catch (IllegalArgumentException ignored) {
        }
        return new Decoded(raw, raw, "plain");
    }

    private static boolean isReadable(String value) {
        if (value.isEmpty()) return true;
        long readable = value.chars().filter(c -> !Character.isISOControl(c) || Character.isWhitespace(c)).count();
        return readable * 10 >= value.length() * 9L;
    }

    public record Decoded(String raw, String text, String method) {
    }
}
