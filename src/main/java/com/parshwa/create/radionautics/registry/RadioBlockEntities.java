package com.parshwa.create.radionautics.registry;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.blockentity.AstronauticalRadioLinkBlockEntity;
import com.parshwa.create.radionautics.blockentity.BrassRadioLinkBlockEntity;
import com.parshwa.create.radionautics.blockentity.CreativeRadioReceiverBlockEntity;
import com.parshwa.create.radionautics.blockentity.GroundBaseBlockEntity;
import com.parshwa.create.radionautics.blockentity.RadioAntennaBlockEntity;
import com.parshwa.create.radionautics.blockentity.ScramblerBlockEntity;
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

    public static final Supplier<BlockEntityType<CreativeRadioReceiverBlockEntity>> CREATIVE_RADIO_RECEIVER = BLOCK_ENTITIES.register(
            "creative_radio_receiver",
            () -> BlockEntityType.Builder.of(
                    CreativeRadioReceiverBlockEntity::new,
                    RadioBlocks.CREATIVE_RADIO_RECEIVER.get()).build(null));

    public static final Supplier<BlockEntityType<AstronauticalRadioLinkBlockEntity>> ASTRONAUTICAL_RADIO_LINK = BLOCK_ENTITIES.register(
            "astronautical_radio_link",
            () -> BlockEntityType.Builder.of(
                    AstronauticalRadioLinkBlockEntity::new,
                    RadioBlocks.ASTRONAUTICAL_RADIO_LINK.get()).build(null));

    public static final Supplier<BlockEntityType<BrassRadioLinkBlockEntity>> BRASS_RADIO_LINK = BLOCK_ENTITIES.register(
            "brass_radio_link",
            () -> BlockEntityType.Builder.of(
                    BrassRadioLinkBlockEntity::new,
                    RadioBlocks.BRASS_RADIO_LINK.get()).build(null));

    public static final Supplier<BlockEntityType<GroundBaseBlockEntity>> GROUND_BASE = BLOCK_ENTITIES.register(
            "ground_base",
            () -> BlockEntityType.Builder.of(
                    GroundBaseBlockEntity::new,
                    RadioBlocks.GROUND_BASE.get()).build(null));

    public static final Supplier<BlockEntityType<ScramblerBlockEntity>> SCRAMBLER = BLOCK_ENTITIES.register(
            "scrambler",
            () -> BlockEntityType.Builder.of(
                    ScramblerBlockEntity::new,
                    RadioBlocks.COPPER_SCRAMBLER.get(),
                    RadioBlocks.BRASS_SCRAMBLER.get()).build(null));

    private RadioBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
