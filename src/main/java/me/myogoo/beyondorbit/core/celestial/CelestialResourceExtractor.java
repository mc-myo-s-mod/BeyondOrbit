package me.myogoo.beyondorbit.core.celestial;

import me.myogoo.beyondorbit.core.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CelestialResourceExtractor {
    private CelestialResourceExtractor() {
    }

    /**
     * Applies extraction to mutable celestial state. Call this from the main server thread.
     * Background workers should only calculate requests/candidates and let the main thread consume reserves.
     */
    public static ResourceExtractionResult extract(
            CelestialBodyDefinition definition,
            CelestialBodyState state,
            ResourceExtractionRequest request,
            RandomSource random
    ) {
        if (!definition.id().equals(state.bodyId())) {
            throw new IllegalArgumentException("Definition/state body id mismatch: " + definition.id() + " != " + state.bodyId());
        }

        Map<ResourceLocation, Long> extracted = new LinkedHashMap<>();
        for (int i = 0; i < request.rolls(); i++) {
            CelestialResourceDefinition selected = selectResource(definition, state, random);
            if (selected == null) {
                break;
            }

            long rawYield = randomYield(selected, random);
            long scaledYield = Math.max(1L, rawYield * request.yieldMultiplierNumerator() / request.yieldMultiplierDenominator());
            long appliedYield = scaledYield;

            if (!isInfinite(definition, selected)) {
                appliedYield = state.consume(selected.id(), scaledYield);
            }

            if (appliedYield > 0L) {
                extracted.merge(selected.id(), appliedYield, Long::sum);
            }
        }

        List<ExtractedResourceStack> stacks = new ArrayList<>();
        extracted.forEach((id, amount) -> {
            if (amount > 0L) {
                stacks.add(new ExtractedResourceStack(id, amount));
            }
        });
        return new ResourceExtractionResult(stacks, state.isDepleted());
    }

    private static CelestialResourceDefinition selectResource(
            CelestialBodyDefinition definition,
            CelestialBodyState state,
            RandomSource random
    ) {
        int totalWeight = 0;
        for (CelestialResourceDefinition resource : definition.resources()) {
            if (isInfinite(definition, resource) || state.remaining(resource.id()) > 0L) {
                totalWeight += resource.weight();
            }
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        for (CelestialResourceDefinition resource : definition.resources()) {
            if (!isInfinite(definition, resource) && state.remaining(resource.id()) <= 0L) {
                continue;
            }
            roll -= resource.weight();
            if (roll < 0) {
                return resource;
            }
        }
        return null;
    }

    private static long randomYield(CelestialResourceDefinition resource, RandomSource random) {
        long range = resource.maxYield() - resource.minYield();
        if (range <= 0L) {
            return resource.minYield();
        }
        long offset = nextLongBounded(random, range + 1L);
        return resource.minYield() + offset;
    }

    private static long nextLongBounded(RandomSource random, long bound) {
        if (bound <= 0L) {
            throw new IllegalArgumentException("bound must be positive");
        }
        long value = random.nextLong() >>> 1;
        return value % bound;
    }

    public static boolean isInfinite(CelestialBodyDefinition definition, CelestialResourceDefinition resource) {
        if (Config.forceInfiniteResources) {
            return true;
        }
        if (Config.forceFiniteResources) {
            return false;
        }

        ResourceMode mode = resource.mode() != ResourceMode.CONFIG_DEFAULT ? resource.mode() : definition.resourceMode();
        if (mode == ResourceMode.INFINITE) {
            return true;
        }
        if (mode == ResourceMode.FINITE) {
            return false;
        }
        return Config.infiniteCelestialResources;
    }
}
