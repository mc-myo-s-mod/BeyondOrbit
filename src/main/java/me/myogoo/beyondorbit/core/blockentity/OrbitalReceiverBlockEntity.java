package me.myogoo.beyondorbit.core.blockentity;

import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.menu.OrbitalReceiverMenu;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
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

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStored();
                case 1 -> energyCapacity();
                case 2 -> storedItemCount();
                case 3 -> occupiedSlots();
                case 4 -> SLOT_COUNT;
                case 5 -> Config.orbitalReceiverSolarFePerTick;
                case 6 -> Config.orbitalReceiverTransferFePerTick;
                case 7 -> Config.orbitalReceiverMaxItemsPerTick;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // The receiver screen is read-only; mutations happen through block ticks and player actions.
        }

        @Override
        public int getCount() {
            return 8;
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
        changed |= blockEntity.receiveOrbitalSolarEnergy();
        changed |= blockEntity.receiveSatelliteResources(BeyondOrbitSavedData.get(level.getServer()));
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

    private boolean receiveOrbitalSolarEnergy() {
        int generated = Config.orbitalReceiverSolarFePerTick;
        if (generated <= 0) {
            return false;
        }
        return energy.receiveEnergy(generated, false) > 0;
    }

    private boolean receiveSatelliteResources(BeyondOrbitSavedData data) {
        int remainingBudget = Config.orbitalReceiverMaxItemsPerTick;
        boolean changed = false;
        for (SatelliteMiningMissionState satellite : data.satellites()) {
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
