package com.parshwa.create.radionautics.registry;

import com.parshwa.create.radionautics.CreateRadionautics;
import com.parshwa.create.radionautics.block.AstronauticalRadioLinkBlock;
import com.parshwa.create.radionautics.block.BrassRadioLinkBlock;
import com.parshwa.create.radionautics.block.CreativeRadioReceiverBlock;
import com.parshwa.create.radionautics.block.GroundBaseBlock;
import com.parshwa.create.radionautics.block.RadioAntennaBlock;
import com.parshwa.create.radionautics.block.ScramblerBlock;
import com.parshwa.create.radionautics.block.ScramblerTier;
import com.parshwa.create.radionautics.radio.AntennaTier;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
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

    public static final Supplier<Block> CREATIVE_RADIO_RECEIVER = BLOCKS.registerBlock(
            "creative_radio_receiver",
            CreativeRadioReceiverBlock::new,
            BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).noLootTable().noOcclusion());

    public static final Supplier<Block> ASTRONAUTICAL_RADIO_LINK = BLOCKS.registerBlock(
            "astronautical_radio_link",
            AstronauticalRadioLinkBlock::new,
            BlockBehaviour.Properties.of().strength(4.0F, 9.0F).noOcclusion());

    public static final Supplier<Block> BRASS_RADIO_LINK = BLOCKS.registerBlock(
            "brass_radio_link",
            BrassRadioLinkBlock::new,
            BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion());

    public static final Supplier<Block> GROUND_BASE = BLOCKS.registerBlock(
            "ground_base",
            GroundBaseBlock::new,
            BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion());

    public static final Supplier<Block> GROUND_BASE_FRAME = BLOCKS.registerSimpleBlock(
            "ground_base_frame",
            BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion());

    public static final Supplier<Block> GROUND_BASE_MAST = BLOCKS.registerBlock(
            "ground_base_mast",
            RotatedPillarBlock::new,
            BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion());

    public static final Supplier<Block> GROUND_BASE_RECEIVER = BLOCKS.registerSimpleBlock(
            "ground_base_receiver",
            BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion());

    public static final Supplier<Block> GROUND_BASE_CAP = BLOCKS.registerSimpleBlock(
            "ground_base_cap",
            BlockBehaviour.Properties.of().strength(5.0F, 12.0F).noOcclusion());

    public static final Supplier<Block> COPPER_SCRAMBLER = BLOCKS.registerBlock(
            "copper_scrambler",
            properties -> new ScramblerBlock(ScramblerTier.COPPER, properties),
            BlockBehaviour.Properties.of().strength(3.0F, 6.0F).noOcclusion());

    public static final Supplier<Block> BRASS_SCRAMBLER = BLOCKS.registerBlock(
            "brass_scrambler",
            properties -> new ScramblerBlock(ScramblerTier.BRASS, properties),
            BlockBehaviour.Properties.of().strength(4.0F, 8.0F).noOcclusion());

    private RadioBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
