package me.myogoo.beyondorbit.core.blockentity;

import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.menu.SatelliteUplinkMenu;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
import me.myogoo.beyondorbit.core.satellite.SatelliteUplinkService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SatelliteUplinkBlockEntity extends BlockEntity implements MenuProvider {
    private static final String ENERGY_TAG = "energy";

    private int energy;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy;
                case 1 -> energyCapacity();
                case 2 -> deployEnergyCost();
                case 3 -> energyGeneratedPerTick();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                energy = Math.max(0, Math.min(value, energyCapacity()));
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public SatelliteUplinkBlockEntity(BlockPos pos, BlockState state) {
        super(BeyondOrbitContent.SATELLITE_UPLINK_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SatelliteUplinkBlockEntity blockEntity) {
        int before = blockEntity.energy;
        blockEntity.energy = Math.min(blockEntity.energyCapacity(), blockEntity.energy + blockEntity.energyGeneratedPerTick());
        if (blockEntity.energy != before) {
            blockEntity.setChanged();
        }
    }

    public boolean consumeDeployEnergy(Player player) {
        int cost = deployEnergyCost();
        if (player.getAbilities().instabuild || cost <= 0) {
            return true;
        }
        if (energy < cost) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.uplink.not_enough_energy", energy, cost));
            return false;
        }
        energy -= cost;
        setChanged();
        return true;
    }

    public int energy() {
        return energy;
    }

    public int energyCapacity() {
        return Config.satelliteUplinkEnergyCapacity;
    }

    public int deployEnergyCost() {
        return Config.satelliteUplinkDeployEnergyCost;
    }

    public int energyGeneratedPerTick() {
        return Config.satelliteUplinkEnergyGeneratedPerTick;
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    @Nullable
    public ResourceLocation linkedSatelliteId() {
        return level == null ? null : SatelliteUplinkService.satelliteIdFor(level, worldPosition);
    }

    @Nullable
    public SatelliteMiningMissionState linkedSatellite() {
        if (level == null || level.getServer() == null) {
            return null;
        }
        ResourceLocation satelliteId = linkedSatelliteId();
        return satelliteId == null ? null : me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData.get(level.getServer()).getSatellite(satelliteId).orElse(null);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = Math.max(0, Math.min(tag.getInt(ENERGY_TAG), energyCapacity()));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(ENERGY_TAG, energy);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.beyondorbit.satellite_uplink");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SatelliteUplinkMenu(containerId, playerInventory, this);
    }
}
