package me.myogoo.beyondorbit.core.celestial;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum ResourceMode implements StringRepresentable {
    CONFIG_DEFAULT,
    FINITE,
    INFINITE;

    public static final Codec<ResourceMode> CODEC = StringRepresentable.fromEnum(ResourceMode::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
