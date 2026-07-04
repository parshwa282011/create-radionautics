package com.parshwa.create.radionautics.cc;

import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ComputerCraftCompat {
    private ComputerCraftCompat() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ComputerCraftCompat::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                PeripheralCapability.get(),
                RadioBlockEntities.RADIO_ANTENNA.get(),
                (antenna, direction) -> new RadioPeripheral(antenna));
    }
}
