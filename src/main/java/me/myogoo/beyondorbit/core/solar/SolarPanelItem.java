package me.myogoo.beyondorbit.core.solar;

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
        tooltipComponents.add(Component.translatable("tooltip.beyondorbit.solar_panel.tier", tier.displayName(), tier.level()));
        tooltipComponents.add(Component.translatable("tooltip.beyondorbit.solar_panel.output", tier.outputPercent()));
    }
}
