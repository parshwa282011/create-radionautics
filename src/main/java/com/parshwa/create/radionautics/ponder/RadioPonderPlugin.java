package com.parshwa.create.radionautics.ponder;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.registry.RadioItems;
import java.util.List;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class RadioPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateRadionautics.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(List.of(
                        itemKey(RadioItems.ANDESITE_RADIO_ANTENNA.get()),
                        itemKey(RadioItems.COPPER_RADIO_ANTENNA.get()),
                        itemKey(RadioItems.BRASS_RADIO_ANTENNA.get())))
                .addStoryBoard("radio_antennas", RadioPonderScenes::radioAntennas);

        helper.addStoryBoard(itemKey(RadioItems.BRASS_RADIO_LINK.get()), "brass_radio_link", RadioPonderScenes::brassRadioLink);
        helper.addStoryBoard(itemKey(RadioItems.ASTRONAUTICAL_RADIO_LINK.get()), "astronautical_radio_link", RadioPonderScenes::astronauticalRadioLink);
    }

    private static ResourceLocation itemKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}
