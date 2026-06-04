package me.myogoo.beyondorbit.core.celestial;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class CelestialBodyReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CelestialBodyReloadListener.class);
    private static final Gson GSON = new Gson();

    public CelestialBodyReloadListener() {
        super(GSON, "beyondorbit/celestial_bodies");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, CelestialBodyDefinition> loaded = new HashMap<>();

        resources.forEach((resourceId, json) -> CelestialBodyDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> LOGGER.error("Failed to load celestial body {}: {}", resourceId, error))
                .ifPresent(definition -> {
                    if (!definition.id().equals(resourceId)) {
                        LOGGER.warn("Celestial body file {} declares id {}; using declared id", resourceId, definition.id());
                    }
                    CelestialBodyDefinition previous = loaded.put(definition.id(), definition);
                    if (previous != null) {
                        LOGGER.warn("Duplicate celestial body id {} replaced previous definition", definition.id());
                    }
                }));

        CelestialBodyRegistry.replaceAll(loaded);
        LOGGER.info("Loaded {} BeyondOrbit celestial body definitions for {}", loaded.size(), BeyondOrbitCore.MODID);
    }
}
