package com.parshwa.create.radionautics.registry;

import com.parshwa.create.radionautics.CreateRadionautics;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RadioCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateRadionautics.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.create_radio.main"))
            .icon(() -> new ItemStack(RadioItems.BRASS_RADIO_LINK.get()))
            .displayItems((parameters, output) -> {
                output.accept(RadioItems.ANDESITE_RADIO_ANTENNA.get());
                output.accept(RadioItems.COPPER_RADIO_ANTENNA.get());
                output.accept(RadioItems.BRASS_RADIO_ANTENNA.get());
                output.accept(RadioItems.BRASS_RADIO_LINK.get());
            })
            .build());

    private RadioCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
