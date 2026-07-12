package com.parshwa.create.radionautics.client;

import com.parshwa.create.radionautics.registry.RadioMenus;
import com.parshwa.create.radionautics.ponder.RadioPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class RadioClient {
    private static boolean ponderRegistered;

    private RadioClient() {
    }

    public static void register(IEventBus modBus) {
        registerPonderPlugin();
        modBus.addListener(RadioClient::registerScreens);
        NeoForge.EVENT_BUS.addListener(RadioClient::registerClientCommands);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RadioMenus.BRASS_RADIO_LINK.get(), BrassRadioLinkScreen::new);
    }

    private static void registerClientCommands(RegisterClientCommandsEvent event) {
        if (ModList.get().isLoaded("rocketnautics")) {
            CosmonauticsWebUClient.registerCommands(event.getDispatcher());
        }
    }

    private static void registerPonderPlugin() {
        if (!ponderRegistered) {
            PonderIndex.addPlugin(new RadioPonderPlugin());
            ponderRegistered = true;
        }
    }
}
