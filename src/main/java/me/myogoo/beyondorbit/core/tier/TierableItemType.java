package me.myogoo.beyondorbit.core.tier;

import net.minecraft.network.chat.Component;

public enum TierableItemType {
    BODY("body"),
    SOLAR_PANEL("solar_panel"),
    RECEIVER("receiver"),
    TRANSMITTER("transmitter"),
    TELESCOPE("telescope");

    private final String serializedName;

    TierableItemType(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public Component displayName() {
        return Component.translatable("tierable.beyondorbit.type." + serializedName);
    }

    public String statTranslationPrefix() {
        return "tooltip.beyondorbit.tierable." + serializedName + ".";
    }

    public static TierableItemType bySerializedName(String serializedName) {
        for (TierableItemType type : values()) {
            if (type.serializedName.equals(serializedName)) {
                return type;
            }
        }
        return null;
    }
}
