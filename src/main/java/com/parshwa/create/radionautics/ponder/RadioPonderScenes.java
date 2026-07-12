package com.parshwa.create.radionautics.ponder;

import com.parshwa.create.radionautics.registry.RadioBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;

public final class RadioPonderScenes {
    private RadioPonderScenes() {
    }

    public static void radioAntennas(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("radio_antennas", "Radio Antennas");
        scene.configureBasePlate(0, 0, 6);
        scene.showBasePlate();
        scene.scaleSceneView(0.82f);
        scene.idle(5);

        BlockPos computerA = util.grid().at(1, 1, 2);
        BlockPos antennaA = util.grid().at(2, 1, 2);
        BlockPos antennaB = util.grid().at(4, 1, 2);
        BlockPos computerB = util.grid().at(5, 1, 2);

        scene.world().setBlock(computerA, Blocks.IRON_BLOCK.defaultBlockState(), false);
        scene.world().setBlock(antennaA, RadioBlocks.COPPER_RADIO_ANTENNA.get().defaultBlockState(), false);
        scene.world().setBlock(antennaB, RadioBlocks.BRASS_RADIO_ANTENNA.get().defaultBlockState(), false);
        scene.world().setBlock(computerB, Blocks.IRON_BLOCK.defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 5, 1, 2), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(70)
                .text("Radio antennas let computers exchange packets on named frequencies.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(antennaA))
                .placeNearTarget();
        scene.idle(75);

        scene.overlay().showLine(PonderPalette.BLUE, util.vector().centerOf(antennaA), util.vector().centerOf(antennaB), 55);
        scene.overlay().showText(60)
                .colored(PonderPalette.BLUE)
                .text("Andesite reaches 2000 blocks. Copper reaches 5000 blocks.")
                .pointAt(util.vector().centerOf(antennaA))
                .placeNearTarget();
        scene.idle(65);

        scene.overlay().showText(55)
                .colored(PonderPalette.BLUE)
                .text("Brass antennas have infinite same-dimension range.")
                .pointAt(util.vector().centerOf(antennaB))
                .placeNearTarget();
        scene.idle(60);

        if (ModList.get().isLoaded("computercraft")) {
            showCcExample(scene, util, antennaA, "base", "hello");
        }

        scene.markAsFinished();
    }

    public static void brassRadioLink(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("brass_radio_link", "Brass Radio Links");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.scaleSceneView(0.82f);
        scene.idle(5);

        BlockPos sender = util.grid().at(1, 1, 2);
        BlockPos receiver = util.grid().at(3, 1, 2);
        BlockPos lamp = util.grid().at(4, 1, 2);

        scene.world().setBlock(sender, RadioBlocks.BRASS_RADIO_LINK.get().defaultBlockState(), false);
        scene.world().setBlock(receiver, RadioBlocks.BRASS_RADIO_LINK.get().defaultBlockState(), false);
        scene.world().setBlock(lamp, Blocks.REDSTONE_LAMP.defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 4, 1, 2), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(70)
                .text("Brass Radio Links are long-range redstone links.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(sender))
                .placeNearTarget();
        scene.idle(75);

        scene.overlay().showLine(PonderPalette.RED, util.vector().centerOf(sender), util.vector().centerOf(receiver), 50);
        scene.overlay().showText(75)
                .colored(PonderPalette.RED)
                .text("Set one link to Sender and the other to Receiver, then give both the same frequency.")
                .pointAt(util.vector().centerOf(receiver))
                .placeNearTarget();
        scene.idle(80);

        scene.world().toggleRedstonePower(util.select().position(lamp));
        scene.overlay().showText(60)
                .text("The receiver outputs the latest redstone strength sent on that frequency.")
                .pointAt(util.vector().centerOf(lamp))
                .placeNearTarget();
        scene.idle(65);
        scene.markAsFinished();
    }

    public static void astronauticalRadioLink(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("astronautical_radio_link", "Astronautical Radio Links");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.scaleSceneView(0.82f);
        scene.idle(5);

        BlockPos link = util.grid().at(2, 1, 2);
        scene.world().setBlock(link, RadioBlocks.ASTRONAUTICAL_RADIO_LINK.get().defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(2, 1, 2, 2, 3, 2), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(70)
                .text("Astronautical Radio Links are the highest radio tier.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(link))
                .placeNearTarget();
        scene.idle(75);

        scene.overlay().showText(75)
                .colored(PonderPalette.BLUE)
                .text("They can bridge dimensions and provide the strongest radio peripheral behavior.")
                .pointAt(util.vector().topOf(link))
                .placeNearTarget();
        scene.idle(80);

        if (ModList.get().isLoaded("computercraft")) {
            showCcExample(scene, util, link, "orbit", "status");
        }

        scene.markAsFinished();
    }

    private static void showCcExample(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos target, String frequency, String message) {
        scene.overlay().showText(70)
                .colored(PonderPalette.GREEN)
                .text("CC:Tweaked example: find the radio peripheral.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(target))
                .placeNearTarget();
        scene.idle(75);

        scene.overlay().showText(70)
                .colored(PonderPalette.GREEN)
                .text("local radio = peripheral.find(\"radionautics_radio\")")
                .pointAt(util.vector().centerOf(target))
                .placeNearTarget();
        scene.idle(75);

        scene.overlay().showText(70)
                .colored(PonderPalette.GREEN)
                .text("radio.udpBind(\"" + frequency + "\")")
                .pointAt(util.vector().centerOf(target))
                .placeNearTarget();
        scene.idle(75);

        scene.overlay().showText(70)
                .colored(PonderPalette.GREEN)
                .text("radio.send(\"" + frequency + "\", \"" + message + "\")")
                .pointAt(util.vector().centerOf(target))
                .placeNearTarget();
        scene.idle(75);
    }
}
