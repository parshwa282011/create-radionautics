package com.parshwa.create.radionautics.registry;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.menu.BrassRadioLinkMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RadioMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CreateRadionautics.MOD_ID);

    public static final Supplier<MenuType<BrassRadioLinkMenu>> BRASS_RADIO_LINK = MENUS.register(
            "brass_radio_link",
            () -> IMenuTypeExtension.create(BrassRadioLinkMenu::new));

    private RadioMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
