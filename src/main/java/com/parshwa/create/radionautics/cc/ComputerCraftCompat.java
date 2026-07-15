package com.parshwa.create.radionautics.cc;

import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
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
        event.registerBlockEntity(
                PeripheralCapability.get(),
                RadioBlockEntities.ASTRONAUTICAL_RADIO_LINK.get(),
                (antenna, direction) -> new RadioPeripheral(antenna));
        event.registerBlockEntity(
                PeripheralCapability.get(),
                RadioBlockEntities.GROUND_BASE.get(),
                (antenna, direction) -> new MegaRadioPeripheral(antenna));
        event.registerBlockEntity(
                PeripheralCapability.get(),
                RadioBlockEntities.CREATIVE_RADIO_RECEIVER.get(),
                (antenna, direction) -> new CreativeRadioReceiverPeripheral(antenna));
        registerSputnikPeripheral(event);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerSputnikPeripheral(RegisterCapabilitiesEvent event) {
        if (!ModList.get().isLoaded("rocketnautics")) {
            return;
        }

        ResourceLocation sputnikId = ResourceLocation.fromNamespaceAndPath("rocketnautics", "sputnik");
        BlockEntityType<?> sputnikType = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(sputnikId);
        if (sputnikType == null || !sputnikId.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(sputnikType))) {
            return;
        }

        event.registerBlockEntity(
                PeripheralCapability.get(),
                (BlockEntityType) sputnikType,
                (blockEntity, direction) -> new RadioPeripheral(SputnikRadioEndpoint.getOrCreate((BlockEntity) blockEntity)));
    }
}
