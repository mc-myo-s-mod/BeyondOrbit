package me.myogoo.beyondorbit.core.module;

import net.minecraft.network.chat.Component;

public enum OrbitalModuleTier {
    BASIC("basic", "module.beyondorbit.tier.basic", 1, 2, 20, 110),
    ADVANCED("advanced", "module.beyondorbit.tier.advanced", 2, 6, 50, 125),
    ELITE("elite", "module.beyondorbit.tier.elite", 3, 12, 100, 150);

    private final String serializedName;
    private final String translationKey;
    private final int level;
    private final int miningRollBonus;
    private final int speedTickReduction;
    private final int efficiencyPercent;

    OrbitalModuleTier(String serializedName, String translationKey, int level, int miningRollBonus, int speedTickReduction, int efficiencyPercent) {
        this.serializedName = serializedName;
        this.translationKey = translationKey;
        this.level = level;
        this.miningRollBonus = miningRollBonus;
        this.speedTickReduction = speedTickReduction;
        this.efficiencyPercent = efficiencyPercent;
    }

    public String serializedName() {
        return serializedName;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public int level() {
        return level;
    }

    public int miningRollBonus() {
        return miningRollBonus;
    }

    public int speedTickReduction() {
        return speedTickReduction;
    }

    public int efficiencyPercent() {
        return efficiencyPercent;
    }

    public static OrbitalModuleTier bySerializedName(String serializedName) {
        for (OrbitalModuleTier tier : values()) {
            if (tier.serializedName.equals(serializedName)) {
                return tier;
            }
        }
        return null;
    }
}
