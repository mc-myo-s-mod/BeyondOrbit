package me.myogoo.beyondorbit.core.menu;

import me.myogoo.beyondorbit.core.blockentity.OrbitalReceiverBlockEntity;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class OrbitalReceiverMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public OrbitalReceiverMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, findBlockEntity(playerInventory, buffer.readBlockPos()));
    }

    public OrbitalReceiverMenu(int containerId, Inventory playerInventory, OrbitalReceiverBlockEntity blockEntity) {
        super(BeyondOrbitContent.ORBITAL_RECEIVER_MENU.get(), containerId);
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.data = blockEntity.dataAccess();
        addDataSlots(data);
    }

    private static OrbitalReceiverBlockEntity findBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof OrbitalReceiverBlockEntity receiver) {
            return receiver;
        }
        throw new IllegalStateException("Expected Orbital Receiver block entity at " + pos);
    }

    public int energy() {
        return data.get(0);
    }

    public int energyCapacity() {
        return data.get(1);
    }

    public int storedItemCount() {
        return data.get(2);
    }

    public int occupiedSlots() {
        return data.get(3);
    }

    public int slotCount() {
        return data.get(4);
    }

    public int solarGenerationPerTick() {
        return data.get(5);
    }

    public int transferPerTick() {
        return data.get(6);
    }

    public int maxItemsPerTick() {
        return data.get(7);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, BeyondOrbitContent.ORBITAL_RECEIVER.get());
    }
}
