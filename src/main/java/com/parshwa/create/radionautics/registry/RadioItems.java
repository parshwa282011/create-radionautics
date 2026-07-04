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
    public static final DeferredItem<BlockItem> BRASS_RADIO_LINK = ITEMS.registerSimpleBlockItem("brass_radio_link", RadioBlocks.BRASS_RADIO_LINK);

    private RadioItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
