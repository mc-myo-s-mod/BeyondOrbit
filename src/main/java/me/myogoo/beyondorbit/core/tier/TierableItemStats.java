package me.myogoo.beyondorbit.core.tier;

import java.util.Map;

public record TierableItemStats(
        TierableItemType type,
        TierableItemTier tier,
        int primaryValue,
        int secondaryValue,
        int tertiaryValue
) {
    private static final Map<TierableItemType, Map<TierableItemTier, TierableItemStats>> DEFAULTS = Map.of(
            TierableItemType.BODY, Map.of(
                    TierableItemTier.BASIC, new TierableItemStats(TierableItemType.BODY, TierableItemTier.BASIC, 10_000, 100, 1),
                    TierableItemTier.ADVANCED, new TierableItemStats(TierableItemType.BODY, TierableItemTier.ADVANCED, 50_000, 250, 2),
                    TierableItemTier.ELITE, new TierableItemStats(TierableItemType.BODY, TierableItemTier.ELITE, 200_000, 500, 3)
            ),
            TierableItemType.SOLAR_PANEL, Map.of(
                    TierableItemTier.BASIC, new TierableItemStats(TierableItemType.SOLAR_PANEL, TierableItemTier.BASIC, 100, 2_000, 100),
                    TierableItemTier.ADVANCED, new TierableItemStats(TierableItemType.SOLAR_PANEL, TierableItemTier.ADVANCED, 150, 4_000, 150),
                    TierableItemTier.ELITE, new TierableItemStats(TierableItemType.SOLAR_PANEL, TierableItemTier.ELITE, 250, 8_000, 250)
            ),
            TierableItemType.RECEIVER, Map.of(
                    TierableItemTier.BASIC, new TierableItemStats(TierableItemType.RECEIVER, TierableItemTier.BASIC, 1_024, 16, 100_000),
                    TierableItemTier.ADVANCED, new TierableItemStats(TierableItemType.RECEIVER, TierableItemTier.ADVANCED, 4_096, 32, 500_000),
                    TierableItemTier.ELITE, new TierableItemStats(TierableItemType.RECEIVER, TierableItemTier.ELITE, 16_384, 64, 2_000_000)
            ),
            TierableItemType.TRANSMITTER, Map.of(
                    TierableItemTier.BASIC, new TierableItemStats(TierableItemType.TRANSMITTER, TierableItemTier.BASIC, 50_000, 1_024, 12),
                    TierableItemTier.ADVANCED, new TierableItemStats(TierableItemType.TRANSMITTER, TierableItemTier.ADVANCED, 250_000, 4_096, 8),
                    TierableItemTier.ELITE, new TierableItemStats(TierableItemType.TRANSMITTER, TierableItemTier.ELITE, 1_000_000, 16_384, 4)
            ),
            TierableItemType.TELESCOPE, Map.of(
                    TierableItemTier.BASIC, new TierableItemStats(TierableItemType.TELESCOPE, TierableItemTier.BASIC, 1, 200, 1),
                    TierableItemTier.ADVANCED, new TierableItemStats(TierableItemType.TELESCOPE, TierableItemTier.ADVANCED, 2, 100, 2),
                    TierableItemTier.ELITE, new TierableItemStats(TierableItemType.TELESCOPE, TierableItemTier.ELITE, 3, 40, 3)
            )
    );

    public static TierableItemStats defaults(TierableItemType type, TierableItemTier tier) {
        Map<TierableItemTier, TierableItemStats> byTier = DEFAULTS.get(type);
        if (byTier == null || !byTier.containsKey(tier)) {
            throw new IllegalArgumentException("No default tierable stats for " + type + "/" + tier);
        }
        return byTier.get(tier);
    }

    public static TierableItemStats solar(TierableItemTier tier, int generationFePerTick, int distanceKm, int outputPercent) {
        return new TierableItemStats(TierableItemType.SOLAR_PANEL, tier, generationFePerTick, distanceKm, outputPercent);
    }

    public int energyCapacity() {
        return primaryValue;
    }

    public int durabilityOrSize() {
        return secondaryValue;
    }

    public int moduleSlots() {
        return tertiaryValue;
    }

    public int generationFePerTick() {
        return primaryValue;
    }

    public int transmissionDistanceKm() {
        return secondaryValue;
    }

    public int outputPercent() {
        return tertiaryValue;
    }

    public int throughputFePerTick() {
        return primaryValue;
    }

    public int wirelessRangeBlocks() {
        return secondaryValue;
    }

    public int bufferCapacity() {
        return tertiaryValue;
    }

    public int lossPercentPer1000Km() {
        return tertiaryValue;
    }

    public int maxObservableTier() {
        return primaryValue;
    }

    public int observationTicks() {
        return secondaryValue;
    }

    public int discoveriesPerUse() {
        return tertiaryValue;
    }
}
