package me.myogoo.beyondorbit.core.celestial;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class CelestialBodyRegistry {
    private static final AtomicReference<Map<ResourceLocation, CelestialBodyDefinition>> BODIES = new AtomicReference<>(Map.of());

    private CelestialBodyRegistry() {
    }

    public static void replaceAll(Map<ResourceLocation, CelestialBodyDefinition> bodies) {
        BODIES.set(Map.copyOf(bodies));
    }

    public static Optional<CelestialBodyDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(BODIES.get().get(id));
    }

    public static Collection<CelestialBodyDefinition> all() {
        return BODIES.get().values();
    }

    public static int size() {
        return BODIES.get().size();
    }
}
