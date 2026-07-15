package com.parshwa.create.radionautics;

import com.mojang.logging.LogUtils;
import com.parshwa.create.radionautics.cc.ComputerCraftCompat;
import com.parshwa.create.radionautics.cc.SputnikRadioEndpoint;
import com.parshwa.create.radionautics.client.RadioClient;
import com.parshwa.create.radionautics.compat.cosmonautics.CosmonauticsWebUCompat;
import com.parshwa.create.radionautics.config.RadioConfig;
import com.parshwa.create.radionautics.network.RadioNetworking;
import com.parshwa.create.radionautics.registry.RadioBlockEntities;
import com.parshwa.create.radionautics.registry.RadioBlocks;
import com.parshwa.create.radionautics.registry.RadioCreativeTabs;
import com.parshwa.create.radionautics.registry.RadioItems;
import com.parshwa.create.radionautics.registry.RadioMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(CreateRadionautics.MOD_ID)
public class CreateRadionautics {
    public static final String MOD_ID = "create_radio";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CreateRadionautics(IEventBus modBus) {
        RadioBlocks.register(modBus);
        RadioItems.register(modBus);
        RadioBlockEntities.register(modBus);
        RadioMenus.register(modBus);
        RadioCreativeTabs.register(modBus);
        RadioNetworking.register(modBus);
        if (FMLEnvironment.dist.isClient()) {
            RadioClient.register(modBus);
        }

        ModLoadingContext.get().getActiveContainer().registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, RadioConfig.SERVER_SPEC);

        if (ModList.get().isLoaded("computercraft")) {
            ComputerCraftCompat.register(modBus);
        }
        if (ModList.get().isLoaded("rocketnautics")) {
            CosmonauticsWebUCompat.register();
        }

        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onServerStarted(ServerStartedEvent event) {
        int deleted = com.parshwa.create.radionautics.radio.RadioMediaStore.clearAll(event.getServer());
        if (deleted > 0) LOGGER.info("Cleared {} cached radio media file(s) at server startup", deleted);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("radionautics")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clear_media")
                        .executes(context -> {
                            int deleted = com.parshwa.create.radionautics.radio.RadioMediaStore.clearAll(
                                    context.getSource().getServer());
                            context.getSource().sendSuccess(
                                    () -> Component.literal("Cleared " + deleted + " cached radio media file(s)."), true);
                            return deleted;
                        })));
    }

    private void onServerStopping(ServerStoppingEvent event) {
        com.parshwa.create.radionautics.radio.RadioNetwork.clear();
        SputnikRadioEndpoint.clear();
    }
}
