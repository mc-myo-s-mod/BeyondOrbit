package me.myogoo.beyondorbit.core.tier;

import net.minecraft.network.chat.Component;

public enum TierableItemTier {
    BASIC("basic", 1),
    ADVANCED("advanced", 2),
    ELITE("elite", 3);

    private final String serializedName;
    private final int level;

    TierableItemTier(String serializedName, int level) {
        this.serializedName = serializedName;
        this.level = level;
    }

    public String serializedName() {
        return serializedName;
    }

    public int level() {
        return level;
    }

    public Component displayName() {
        return Component.translatable("tierable.beyondorbit.tier." + serializedName);
    }

    public static TierableItemTier bySerializedName(String serializedName) {
        for (TierableItemTier tier : values()) {
            if (tier.serializedName.equals(serializedName)) {
                return tier;
            }
        }
        return null;
    }
}
