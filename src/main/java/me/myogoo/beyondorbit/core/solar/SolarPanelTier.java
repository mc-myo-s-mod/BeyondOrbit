package me.myogoo.beyondorbit.core.solar;

import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.tier.TierableItemTier;
import net.minecraft.network.chat.Component;

public enum SolarPanelTier {
    BASIC("basic", 1),
    ADVANCED("advanced", 2),
    ELITE("elite", 3);

    private final String serializedName;
    private final int level;

    SolarPanelTier(String serializedName, int level) {
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
        return Component.translatable("solar_panel.beyondorbit.tier." + serializedName);
    }

    public int outputPercent() {
        return switch (this) {
            case BASIC -> Config.orbitalReceiverBasicSolarPanelOutputPercent;
            case ADVANCED -> Config.orbitalReceiverAdvancedSolarPanelOutputPercent;
            case ELITE -> Config.orbitalReceiverEliteSolarPanelOutputPercent;
        };
    }

    public TierableItemTier tierableTier() {
        return switch (this) {
            case BASIC -> TierableItemTier.BASIC;
            case ADVANCED -> TierableItemTier.ADVANCED;
            case ELITE -> TierableItemTier.ELITE;
        };
    }

    public int generationFePerTick() {
        return Math.max(0, Config.orbitalReceiverSolarFePerTick * outputPercent() / 100);
    }

    public int transmissionDistanceKm() {
        return switch (this) {
            case BASIC -> Config.lowOrbitSolarDistanceKm;
            case ADVANCED -> Config.lowOrbitSolarDistanceKm * 2;
            case ELITE -> Config.lowOrbitSolarDistanceKm * 4;
        };
    }

    public static SolarPanelTier bySerializedName(String serializedName) {
        for (SolarPanelTier tier : values()) {
            if (tier.serializedName.equals(serializedName)) {
                return tier;
            }
        }
        return null;
    }
}
