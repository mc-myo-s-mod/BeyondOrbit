package me.myogoo.beyondorbit.core.blockentity;

import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.menu.OrbitalReceiverMenu;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
import me.myogoo.beyondorbit.core.solar.SolarPanelTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrbitalReceiverBlockEntity extends BlockEntity implements MenuProvider {
    private static final String ITEMS_TAG = "items";
    private static final String ENERGY_TAG = "energy";
    public static final int SLOT_COUNT = 18;
    private final int[] syncedData = new int[12];

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (level != null && level.isClientSide()) {
                return index >= 0 && index < syncedData.length ? syncedData[index] : 0;
            }
            return switch (index) {
                case 0 -> energyStored();
                case 1 -> energyCapacity();
                case 2 -> storedItemCount();
                case 3 -> occupiedSlots();
                case 4 -> SLOT_COUNT;
                case 5 -> currentSolarGenerationPerTick();
                case 6 -> Config.orbitalReceiverTransferFePerTick;
                case 7 -> Config.orbitalReceiverMaxItemsPerTick;
                case 8 -> currentDeployedSolarSatellites();
                case 9 -> currentActiveSolarSatellites();
                case 10 -> currentSolarGrossGenerationPerTick();
                case 11 -> currentSolarTransmissionLossPercent();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index >= 0 && index < syncedData.length) {
                syncedData[index] = value;
            }
        }

        @Override
        public int getCount() {
            return 12;
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final EnergyStorage energy = new EnergyStorage(Config.orbitalReceiverEnergyCapacity, Config.orbitalReceiverTransferFePerTick, Config.orbitalReceiverTransferFePerTick) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) {
                setChanged();
            }
            return extracted;
        }
    };

    public OrbitalReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(BeyondOrbitContent.ORBITAL_RECEIVER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OrbitalReceiverBlockEntity blockEntity) {
        if (level.getServer() == null) {
            return;
        }
        boolean changed = false;
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(level.getServer());
        changed |= blockEntity.receiveOrbitalSolarEnergy(data);
        changed |= blockEntity.receiveSatelliteResources(data);
        changed |= blockEntity.exportEnergyToNeighbors(level, pos);
        if (changed) {
            blockEntity.setChanged();
        }
    }

    public ItemStackHandler items() {
        return items;
    }

    public IEnergyStorage energy() {
        return energy;
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public int energyStored() {
        return energy.getEnergyStored();
    }

    public int energyCapacity() {
        return energy.getMaxEnergyStored();
    }

    public int occupiedSlots() {
        int occupied = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                occupied++;
            }
        }
        return occupied;
    }

    public int storedItemCount() {
        int total = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            total += items.getStackInSlot(i).getCount();
        }
        return total;
    }

    public int collectToPlayer(Player player) {
        int moved = 0;
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack toMove = stack.copy();
            if (player.addItem(toMove)) {
                moved += stack.getCount();
                items.setStackInSlot(i, ItemStack.EMPTY);
            } else if (toMove.getCount() < stack.getCount()) {
                int accepted = stack.getCount() - toMove.getCount();
                moved += accepted;
                stack.shrink(accepted);
                items.setStackInSlot(i, stack);
            }
        }
        if (moved > 0) {
            setChanged();
        }
        return moved;
    }

    public List<ItemStack> copyStoredItems() {
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
    }

    public int currentSolarGenerationPerTick() {
        if (level == null || level.getServer() == null) {
            return 0;
        }
        return solarGenerationPerTick(BeyondOrbitSavedData.get(level.getServer()));
    }

    public int currentSolarGrossGenerationPerTick() {
        if (level == null || level.getServer() == null) {
            return 0;
        }
        return solarGrossGenerationPerTick(BeyondOrbitSavedData.get(level.getServer()));
    }

    public int currentSolarTransmissionLossPercent() {
        if (level == null || level.getServer() == null) {
            return 0;
        }
        return solarTransmissionLossPercent(BeyondOrbitSavedData.get(level.getServer()));
    }

    public int currentDeployedSolarSatellites() {
        if (level == null || level.getServer() == null) {
            return 0;
        }
        return BeyondOrbitSavedData.get(level.getServer()).lowOrbitSolarSatelliteCount();
    }

    public int currentActiveSolarSatellites() {
        if (level == null || level.getServer() == null) {
            return 0;
        }
        return BeyondOrbitSavedData.get(level.getServer()).activeLowOrbitSolarSatelliteCount();
    }

    private boolean receiveOrbitalSolarEnergy(BeyondOrbitSavedData data) {
        int generated = solarGenerationPerTick(data);
        int stored = storeOrbitalEnergy(data, generated);
        int direct = Math.max(0, generated - stored);
        int capacityLeft = Math.max(0, energy.getMaxEnergyStored() - energy.getEnergyStored());
        int fromStorage = extractStoredOrbitalEnergy(data, Math.max(0, capacityLeft - direct));
        int received = direct + fromStorage;
        if (received <= 0) {
            return false;
        }
        return energy.receiveEnergy(received, false) > 0 || stored > 0 || fromStorage > 0;
    }

    public static int storeOrbitalEnergy(BeyondOrbitSavedData data, int amount) {
        if (amount <= 0 || Config.orbitalEnergyStorageTransferFePerTick <= 0) {
            return 0;
        }
        int remaining = amount;
        int stored = 0;
        for (SatelliteMiningMissionState satellite : data.activeEnergyStorageSatellites()) {
            if (remaining <= 0) {
                break;
            }
            int moved = satellite.receiveEnergy(remaining, Config.orbitalEnergyStorageTransferFePerTick);
            if (moved > 0) {
                remaining -= moved;
                stored += moved;
            }
        }
        if (stored > 0) {
            data.setDirty();
        }
        return stored;
    }

    public static int extractStoredOrbitalEnergy(BeyondOrbitSavedData data, int amount) {
        if (amount <= 0 || Config.orbitalEnergyStorageTransferFePerTick <= 0) {
            return 0;
        }
        int remaining = amount;
        int extracted = 0;
        for (SatelliteMiningMissionState satellite : data.activeEnergyStorageSatellites()) {
            if (remaining <= 0) {
                break;
            }
            int moved = satellite.extractEnergy(remaining, Config.orbitalEnergyStorageTransferFePerTick);
            if (moved > 0) {
                remaining -= moved;
                extracted += moved;
            }
        }
        if (extracted > 0) {
            data.setDirty();
        }
        return extracted;
    }

    public static int transmissionLossPercentForDistance(int distanceKm) {
        long loss = (long) Math.max(0, distanceKm) * Config.orbitalReceiverTransmissionLossPercentPer1000Km / 1000L;
        return (int) Math.min(100L, Math.max(0L, loss));
    }

    public static int solarGrossOutputPerActiveSatellite(SatelliteMiningMissionState satellite) {
        if (satellite == null || !satellite.isLowOrbitSolar()) {
            return 0;
        }
        long gross = (long) Config.orbitalReceiverSolarFePerTick * satellite.solarPanelTier().outputPercent() / 100L;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, gross));
    }

    public static int solarOutputPerActiveSatellite(SatelliteMiningMissionState satellite) {
        int gross = solarGrossOutputPerActiveSatellite(satellite);
        int lossPercent = transmissionLossPercentForDistance(satellite.orbitDistanceKm());
        long net = (long) gross * (100L - lossPercent) / 100L;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, net));
    }

    public static int solarOutputPerActiveSatellite() {
        SatelliteMiningMissionState preview = new SatelliteMiningMissionState(
                ResourceLocation.fromNamespaceAndPath("beyondorbit", "preview_low_orbit_solar")
        );
        preview.markLowOrbitSolar(0, SolarPanelTier.BASIC, Config.lowOrbitSolarDistanceKm);
        return solarOutputPerActiveSatellite(preview);
    }

    public static int solarGrossGenerationPerTick(BeyondOrbitSavedData data) {
        long generated = data.activeLowOrbitSolarSatellites().stream()
                .mapToLong(OrbitalReceiverBlockEntity::solarGrossOutputPerActiveSatellite)
                .sum();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, generated));
    }

    public static int solarGenerationPerTick(BeyondOrbitSavedData data) {
        long generated = data.activeLowOrbitSolarSatellites().stream()
                .mapToLong(OrbitalReceiverBlockEntity::solarOutputPerActiveSatellite)
                .sum();
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, generated));
    }

    public static int solarTransmissionLossPercent(BeyondOrbitSavedData data) {
        int gross = solarGrossGenerationPerTick(data);
        int net = solarGenerationPerTick(data);
        if (gross <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(100, 100 - (int) ((long) net * 100L / gross)));
    }

    private boolean receiveSatelliteResources(BeyondOrbitSavedData data) {
        int remainingBudget = Config.orbitalReceiverMaxItemsPerTick;
        boolean changed = false;
        for (SatelliteMiningMissionState satellite : data.satellites()) {
            if (satellite.isLowOrbitSolar() || satellite.isEnergyStorage()) {
                continue;
            }
            if (satellite.satelliteId().getPath().startsWith("uplink_")) {
                continue;
            }
            if (remainingBudget <= 0) {
                break;
            }
            for (Map.Entry<ResourceLocation, Long> entry : List.copyOf(satellite.totalExtractedView().entrySet())) {
                if (remainingBudget <= 0) {
                    break;
                }
                Item item = BuiltInRegistries.ITEM.get(entry.getKey());
                if (item == Items.AIR) {
                    continue;
                }
                long wanted = Math.min(entry.getValue(), remainingBudget);
                long inserted = insertResource(item, wanted);
                if (inserted > 0L) {
                    satellite.removeExtracted(entry.getKey(), inserted);
                    remainingBudget -= (int) inserted;
                    changed = true;
                }
            }
        }
        if (changed) {
            data.setDirty();
        }
        return changed;
    }

    private long insertResource(Item item, long amount) {
        long inserted = 0L;
        int maxStackSize = Math.max(1, item.getDefaultMaxStackSize());
        while (amount > 0L) {
            int stackSize = (int) Math.min(amount, maxStackSize);
            ItemStack remaining = new ItemStack(item, stackSize);
            for (int slot = 0; slot < items.getSlots() && !remaining.isEmpty(); slot++) {
                remaining = items.insertItem(slot, remaining, false);
            }
            int accepted = stackSize - remaining.getCount();
            if (accepted <= 0) {
                break;
            }
            inserted += accepted;
            amount -= accepted;
        }
        return inserted;
    }

    private boolean exportEnergyToNeighbors(Level level, BlockPos pos) {
        if (Config.orbitalReceiverTransferFePerTick <= 0 || energy.getEnergyStored() <= 0) {
            return false;
        }
        int remaining = Math.min(Config.orbitalReceiverTransferFePerTick, energy.getEnergyStored());
        boolean changed = false;
        for (Direction direction : Direction.values()) {
            if (remaining <= 0) {
                break;
            }
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            BlockEntity neighborBlockEntity = level.getBlockEntity(neighborPos);
            IEnergyStorage target = Capabilities.EnergyStorage.BLOCK.getCapability(level, neighborPos, neighborState, neighborBlockEntity, direction.getOpposite());
            if (target == null || !target.canReceive()) {
                continue;
            }
            int moved = target.receiveEnergy(remaining, false);
            if (moved > 0) {
                energy.extractEnergy(moved, false);
                remaining -= moved;
                changed = true;
            }
        }
        return changed;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound(ITEMS_TAG));
        energy.deserializeNBT(registries, tag.get(ENERGY_TAG));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(ITEMS_TAG, items.serializeNBT(registries));
        tag.put(ENERGY_TAG, energy.serializeNBT(registries));
    }

    public Component statusMessage() {
        return Component.translatable(
                "message.beyondorbit.orbital_receiver.status",
                energyStored(),
                energyCapacity(),
                storedItemCount(),
                occupiedSlots(),
                SLOT_COUNT
        );
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.beyondorbit.orbital_receiver");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OrbitalReceiverMenu(containerId, playerInventory, this);
    }
}
