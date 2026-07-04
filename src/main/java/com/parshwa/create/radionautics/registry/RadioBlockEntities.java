package com.parshwa.create.radionautics.registry;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.blockentity.BrassRadioLinkBlockEntity;
import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RadioBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateRadionautics.MOD_ID);

    public static final Supplier<BlockEntityType<RadioAntennaBlockEntity>> RADIO_ANTENNA = BLOCK_ENTITIES.register(
            "radio_antenna",
            () -> BlockEntityType.Builder.of(
                    RadioAntennaBlockEntity::new,
                    RadioBlocks.ANDESITE_RADIO_ANTENNA.get(),
                    RadioBlocks.COPPER_RADIO_ANTENNA.get(),
                    RadioBlocks.BRASS_RADIO_ANTENNA.get()).build(null));

    public static final Supplier<BlockEntityType<BrassRadioLinkBlockEntity>> BRASS_RADIO_LINK = BLOCK_ENTITIES.register(
            "brass_radio_link",
            () -> BlockEntityType.Builder.of(
                    BrassRadioLinkBlockEntity::new,
                    RadioBlocks.BRASS_RADIO_LINK.get()).build(null));

    private RadioBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
