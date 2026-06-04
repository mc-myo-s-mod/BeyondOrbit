package me.myogoo.beyondorbit.core;

import com.mojang.logging.LogUtils;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(BeyondOrbitCore.MODID)
public class BeyondOrbitCore {
    public static final String MODID = "beyondorbit";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BeyondOrbitCore(IEventBus modEventBus, ModContainer modContainer) {
        BeyondOrbitContent.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(BeyondOrbitContent::registerCapabilities);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("BeyondOrbit Core initialized");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("BeyondOrbit server systems are ready to initialize");
    }
}
