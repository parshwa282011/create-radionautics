package com.parshwa.create.radionautics.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.parshwa.create.radionautics.compat.cosmonautics.CosmonauticsWebUCompat;
import dev.devce.websnodelib.api.NodeRegistry;
import dev.devce.websnodelib.api.WGraph;
import dev.devce.websnodelib.api.WNode;
import dev.devce.websnodelib.client.ui.WNodeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class CosmonauticsWebUClient {
    private CosmonauticsWebUClient() {
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("webu")
                .then(Commands.literal("lua_editor")
                        .executes(context -> {
                            Minecraft.getInstance().tell(CosmonauticsWebUClient::openLuaEditor);
                            return Command.SINGLE_SUCCESS;
                        })));
    }

    private static void openLuaEditor() {
        CosmonauticsWebUCompat.register();

        WGraph graph = new WGraph();
        WNode receiver = NodeRegistry.createNode(CosmonauticsWebUCompat.RADIO_RECEIVER, 80, 80);
        WNode viewer = NodeRegistry.createNode(CosmonauticsWebUCompat.RADIO_TEXT_VIEWER, 360, 80);
        WNode charParser = NodeRegistry.createNode(CosmonauticsWebUCompat.RADIO_TEXT_CHAR, 360, 230);

        if (receiver != null) {
            graph.addNode(receiver);
        }
        if (viewer != null) {
            graph.addNode(viewer);
        }
        if (charParser != null) {
            graph.addNode(charParser);
        }

        Minecraft.getInstance().setScreen(new WNodeScreen(Component.literal("Radionautics Lua Editor"), graph, tag -> {}, null));
    }
}
