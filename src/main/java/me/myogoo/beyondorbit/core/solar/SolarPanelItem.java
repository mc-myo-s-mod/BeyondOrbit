package me.myogoo.beyondorbit.core.solar;

import me.myogoo.beyondorbit.core.tier.TierableComponentItem;
import me.myogoo.beyondorbit.core.tier.TierableItemStats;
import me.myogoo.beyondorbit.core.tier.TierableItemType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SolarPanelItem extends Item {
    private final SolarPanelTier tier;

    public SolarPanelItem(SolarPanelTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public SolarPanelTier tier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        TierableComponentItem.appendTierableTooltip(
                TierableItemType.SOLAR_PANEL,
                tier.tierableTier(),
                TierableItemStats.solar(tier.tierableTier(), tier.generationFePerTick(), tier.transmissionDistanceKm(), tier.outputPercent()),
                tooltipComponents
        );
    }
}
