package me.myogoo.beyondorbit.core.client;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.menu.OrbitalReceiverMenu;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class OrbitalReceiverScreen extends AbstractContainerScreen<OrbitalReceiverMenu> {
    public OrbitalReceiverScreen(OrbitalReceiverMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 210;
        this.imageHeight = 142;
        this.inventoryLabelY = 10_000;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF101218);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFF202734);
        graphics.fill(x + 12, y + 42, x + imageWidth - 12, y + 55, 0xFF0A0E13);
        int width = menu.energyCapacity() <= 0 ? 0 : (int) ((imageWidth - 26L) * menu.energy() / Math.max(1, menu.energyCapacity()));
        graphics.fill(x + 13, y + 43, x + 13 + width, y + 54, 0xFFFFC247);
        graphics.fill(x + 12, y + 85, x + imageWidth - 12, y + 98, 0xFF0A0E13);
        int slotWidth = menu.slotCount() <= 0 ? 0 : (int) ((imageWidth - 26L) * menu.occupiedSlots() / Math.max(1, menu.slotCount()));
        graphics.fill(x + 13, y + 86, x + 13 + slotWidth, y + 97, 0xFF77D18D);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 8, 0xFFE8EEF7, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.receiver.energy", menu.energy(), menu.energyCapacity()), 12, 26, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.receiver.generation", menu.solarGenerationPerTick()), 12, 60, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.receiver.transfer", menu.transferPerTick()), 12, 72, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.receiver.items", menu.storedItemCount(), menu.occupiedSlots(), menu.slotCount()), 12, 103, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.receiver.pull_rate", menu.maxItemsPerTick()), 12, 115, 0xFFB9C7D8, false);
        graphics.drawString(font, Component.translatable("screen.beyondorbit.receiver.collect_hint"), 12, 128, 0xFF7F91A7, false);
    }

    @EventBusSubscriber(modid = BeyondOrbitCore.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(BeyondOrbitContent.ORBITAL_RECEIVER_MENU.get(), OrbitalReceiverScreen::new);
        }
    }
}
