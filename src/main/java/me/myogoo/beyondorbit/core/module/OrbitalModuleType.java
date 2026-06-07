package me.myogoo.beyondorbit.core.module;

import net.minecraft.network.chat.Component;

public enum OrbitalModuleType {
    MINING("mining", "module.beyondorbit.type.mining"),
    SPEED("speed", "module.beyondorbit.type.speed"),
    EFFICIENCY("efficiency", "module.beyondorbit.type.efficiency");

    private final String serializedName;
    private final String translationKey;

    OrbitalModuleType(String serializedName, String translationKey) {
        this.serializedName = serializedName;
        this.translationKey = translationKey;
    }

    public String serializedName() {
        return serializedName;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public static OrbitalModuleType bySerializedName(String serializedName) {
        for (OrbitalModuleType type : values()) {
            if (type.serializedName.equals(serializedName)) {
                return type;
            }
        }
        return null;
    }
}
