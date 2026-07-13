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
        scene.configureBasePlate(0, 0, 6);
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

    public static void groundBase(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("ground_base", "Ground Base");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.scaleSceneView(0.78f);
        scene.idle(5);

        BlockPos base = util.grid().at(2, 1, 2);
        scene.world().setBlock(base, RadioBlocks.GROUND_BASE.get().defaultBlockState(), false);
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                if (x != 2 || z != 2) {
                    scene.world().setBlock(util.grid().at(x, 1, z), RadioBlocks.GROUND_BASE_FRAME.get().defaultBlockState(), false);
                }
            }
        }
        for (int y = 2; y <= 4; y++) {
            scene.world().setBlock(util.grid().at(2, y, 2), RadioBlocks.GROUND_BASE_MAST.get().defaultBlockState(), false);
        }
        scene.world().setBlock(util.grid().at(1, 4, 2), RadioBlocks.GROUND_BASE_RECEIVER.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(3, 4, 2), RadioBlocks.GROUND_BASE_RECEIVER.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(2, 4, 1), RadioBlocks.GROUND_BASE_RECEIVER.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(2, 4, 3), RadioBlocks.GROUND_BASE_RECEIVER.get().defaultBlockState(), false);
        scene.world().setBlock(util.grid().at(2, 5, 2), RadioBlocks.GROUND_BASE_CAP.get().defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(1, 1, 1, 3, 5, 3), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(70)
                .text("Build the Ground Base from separate heavy antenna parts.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(base))
                .placeNearTarget();
        scene.idle(75);

        scene.overlay().showText(75)
                .colored(PonderPalette.BLUE)
                .text("It receives every frequency at once and can transmit on any channel.")
                .pointAt(util.vector().centerOf(base))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
                .colored(PonderPalette.RED)
                .text("If any required antenna part is missing, the peripheral stops receiving events.")
                .pointAt(util.vector().centerOf(base))
                .placeNearTarget();
        scene.idle(75);

        if (ModList.get().isLoaded("computercraft")) {
            scene.overlay().showText(70)
                    .colored(PonderPalette.GREEN)
                    .text("CC:Tweaked type: radionautics_mega_radio")
                    .attachKeyFrame()
                    .pointAt(util.vector().centerOf(base))
                    .placeNearTarget();
            scene.idle(75);

            scene.overlay().showText(70)
                    .colored(PonderPalette.GREEN)
                    .text("local radio = peripheral.find(\"radionautics_mega_radio\")")
                    .pointAt(util.vector().centerOf(base))
                    .placeNearTarget();
            scene.idle(75);

            scene.overlay().showText(70)
                    .colored(PonderPalette.GREEN)
                    .text("radio.sendMany({\"base\", \"orbit\"}, \"status\")")
                    .pointAt(util.vector().centerOf(base))
                    .placeNearTarget();
            scene.idle(75);
        }

        scene.markAsFinished();
    }

    public static void scramblers(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("scramblers", "Radio Scramblers");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.scaleSceneView(0.82f);
        scene.idle(5);

        BlockPos copper = util.grid().at(1, 1, 2);
        BlockPos brass = util.grid().at(3, 1, 2);
        scene.world().setBlock(copper, RadioBlocks.COPPER_SCRAMBLER.get().defaultBlockState(), false);
        scene.world().setBlock(brass, RadioBlocks.BRASS_SCRAMBLER.get().defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 3, 1, 2), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(70)
                .text("Scramblers interfere with one configured frequency.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(copper))
                .placeNearTarget();
        scene.idle(75);

        scene.overlay().showText(75)
                .colored(PonderPalette.RED)
                .text("Copper covers 100 blocks. Brass covers 1000 blocks.")
                .pointAt(util.vector().centerOf(brass))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(80)
                .colored(PonderPalette.RED)
                .text("Computer packets are scrambled. Redstone radio links on that frequency are blocked.")
                .pointAt(util.vector().centerOf(brass))
                .placeNearTarget();
        scene.idle(85);

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
