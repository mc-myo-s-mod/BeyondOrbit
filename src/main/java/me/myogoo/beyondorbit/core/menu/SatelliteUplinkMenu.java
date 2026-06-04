package me.myogoo.beyondorbit.core.menu;

import me.myogoo.beyondorbit.core.blockentity.SatelliteUplinkBlockEntity;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SatelliteUplinkMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final ResourceLocation satelliteId;
    private final boolean active;
    private final String targetBody;
    private final long completedExtractions;
    private final int bufferedResourceTypes;

    public SatelliteUplinkMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, findBlockEntity(playerInventory, buffer.readBlockPos()));
    }

    public SatelliteUplinkMenu(int containerId, Inventory playerInventory, SatelliteUplinkBlockEntity blockEntity) {
        super(BeyondOrbitContent.SATELLITE_UPLINK_MENU.get(), containerId);
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.data = blockEntity.dataAccess();
        var satellite = blockEntity.linkedSatellite();
        var id = blockEntity.linkedSatelliteId();
        this.satelliteId = id;
        this.active = satellite != null && satellite.active();
        this.targetBody = satellite == null || satellite.targetBody() == null ? "<none>" : satellite.targetBody().toString();
        this.completedExtractions = satellite == null ? 0L : satellite.completedExtractions();
        this.bufferedResourceTypes = satellite == null ? 0 : satellite.totalExtractedView().size();
        addDataSlots(data);
    }

    private static SatelliteUplinkBlockEntity findBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SatelliteUplinkBlockEntity uplink) {
            return uplink;
        }
        throw new IllegalStateException("Expected Satellite Uplink block entity at " + pos);
    }

    public int energy() {
        return data.get(0);
    }

    public int energyCapacity() {
        return data.get(1);
    }

    public int deployEnergyCost() {
        return data.get(2);
    }

    public int energyGeneratedPerTick() {
        return data.get(3);
    }

    public String satelliteIdText() {
        return satelliteId == null ? "<unlinked>" : satelliteId.toString();
    }

    public boolean active() {
        return active;
    }

    public String targetBody() {
        return targetBody;
    }

    public long completedExtractions() {
        return completedExtractions;
    }

    public int bufferedResourceTypes() {
        return bufferedResourceTypes;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, BeyondOrbitContent.SATELLITE_UPLINK.get());
    }
}
