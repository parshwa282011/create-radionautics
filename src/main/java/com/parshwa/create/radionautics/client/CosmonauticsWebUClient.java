package com.parshwa.create.radionautics.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.parshwa.create.radionautics.compat.cosmonautics.CosmonauticsWebUCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

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
        Object screen = CosmonauticsWebUCompat.createLuaEditorScreen();
        if (screen instanceof Screen minecraftScreen) {
            Minecraft.getInstance().setScreen(minecraftScreen);
        }
    }
}
