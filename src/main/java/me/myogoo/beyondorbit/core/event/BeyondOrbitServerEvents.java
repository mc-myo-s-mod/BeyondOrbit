package me.myogoo.beyondorbit.core.event;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyReloadListener;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = BeyondOrbitCore.MODID)
public final class BeyondOrbitServerEvents {
    private static final RandomSource SATELLITE_RANDOM = RandomSource.create();

    private BeyondOrbitServerEvents() {
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CelestialBodyReloadListener());
    }

    @SubscribeEvent
    public static void tickSatellites(ServerTickEvent.Post event) {
        BeyondOrbitSavedData.get(event.getServer()).tickSatellites(SATELLITE_RANDOM);
    }
}
