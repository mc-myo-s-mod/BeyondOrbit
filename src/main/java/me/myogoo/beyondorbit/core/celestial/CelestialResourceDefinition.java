package me.myogoo.beyondorbit.core.celestial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record CelestialResourceDefinition(
        ResourceLocation id,
        ResourceMode mode,
        long amount,
        int weight,
        long minYield,
        long maxYield
) {
    public static final Codec<CelestialResourceDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(CelestialResourceDefinition::id),
            ResourceMode.CODEC.optionalFieldOf("mode", ResourceMode.CONFIG_DEFAULT).forGetter(CelestialResourceDefinition::mode),
            Codec.LONG.optionalFieldOf("amount", 0L).forGetter(CelestialResourceDefinition::amount),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(CelestialResourceDefinition::weight),
            Codec.LONG.optionalFieldOf("min_yield", 1L).forGetter(CelestialResourceDefinition::minYield),
            Codec.LONG.optionalFieldOf("max_yield", 1L).forGetter(CelestialResourceDefinition::maxYield)
    ).apply(instance, CelestialResourceDefinition::new));

    public CelestialResourceDefinition {
        if (amount < 0) {
            throw new IllegalArgumentException("Celestial resource amount cannot be negative: " + id);
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Celestial resource weight must be positive: " + id);
        }
        if (minYield <= 0) {
            throw new IllegalArgumentException("Celestial resource min_yield must be positive: " + id);
        }
        if (maxYield < minYield) {
            throw new IllegalArgumentException("Celestial resource max_yield must be >= min_yield: " + id);
        }
    }
}
