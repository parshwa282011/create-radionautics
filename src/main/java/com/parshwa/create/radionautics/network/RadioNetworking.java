package com.parshwa.create.radionautics.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RadioNetworking {
    private RadioNetworking() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RadioNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                ConfigureBrassRadioLinkPayload.TYPE,
                ConfigureBrassRadioLinkPayload.STREAM_CODEC,
                ConfigureBrassRadioLinkPayload::handle);
    }
}
