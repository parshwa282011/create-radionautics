package com.parshwa.create.radionautics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RadioConfig {
    public static final ModConfigSpec SERVER_SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_RECEIVER_CHUNK_LOADING;
    public static final ModConfigSpec.BooleanValue ENABLE_SABLE_CONTINOUS_LOADING;
    public static final ModConfigSpec.IntValue CHUNK_LOAD_RADIUS;
    public static final ModConfigSpec.IntValue MAX_TRANSPORT_CHUNK_BYTES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("radio");
        ENABLE_RECEIVER_CHUNK_LOADING = builder
                .comment("Allows Brass Radio Link receivers to request a 5x5 server-side chunk ticket.")
                .define("enableReceiverChunkLoading", true);
        ENABLE_SABLE_CONTINOUS_LOADING = builder
                .comment("Allows radio peripherals and radio links to request continuous logical loading for Sable/Create Aeronautics ships.")
                .define("enableSableContinousLoading", true);
        CHUNK_LOAD_RADIUS = builder
                .comment("Chunk radius for non-ship receiver loading. 2 means a 5x5 area.")
                .defineInRange("chunkLoadRadius", 2, 0, 8);
        MAX_TRANSPORT_CHUNK_BYTES = builder
                .comment("Internal chunk size for splitting large radio payloads.")
                .defineInRange("maxTransportChunkBytes", 8192, 1024, 1048576);
        builder.pop();

        SERVER_SPEC = builder.build();
    }

    private RadioConfig() {
    }
}
