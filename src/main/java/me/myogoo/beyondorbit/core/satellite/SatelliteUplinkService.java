package me.myogoo.beyondorbit.core.satellite;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.blockentity.OrbitalReceiverBlockEntity;
import me.myogoo.beyondorbit.core.blockentity.SatelliteUplinkBlockEntity;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyRegistry;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.module.OrbitalModuleItem;
import me.myogoo.beyondorbit.core.module.OrbitalModuleTier;
import me.myogoo.beyondorbit.core.module.OrbitalModuleType;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.solar.SolarPanelItem;
import me.myogoo.beyondorbit.core.solar.SolarPanelTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public final class SatelliteUplinkService {
    private SatelliteUplinkService() {
    }

    public static ResourceLocation satelliteIdFor(Level level, BlockPos pos) {
        String dimension = sanitizePath(level.dimension().location().toString());
        return ResourceLocation.fromNamespaceAndPath(
                BeyondOrbitCore.MODID,
                "uplink_" + dimension + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ()
        );
    }

    public static ResourceLocation launchPadSatelliteIdFor(Level level, BlockPos pos) {
        String dimension = sanitizePath(level.dimension().location().toString());
        return ResourceLocation.fromNamespaceAndPath(
                BeyondOrbitCore.MODID,
                "launchpad_" + dimension + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ()
        );
    }

    public static ResourceLocation lowOrbitSolarSatelliteIdFor(Level level, BlockPos pos) {
        String dimension = sanitizePath(level.dimension().location().toString());
        return ResourceLocation.fromNamespaceAndPath(
                BeyondOrbitCore.MODID,
                "low_orbit_solar_" + dimension + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ()
        );
    }

    public static ResourceLocation energyStorageSatelliteIdFor(Level level, BlockPos pos) {
        String dimension = sanitizePath(level.dimension().location().toString());
        return ResourceLocation.fromNamespaceAndPath(
                BeyondOrbitCore.MODID,
                "energy_storage_" + dimension + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ()
        );
    }

    public static boolean deploySatellite(Level level, BlockPos pos, Player player, ItemStack satelliteStack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        Optional<CelestialBodyDefinition> target = defaultMiningTarget(data);
        if (target.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    CelestialBodyRegistry.size() == 0
                            ? "message.beyondorbit.uplink.no_bodies"
                            : "message.beyondorbit.uplink.no_discovered_bodies"
            ));
            return false;
        }
        if (serverLevel.getBlockEntity(pos) instanceof SatelliteUplinkBlockEntity uplink && !uplink.consumeDeployEnergy(player)) {
            return false;
        }

        ResourceLocation satelliteId = satelliteIdFor(level, pos);
        CelestialBodyDefinition definition = target.get();
        data.getOrCreateState(definition);
        SatelliteMiningMissionState satellite = data.getOrCreateSatellite(satelliteId);
        int rolls = uplinkRolls(satellite.equippedModuleTier(OrbitalModuleType.MINING), satellite.equippedModuleTier(OrbitalModuleType.EFFICIENCY));
        int intervalTicks = uplinkIntervalTicks(satellite.equippedModuleTier(OrbitalModuleType.SPEED));
        satellite.startMining(definition.id(), rolls, intervalTicks);
        data.setDirty();

        if (!player.getAbilities().instabuild) {
            satelliteStack.shrink(1);
        }

        player.sendSystemMessage(Component.translatable(
                "message.beyondorbit.uplink.deployed",
                satelliteId.toString(),
                definition.id().toString(),
                rolls,
                intervalTicks
        ));
        return true;
    }

    public static boolean launchSatelliteFromPad(Level level, BlockPos pos, Player player, ItemStack satelliteStack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        Optional<CelestialBodyDefinition> defaultTarget = defaultMiningTarget(data);
        if (defaultTarget.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    CelestialBodyRegistry.size() == 0
                            ? "message.beyondorbit.uplink.no_bodies"
                            : "message.beyondorbit.launch_pad.no_discovered_bodies"
            ));
            return false;
        }
        OrbitalModuleTier miningTier = OrbitalModuleTier.BASIC;
        OrbitalModuleTier speedTier = null;
        OrbitalModuleTier efficiencyTier = null;
        if (!player.getAbilities().instabuild) {
            if (!consumeOne(player, BeyondOrbitContent.ROCKET_FRAME.get())) {
                player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.missing_rocket_frame"));
                return false;
            }
            OrbitalModuleTier consumedMiningTier = consumeBestModule(player, OrbitalModuleType.MINING);
            if (consumedMiningTier == null) {
                player.getInventory().add(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()));
                player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.missing_mining_module"));
                return false;
            }
            miningTier = consumedMiningTier;
            speedTier = consumeBestModule(player, OrbitalModuleType.SPEED);
            efficiencyTier = consumeBestModule(player, OrbitalModuleType.EFFICIENCY);
            satelliteStack.shrink(1);
        } else {
            miningTier = OrbitalModuleTier.ELITE;
            speedTier = OrbitalModuleTier.ELITE;
            efficiencyTier = OrbitalModuleTier.ELITE;
        }

        Optional<CelestialBodyDefinition> target = launchPadMiningTarget(data, miningTier, speedTier, efficiencyTier);
        CelestialBodyDefinition definition = target.orElse(defaultTarget.get());
        ResourceLocation satelliteId = launchPadSatelliteIdFor(level, pos);
        data.getOrCreateState(definition);
        SatelliteMiningMissionState satellite = data.getOrCreateSatellite(satelliteId);
        int rolls = launchPadRolls(miningTier, efficiencyTier);
        int intervalTicks = launchPadIntervalTicks(speedTier);
        int launchTicks = launchPadLaunchTicks(speedTier);
        int transitTicks = launchPadTransitTicks(definition, speedTier);
        satellite.startLaunchPadMining(definition.id(), rolls, intervalTicks, launchTicks, transitTicks);
        satellite.replaceEquippedModules(miningTier, speedTier, efficiencyTier);
        applyEquippedModulesToMission(satellite, Config.launchPadRollsPerExtraction, Config.launchPadTicksPerExtraction);
        data.setDirty();

        player.sendSystemMessage(Component.translatable(
                "message.beyondorbit.launch_pad.launched",
                satelliteId.toString(),
                definition.id().toString(),
                rolls,
                intervalTicks
        ));
        return true;
    }

    public static boolean launchLowOrbitSolarSatelliteFromPad(Level level, BlockPos pos, Player player, ItemStack satelliteStack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        ResourceLocation satelliteId = lowOrbitSolarSatelliteIdFor(level, pos);
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        SatelliteMiningMissionState existingSatellite = data.getSatellite(satelliteId).orElse(null);
        if (existingSatellite != null && existingSatellite.isLowOrbitSolar()) {
            player.sendSystemMessage(Component.translatable(
                    "message.beyondorbit.launch_pad.solar_already_deployed",
                    satelliteId.toString(),
                    data.lowOrbitSolarSatelliteCount(),
                    OrbitalReceiverBlockEntity.solarOutputPerActiveSatellite(existingSatellite)
            ));
            return false;
        }

        SolarPanelTier panelTier = SolarPanelTier.BASIC;
        if (!player.getAbilities().instabuild) {
            if (!hasOne(player, BeyondOrbitContent.ROCKET_FRAME.get())) {
                player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.missing_rocket_frame"));
                return false;
            }
            SolarPanelTier consumedPanelTier = consumeBestSolarPanel(player);
            if (consumedPanelTier == null) {
                player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.missing_solar_panel"));
                return false;
            }
            consumeOne(player, BeyondOrbitContent.ROCKET_FRAME.get());
            panelTier = consumedPanelTier;
            satelliteStack.shrink(1);
        }

        SatelliteMiningMissionState satellite = data.getOrCreateSatellite(satelliteId);
        satellite.markLowOrbitSolar(Config.lowOrbitSolarDeploymentTicks, panelTier, Config.lowOrbitSolarDistanceKm);
        data.setDirty();

        player.sendSystemMessage(Component.translatable(
                "message.beyondorbit.launch_pad.solar_launched",
                satelliteId.toString(),
                data.lowOrbitSolarSatelliteCount(),
                OrbitalReceiverBlockEntity.solarOutputPerActiveSatellite(satellite)
        ));
        return true;
    }

    public static boolean launchEnergyStorageSatelliteFromPad(Level level, BlockPos pos, Player player, ItemStack satelliteStack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        ResourceLocation satelliteId = energyStorageSatelliteIdFor(level, pos);
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        SatelliteMiningMissionState existingSatellite = data.getSatellite(satelliteId).orElse(null);
        if (existingSatellite != null && existingSatellite.isEnergyStorage()) {
            player.sendSystemMessage(Component.translatable(
                    "message.beyondorbit.launch_pad.energy_storage_already_deployed",
                    satelliteId.toString(),
                    data.energyStorageSatelliteCount(),
                    existingSatellite.storedEnergy(),
                    existingSatellite.energyCapacity()
            ));
            return false;
        }

        if (!player.getAbilities().instabuild) {
            if (!hasOne(player, BeyondOrbitContent.ROCKET_FRAME.get())) {
                player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.missing_rocket_frame"));
                return false;
            }
            if (!hasOne(player, BeyondOrbitContent.ORBITAL_DATA_CORE.get())) {
                player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.missing_orbital_data_core"));
                return false;
            }
            consumeOne(player, BeyondOrbitContent.ROCKET_FRAME.get());
            consumeOne(player, BeyondOrbitContent.ORBITAL_DATA_CORE.get());
            satelliteStack.shrink(1);
        }

        SatelliteMiningMissionState satellite = data.getOrCreateSatellite(satelliteId);
        satellite.markEnergyStorage(Config.orbitalEnergyStorageDeploymentTicks, Config.orbitalEnergyStorageCapacity);
        data.setDirty();

        player.sendSystemMessage(Component.translatable(
                "message.beyondorbit.launch_pad.energy_storage_launched",
                satelliteId.toString(),
                data.energyStorageSatelliteCount(),
                satellite.energyCapacity()
        ));
        return true;
    }

    public static int collectResources(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        ResourceLocation satelliteId = satelliteIdFor(level, pos);
        return collectResources(serverLevel, satelliteId, player, "message.beyondorbit.uplink.no_satellite");
    }

    public static int collectLaunchPadResources(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        ResourceLocation satelliteId = launchPadSatelliteIdFor(level, pos);
        return collectResources(serverLevel, satelliteId, player, "message.beyondorbit.launch_pad.no_satellite");
    }

    public static boolean equipUplinkModule(Level level, BlockPos pos, Player player, ItemStack moduleStack) {
        return equipModule(level, satelliteIdFor(level, pos), player, moduleStack, Config.satelliteUplinkRollsPerExtraction, Config.satelliteUplinkTicksPerExtraction, "message.beyondorbit.uplink.no_satellite");
    }

    public static boolean equipLaunchPadModule(Level level, BlockPos pos, Player player, ItemStack moduleStack) {
        return equipModule(level, launchPadSatelliteIdFor(level, pos), player, moduleStack, Config.launchPadRollsPerExtraction, Config.launchPadTicksPerExtraction, "message.beyondorbit.launch_pad.no_satellite");
    }

    private static boolean equipModule(Level level, ResourceLocation satelliteId, Player player, ItemStack moduleStack, int baseRolls, int baseIntervalTicks, String noSatelliteKey) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        if (!(moduleStack.getItem() instanceof OrbitalModuleItem moduleItem)) {
            return false;
        }

        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        SatelliteMiningMissionState satellite = data.getSatellite(satelliteId).orElse(null);
        if (satellite == null) {
            player.sendSystemMessage(Component.translatable(noSatelliteKey, satelliteId.toString()));
            return false;
        }

        OrbitalModuleTier previousTier = satellite.equipModule(moduleItem.moduleType(), moduleItem.moduleTier());
        applyEquippedModulesToMission(satellite, baseRolls, baseIntervalTicks);
        data.setDirty();

        if (!player.getAbilities().instabuild) {
            moduleStack.shrink(1);
            if (previousTier != null) {
                ItemStack previousStack = moduleStackFor(moduleItem.moduleType(), previousTier);
                if (!previousStack.isEmpty() && !player.addItem(previousStack)) {
                    player.drop(previousStack, false);
                }
            }
        }

        player.sendSystemMessage(Component.translatable(
                previousTier == null ? "message.beyondorbit.module.equipped" : "message.beyondorbit.module.replaced",
                moduleItem.moduleType().displayName(),
                moduleItem.moduleTier().displayName(),
                satelliteId.toString()
        ));
        return true;
    }

    public static boolean reportLaunchPadStatus(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        ResourceLocation satelliteId = launchPadSatelliteIdFor(level, pos);
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        SatelliteMiningMissionState satellite = data.getSatellite(satelliteId).orElse(null);
        if (satellite == null) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.no_satellite", satelliteId.toString()));
            return false;
        }

        player.sendSystemMessage(Component.translatable(
                "message.beyondorbit.launch_pad.status",
                satelliteId.toString(),
                satellite.targetBody() == null ? "<none>" : satellite.targetBody().toString(),
                satellite.completedExtractions(),
                satellite.ticksUntilNextExtraction(),
                satellite.totalExtractedView().values().stream().mapToLong(Long::longValue).sum()
        ));
        return true;
    }

    private static int collectResources(ServerLevel serverLevel, ResourceLocation satelliteId, Player player, String noSatelliteKey) {
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        SatelliteMiningMissionState satellite = data.getSatellite(satelliteId).orElse(null);
        if (satellite == null) {
            player.sendSystemMessage(Component.translatable(noSatelliteKey, satelliteId.toString()));
            return 0;
        }

        Map<ResourceLocation, Long> drained = satellite.drainTotalExtracted();
        if (drained.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.beyondorbit.uplink.status",
                    satelliteId.toString(),
                    satellite.targetBody() == null ? "<none>" : satellite.targetBody().toString(),
                    satellite.completedExtractions(),
                    satellite.ticksUntilNextExtraction()
            ));
            return 0;
        }

        int stacksGiven = 0;
        long itemsGiven = 0L;
        for (Map.Entry<ResourceLocation, Long> entry : drained.entrySet()) {
            Item item = BuiltInRegistries.ITEM.get(entry.getKey());
            if (item == Items.AIR) {
                continue;
            }
            long remaining = entry.getValue();
            int maxStackSize = Math.max(1, item.getDefaultMaxStackSize());
            while (remaining > 0L) {
                int stackSize = (int) Math.min(remaining, maxStackSize);
                ItemStack stack = new ItemStack(item, stackSize);
                if (!player.addItem(stack)) {
                    player.drop(stack, false);
                }
                stacksGiven++;
                itemsGiven += stackSize;
                remaining -= stackSize;
            }
        }

        if (stacksGiven <= 0) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.uplink.unregistered_resources", satelliteId.toString()));
            return 0;
        }

        data.setDirty();
        player.sendSystemMessage(Component.translatable("message.beyondorbit.uplink.collected", itemsGiven, satelliteId.toString(), stacksGiven));
        return stacksGiven;
    }

    public static int uplinkRolls(OrbitalModuleTier miningTier, OrbitalModuleTier efficiencyTier) {
        return moduleAdjustedRolls(Config.satelliteUplinkRollsPerExtraction, miningTier, efficiencyTier);
    }

    public static int uplinkIntervalTicks(OrbitalModuleTier speedTier) {
        return moduleAdjustedIntervalTicks(Config.satelliteUplinkTicksPerExtraction, speedTier);
    }

    public static int launchPadRolls(OrbitalModuleTier miningTier, OrbitalModuleTier efficiencyTier) {
        return moduleAdjustedRolls(Config.launchPadRollsPerExtraction, miningTier, efficiencyTier);
    }

    public static int launchPadIntervalTicks(OrbitalModuleTier speedTier) {
        return moduleAdjustedIntervalTicks(Config.launchPadTicksPerExtraction, speedTier);
    }

    public static int launchPadLaunchTicks(OrbitalModuleTier speedTier) {
        return moduleAdjustedPhaseTicks(Config.launchPadMissionPhaseTicks, speedTier);
    }

    public static int launchPadTransitTicks(CelestialBodyDefinition definition, OrbitalModuleTier speedTier) {
        long ticks = (long) Config.launchPadTransitBaseTicks
                + (long) definition.tier() * Config.launchPadTransitTicksPerTier
                + (long) definition.distance() * Config.launchPadTransitTicksPerDistance;
        if (speedTier != null) {
            int reductionPercent = Math.min(100, Math.max(0, speedTier.level() * Config.launchPadTransitSpeedReductionPercentPerLevel));
            ticks = ticks * (100L - reductionPercent) / 100L;
        }
        return clampTicks(ticks, 0, 72000);
    }

    private static void applyEquippedModulesToMission(SatelliteMiningMissionState satellite, int baseRolls, int baseIntervalTicks) {
        if (satellite.targetBody() == null || satellite.isLowOrbitSolar()) {
            return;
        }
        int rolls = moduleAdjustedRolls(
                baseRolls,
                satellite.equippedModuleTier(OrbitalModuleType.MINING),
                satellite.equippedModuleTier(OrbitalModuleType.EFFICIENCY)
        );
        int intervalTicks = moduleAdjustedIntervalTicks(baseIntervalTicks, satellite.equippedModuleTier(OrbitalModuleType.SPEED));
        satellite.updateMiningProfile(rolls, intervalTicks);
    }

    private static int moduleAdjustedRolls(int baseRolls, OrbitalModuleTier miningTier, OrbitalModuleTier efficiencyTier) {
        int rolls = baseRolls + (miningTier == null ? 0 : miningTier.miningRollBonus());
        if (efficiencyTier != null) {
            rolls = Math.max(1, rolls * efficiencyTier.efficiencyPercent() / 100);
        }
        return Math.min(rolls, Config.maxExtractionRollsPerOperation);
    }

    private static int moduleAdjustedIntervalTicks(int baseIntervalTicks, OrbitalModuleTier speedTier) {
        int intervalTicks = baseIntervalTicks;
        if (speedTier != null) {
            intervalTicks -= speedTier.speedTickReduction();
        }
        return Math.max(1, intervalTicks);
    }

    private static int moduleAdjustedPhaseTicks(int baseTicks, OrbitalModuleTier speedTier) {
        int ticks = baseTicks;
        if (speedTier != null) {
            ticks -= speedTier.speedTickReduction();
        }
        return Math.max(0, ticks);
    }

    private static int clampTicks(long ticks, int min, int max) {
        if (ticks < min) {
            return min;
        }
        if (ticks > max) {
            return max;
        }
        return (int) ticks;
    }

    private static ItemStack moduleStackFor(OrbitalModuleType type, OrbitalModuleTier tier) {
        DeferredItem<Item> item = switch (type) {
            case MINING -> switch (tier) {
                case BASIC -> BeyondOrbitContent.ORBITAL_MINING_MODULE;
                case ADVANCED -> BeyondOrbitContent.ADVANCED_MINING_MODULE;
                case ELITE -> BeyondOrbitContent.ELITE_MINING_MODULE;
            };
            case SPEED -> switch (tier) {
                case BASIC -> BeyondOrbitContent.BASIC_SPEED_MODULE;
                case ADVANCED -> BeyondOrbitContent.ADVANCED_SPEED_MODULE;
                case ELITE -> BeyondOrbitContent.ELITE_SPEED_MODULE;
            };
            case EFFICIENCY -> switch (tier) {
                case BASIC -> BeyondOrbitContent.BASIC_EFFICIENCY_MODULE;
                case ADVANCED -> BeyondOrbitContent.ADVANCED_EFFICIENCY_MODULE;
                case ELITE -> BeyondOrbitContent.ELITE_EFFICIENCY_MODULE;
            };
        };
        return new ItemStack(item.get());
    }

    public static Optional<CelestialBodyDefinition> defaultMiningTarget() {
        return CelestialBodyRegistry.all().stream()
                .min(targetOrder());
    }

    public static Optional<CelestialBodyDefinition> defaultMiningTarget(BeyondOrbitSavedData data) {
        return CelestialBodyRegistry.all().stream()
                .filter(definition -> data.isCelestialBodyDiscovered(definition.id()))
                .min(targetOrder());
    }

    public static Optional<CelestialBodyDefinition> launchPadMiningTarget(
            OrbitalModuleTier miningTier,
            OrbitalModuleTier speedTier,
            OrbitalModuleTier efficiencyTier
    ) {
        if (miningTier == OrbitalModuleTier.ELITE
                && speedTier == OrbitalModuleTier.ELITE
                && efficiencyTier == OrbitalModuleTier.ELITE) {
            return CelestialBodyRegistry.all().stream()
                    .max(targetOrder());
        }
        return defaultMiningTarget();
    }

    public static Optional<CelestialBodyDefinition> launchPadMiningTarget(
            BeyondOrbitSavedData data,
            OrbitalModuleTier miningTier,
            OrbitalModuleTier speedTier,
            OrbitalModuleTier efficiencyTier
    ) {
        if (miningTier == OrbitalModuleTier.ELITE
                && speedTier == OrbitalModuleTier.ELITE
                && efficiencyTier == OrbitalModuleTier.ELITE) {
            return CelestialBodyRegistry.all().stream()
                    .filter(definition -> data.isCelestialBodyDiscovered(definition.id()))
                    .max(targetOrder());
        }
        return defaultMiningTarget(data);
    }

    private static Comparator<CelestialBodyDefinition> targetOrder() {
        return Comparator.comparingInt(CelestialBodyDefinition::tier).thenComparing(CelestialBodyDefinition::id);
    }

    private static boolean consumeOne(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }

    private static boolean hasOne(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    private static SolarPanelTier consumeBestSolarPanel(Player player) {
        SolarPanelTier bestTier = null;
        int bestSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof SolarPanelItem panelItem) {
                if (bestTier == null || panelItem.tier().level() > bestTier.level()) {
                    bestTier = panelItem.tier();
                    bestSlot = i;
                }
            }
        }
        if (bestSlot >= 0) {
            ItemStack stack = player.getInventory().getItem(bestSlot);
            stack.shrink(1);
            player.getInventory().setChanged();
        }
        return bestTier;
    }

    private static OrbitalModuleTier consumeBestModule(Player player, OrbitalModuleType type) {
        OrbitalModuleTier bestTier = null;
        int bestSlot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof OrbitalModuleItem moduleItem && moduleItem.moduleType() == type) {
                if (bestTier == null || moduleItem.moduleTier().level() > bestTier.level()) {
                    bestTier = moduleItem.moduleTier();
                    bestSlot = i;
                }
            }
        }
        if (bestSlot >= 0) {
            ItemStack stack = player.getInventory().getItem(bestSlot);
            stack.shrink(1);
            player.getInventory().setChanged();
        }
        return bestTier;
    }

    private static String sanitizePath(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }
}
