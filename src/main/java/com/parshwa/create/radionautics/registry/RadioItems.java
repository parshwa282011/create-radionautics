package com.parshwa.create.radionautics.registry;

import com.parshwa.create.radionautics.CreateRadionautics;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RadioItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateRadionautics.MOD_ID);

    public static final DeferredItem<BlockItem> ANDESITE_RADIO_ANTENNA = ITEMS.registerSimpleBlockItem("andesite_radio_antenna", RadioBlocks.ANDESITE_RADIO_ANTENNA);
    public static final DeferredItem<BlockItem> COPPER_RADIO_ANTENNA = ITEMS.registerSimpleBlockItem("copper_radio_antenna", RadioBlocks.COPPER_RADIO_ANTENNA);
    public static final DeferredItem<BlockItem> BRASS_RADIO_ANTENNA = ITEMS.registerSimpleBlockItem("brass_radio_antenna", RadioBlocks.BRASS_RADIO_ANTENNA);
    public static final DeferredItem<BlockItem> ASTRONAUTICAL_RADIO_LINK = ITEMS.registerSimpleBlockItem("astronautical_radio_link", RadioBlocks.ASTRONAUTICAL_RADIO_LINK);
    public static final DeferredItem<BlockItem> BRASS_RADIO_LINK = ITEMS.registerSimpleBlockItem("brass_radio_link", RadioBlocks.BRASS_RADIO_LINK);
    public static final DeferredItem<BlockItem> GROUND_BASE = ITEMS.registerSimpleBlockItem("ground_base", RadioBlocks.GROUND_BASE);
    public static final DeferredItem<BlockItem> GROUND_BASE_FRAME = ITEMS.registerSimpleBlockItem("ground_base_frame", RadioBlocks.GROUND_BASE_FRAME);
    public static final DeferredItem<BlockItem> GROUND_BASE_MAST = ITEMS.registerSimpleBlockItem("ground_base_mast", RadioBlocks.GROUND_BASE_MAST);
    public static final DeferredItem<BlockItem> GROUND_BASE_RECEIVER = ITEMS.registerSimpleBlockItem("ground_base_receiver", RadioBlocks.GROUND_BASE_RECEIVER);
    public static final DeferredItem<BlockItem> GROUND_BASE_CAP = ITEMS.registerSimpleBlockItem("ground_base_cap", RadioBlocks.GROUND_BASE_CAP);
    public static final DeferredItem<BlockItem> COPPER_SCRAMBLER = ITEMS.registerSimpleBlockItem("copper_scrambler", RadioBlocks.COPPER_SCRAMBLER);
    public static final DeferredItem<BlockItem> BRASS_SCRAMBLER = ITEMS.registerSimpleBlockItem("brass_scrambler", RadioBlocks.BRASS_SCRAMBLER);

    private RadioItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
