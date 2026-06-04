package me.myogoo.beyondorbit.core.celestial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record CelestialBodyDefinition(
        ResourceLocation id,
        CelestialBodyType type,
        ResourceMode resourceMode,
        int tier,
        List<CelestialResourceDefinition> resources
) {
    public static final Codec<CelestialBodyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(CelestialBodyDefinition::id),
            CelestialBodyType.CODEC.fieldOf("type").forGetter(CelestialBodyDefinition::type),
            ResourceMode.CODEC.optionalFieldOf("resource_mode", ResourceMode.CONFIG_DEFAULT).forGetter(CelestialBodyDefinition::resourceMode),
            Codec.INT.optionalFieldOf("tier", 1).forGetter(CelestialBodyDefinition::tier),
            CelestialResourceDefinition.CODEC.listOf().fieldOf("resources").forGetter(CelestialBodyDefinition::resources)
    ).apply(instance, CelestialBodyDefinition::new));

    public CelestialBodyDefinition {
        resources = List.copyOf(resources);
        if (tier <= 0) {
            throw new IllegalArgumentException("Celestial body tier must be positive: " + id);
        }
        if (resources.isEmpty()) {
            throw new IllegalArgumentException("Celestial body must define at least one resource: " + id);
        }
    }
}
