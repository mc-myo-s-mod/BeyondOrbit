package me.myogoo.beyondorbit.core.tier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TierableComponentItem extends Item {
    private final TierableItemType type;
    private final TierableItemTier tier;

    public TierableComponentItem(TierableItemType type, TierableItemTier tier, Properties properties) {
        super(properties);
        this.type = type;
        this.tier = tier;
    }

    public TierableItemType type() {
        return type;
    }

    public TierableItemTier tier() {
        return tier;
    }

    public TierableItemStats stats() {
        return TierableItemStats.defaults(type, tier);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        appendTierableTooltip(type, tier, stats(), tooltipComponents);
    }

    public static void appendTierableTooltip(TierableItemType type, TierableItemTier tier, TierableItemStats stats, List<Component> tooltipComponents) {
        tooltipComponents.add(Component.translatable("tooltip.beyondorbit.tierable.type", type.displayName()));
        tooltipComponents.add(Component.translatable("tooltip.beyondorbit.tierable.tier", tier.displayName(), tier.level()));
        String prefix = type.statTranslationPrefix();
        switch (type) {
            case BODY -> {
                tooltipComponents.add(Component.translatable(prefix + "energy_capacity", stats.energyCapacity()));
                tooltipComponents.add(Component.translatable(prefix + "durability", stats.durabilityOrSize()));
                tooltipComponents.add(Component.translatable(prefix + "module_slots", stats.moduleSlots()));
            }
            case SOLAR_PANEL -> {
                tooltipComponents.add(Component.translatable(prefix + "generation", stats.generationFePerTick()));
                tooltipComponents.add(Component.translatable(prefix + "distance", stats.transmissionDistanceKm()));
                tooltipComponents.add(Component.translatable(prefix + "output", stats.outputPercent()));
            }
            case RECEIVER -> {
                tooltipComponents.add(Component.translatable(prefix + "throughput", stats.throughputFePerTick()));
                tooltipComponents.add(Component.translatable(prefix + "range", stats.wirelessRangeBlocks()));
                tooltipComponents.add(Component.translatable(prefix + "buffer", stats.bufferCapacity()));
            }
            case TRANSMITTER -> {
                tooltipComponents.add(Component.translatable(prefix + "capacity", stats.energyCapacity()));
                tooltipComponents.add(Component.translatable(prefix + "throughput", stats.throughputFePerTick()));
                tooltipComponents.add(Component.translatable(prefix + "loss", stats.lossPercentPer1000Km()));
            }
            case TELESCOPE -> {
                tooltipComponents.add(Component.translatable(prefix + "max_tier", stats.maxObservableTier()));
                tooltipComponents.add(Component.translatable(prefix + "observation_ticks", stats.observationTicks()));
                tooltipComponents.add(Component.translatable(prefix + "discoveries", stats.discoveriesPerUse()));
            }
        }
    }
}
