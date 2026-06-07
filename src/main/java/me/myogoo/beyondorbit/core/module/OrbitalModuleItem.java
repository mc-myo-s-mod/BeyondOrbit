package me.myogoo.beyondorbit.core.module;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class OrbitalModuleItem extends Item {
    private final OrbitalModuleType moduleType;
    private final OrbitalModuleTier moduleTier;

    public OrbitalModuleItem(OrbitalModuleType moduleType, OrbitalModuleTier moduleTier, Properties properties) {
        super(properties);
        this.moduleType = moduleType;
        this.moduleTier = moduleTier;
    }

    public OrbitalModuleType moduleType() {
        return moduleType;
    }

    public OrbitalModuleTier moduleTier() {
        return moduleTier;
    }

    public int miningRollBonus() {
        return moduleType == OrbitalModuleType.MINING ? moduleTier.miningRollBonus() : 0;
    }

    public int speedTickReduction() {
        return moduleType == OrbitalModuleType.SPEED ? moduleTier.speedTickReduction() : 0;
    }

    public int efficiencyPercent() {
        return moduleType == OrbitalModuleType.EFFICIENCY ? moduleTier.efficiencyPercent() : 100;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.beyondorbit.module.type", moduleType.displayName()));
        tooltipComponents.add(Component.translatable("tooltip.beyondorbit.module.tier", moduleTier.displayName(), moduleTier.level()));
        switch (moduleType) {
            case MINING -> tooltipComponents.add(Component.translatable("tooltip.beyondorbit.module.mining", moduleTier.miningRollBonus()));
            case SPEED -> tooltipComponents.add(Component.translatable("tooltip.beyondorbit.module.speed", moduleTier.speedTickReduction()));
            case EFFICIENCY -> tooltipComponents.add(Component.translatable("tooltip.beyondorbit.module.efficiency", moduleTier.efficiencyPercent()));
        }
    }
}
