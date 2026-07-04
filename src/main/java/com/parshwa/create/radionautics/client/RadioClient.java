package com.parshwa.create.radionautics.client;

import com.parshwa.create.radionautics.registry.RadioMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class RadioClient {
    private RadioClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(RadioClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RadioMenus.BRASS_RADIO_LINK.get(), BrassRadioLinkScreen::new);
    }
}
