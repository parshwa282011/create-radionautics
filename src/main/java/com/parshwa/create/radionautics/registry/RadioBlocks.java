package com.parshwa.create.radionautics.registry;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.block.BrassRadioLinkBlock;
import com.parshwa.create.radionautics.block.RadioAntennaBlock;
import com.parshwa.create.radionautics.radio.AntennaTier;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RadioBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CreateRadionautics.MOD_ID);

    public static final Supplier<Block> ANDESITE_RADIO_ANTENNA = BLOCKS.registerBlock(
            "andesite_radio_antenna",
            properties -> new RadioAntennaBlock(AntennaTier.ANDESITE, properties),
            BlockBehaviour.Properties.of().strength(2.0F, 6.0F).noOcclusion());

    public static final Supplier<Block> COPPER_RADIO_ANTENNA = BLOCKS.registerBlock(
            "copper_radio_antenna",
            properties -> new RadioAntennaBlock(AntennaTier.COPPER, properties),
            BlockBehaviour.Properties.of().strength(2.5F, 6.0F).noOcclusion());

    public static final Supplier<Block> BRASS_RADIO_ANTENNA = BLOCKS.registerBlock(
            "brass_radio_antenna",
            properties -> new RadioAntennaBlock(AntennaTier.BRASS, properties),
            BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion());

    public static final Supplier<Block> BRASS_RADIO_LINK = BLOCKS.registerBlock(
            "brass_radio_link",
            BrassRadioLinkBlock::new,
            BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion());

    private RadioBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
