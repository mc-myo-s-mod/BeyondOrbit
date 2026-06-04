package me.myogoo.beyondorbit.core.menu;

import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
import me.myogoo.beyondorbit.core.satellite.SatelliteUplinkService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LaunchPadMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 8;

    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final ResourceLocation satelliteId;

    public LaunchPadMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, playerInventory.player.level(), buffer.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    public LaunchPadMenu(int containerId, Inventory playerInventory, Level level, BlockPos pos) {
        this(containerId, playerInventory, level, pos, createData(level, pos));
    }

    private LaunchPadMenu(int containerId, Inventory playerInventory, Level level, BlockPos pos, ContainerData data) {
        super(BeyondOrbitContent.LAUNCH_PAD_MENU.get(), containerId);
        this.access = ContainerLevelAccess.create(level, pos);
        this.data = data;
        this.satelliteId = SatelliteUplinkService.launchPadSatelliteIdFor(level, pos);
        addDataSlots(data);
    }

    private static ContainerData createData(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return new SimpleContainerData(DATA_COUNT);
        }
        ResourceLocation satelliteId = SatelliteUplinkService.launchPadSatelliteIdFor(level, pos);
        return new ContainerData() {
            @Override
            public int get(int index) {
                SatelliteMiningMissionState satellite = BeyondOrbitSavedData.get(serverLevel.getServer()).getSatellite(satelliteId).orElse(null);
                return switch (index) {
                    case 0 -> satellite == null ? 0 : 1;
                    case 1 -> satellite != null && satellite.active() ? 1 : 0;
                    case 2 -> satellite == null ? 0 : satellite.ticksUntilNextExtraction();
                    case 3 -> satellite == null ? 0 : clampLongToInt(satellite.completedExtractions());
                    case 4 -> satellite == null ? 0 : satellite.totalExtractedView().size();
                    case 5 -> satellite == null ? 0 : clampLongToInt(satellite.totalExtractedView().values().stream().mapToLong(Long::longValue).sum());
                    case 6 -> satellite == null ? 0 : satellite.rollsPerExtraction();
                    case 7 -> satellite == null ? 0 : satellite.ticksPerExtraction();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Read-only status menu.
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    private static int clampLongToInt(long value) {
        if (value <= 0L) {
            return 0;
        }
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    public String satelliteIdText() {
        return satelliteId.toString();
    }

    public boolean hasSatellite() {
        return data.get(0) != 0;
    }

    public boolean active() {
        return data.get(1) != 0;
    }

    public int ticksUntilNextExtraction() {
        return data.get(2);
    }

    public int completedExtractions() {
        return data.get(3);
    }

    public int bufferedResourceTypes() {
        return data.get(4);
    }

    public int bufferedItemCount() {
        return data.get(5);
    }

    public int rollsPerExtraction() {
        return data.get(6);
    }

    public int ticksPerExtraction() {
        return data.get(7);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, BeyondOrbitContent.LAUNCH_PAD.get());
    }
}
