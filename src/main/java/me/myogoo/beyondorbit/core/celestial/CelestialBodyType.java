package me.myogoo.beyondorbit.core.celestial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum CelestialBodyType implements StringRepresentable {
    PLANET,
    ASTEROID;

    public static final Codec<CelestialBodyType> CODEC = StringRepresentable.fromEnum(CelestialBodyType::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
