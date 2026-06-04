package me.myogoo.beyondorbit.core.client;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.menu.LaunchPadMenu;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class LaunchPadScreen extends AbstractContainerScreen<LaunchPadMenu> {
    public LaunchPadScreen(LaunchPadMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 210;
        this.imageHeight = 148;
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF101218);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF202631);
        graphics.fill(x + 10, y + 34, x + imageWidth - 10, y + 56, 0xFF111820);
        graphics.fill(x + 12, y + 36, x + imageWidth - 12, y + 54, menu.active() ? 0xFF315F7E : 0xFF3A3F48);
        graphics.fill(x + 10, y + 86, x + imageWidth - 10, y + 88, 0xFF4D5B6B);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0xFFE8EEF7, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.launch_pad.satellite", menu.satelliteIdText()), 12, 22, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable(menu.hasSatellite() ? "screen.beyondorbit.launch_pad.linked" : "screen.beyondorbit.launch_pad.unlinked"), 14, 42, 0xFFE8EEF7, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.launch_pad.active", menu.active() ? Component.translatable("screen.beyondorbit.common.yes") : Component.translatable("screen.beyondorbit.common.no")), 12, 64, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.launch_pad.extractions", menu.completedExtractions(), menu.ticksUntilNextExtraction()), 12, 76, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.launch_pad.buffer", menu.bufferedItemCount(), menu.bufferedResourceTypes()), 12, 96, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.launch_pad.mission", menu.rollsPerExtraction(), menu.ticksPerExtraction()), 12, 108, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.launch_pad.collect_hint"), 12, 130, 0xFF7F91A7, false);
    }

    @EventBusSubscriber(modid = BeyondOrbitCore.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(BeyondOrbitContent.LAUNCH_PAD_MENU.get(), LaunchPadScreen::new);
        }
    }
}
