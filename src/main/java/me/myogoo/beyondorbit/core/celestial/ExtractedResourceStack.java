package me.myogoo.beyondorbit.core.celestial;

import net.minecraft.resources.ResourceLocation;

public record ExtractedResourceStack(ResourceLocation id, long amount) {
    public ExtractedResourceStack {
        if (amount <= 0L) {
            throw new IllegalArgumentException("Extracted resource amount must be positive");
        }
    }
}
