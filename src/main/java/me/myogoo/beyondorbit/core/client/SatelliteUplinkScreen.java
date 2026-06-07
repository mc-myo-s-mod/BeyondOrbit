package me.myogoo.beyondorbit.core.client;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.menu.SatelliteUplinkMenu;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class SatelliteUplinkScreen extends AbstractContainerScreen<SatelliteUplinkMenu> {
    public SatelliteUplinkScreen(SatelliteUplinkMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 196;
        this.imageHeight = 148;
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF10141A);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF1B2430);
        graphics.fill(x + 10, y + 38, x + imageWidth - 10, y + 50, 0xFF0B0F14);
        int width = menu.energyCapacity() <= 0 ? 0 : (int) ((imageWidth - 22L) * menu.energy() / Math.max(1, menu.energyCapacity()));
        graphics.fill(x + 11, y + 39, x + 11 + width, y + 49, 0xFF4AB6FF);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0xFFE8EEF7, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.uplink.energy", menu.energy(), menu.energyCapacity()), 12, 24, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.uplink.deploy_cost", menu.deployEnergyCost()), 12, 56, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.uplink.generation", menu.energyGeneratedPerTick()), 12, 68, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.uplink.satellite", menu.satelliteIdText()), 12, 84, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.uplink.target", menu.targetBody()), 12, 96, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.uplink.extractions", menu.completedExtractions(), menu.bufferedResourceTypes()), 12, 108, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.modules", menu.miningModuleLevel(), menu.speedModuleLevel(), menu.efficiencyModuleLevel()), 12, 120, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.uplink.collect_hint"), 12, 134, 0xFF7F91A7, false);
    }

    @EventBusSubscriber(modid = BeyondOrbitCore.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(BeyondOrbitContent.SATELLITE_UPLINK_MENU.get(), SatelliteUplinkScreen::new);
        }
    }
}
