package me.myogoo.beyondorbit.core.satellite;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.blockentity.SatelliteUplinkBlockEntity;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyRegistry;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    public static boolean deploySatellite(Level level, BlockPos pos, Player player, ItemStack satelliteStack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }

        Optional<CelestialBodyDefinition> target = defaultMiningTarget();
        if (target.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.uplink.no_bodies"));
            return false;
        }
        if (serverLevel.getBlockEntity(pos) instanceof SatelliteUplinkBlockEntity uplink && !uplink.consumeDeployEnergy(player)) {
            return false;
        }

        ResourceLocation satelliteId = satelliteIdFor(level, pos);
        CelestialBodyDefinition definition = target.get();
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        data.getOrCreateState(definition);
        SatelliteMiningMissionState satellite = data.getOrCreateSatellite(satelliteId);
        int rolls = Math.min(Config.satelliteUplinkRollsPerExtraction, Config.maxExtractionRollsPerOperation);
        int intervalTicks = Config.satelliteUplinkTicksPerExtraction;
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

        Optional<CelestialBodyDefinition> target = defaultMiningTarget();
        if (target.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.beyondorbit.uplink.no_bodies"));
            return false;
        }
        if (!player.getAbilities().instabuild) {
            if (!consumeOne(player, BeyondOrbitContent.ROCKET_FRAME.get())) {
                player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.missing_rocket_frame"));
                return false;
            }
            if (!consumeOne(player, BeyondOrbitContent.ORBITAL_MINING_MODULE.get())) {
                player.getInventory().add(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()));
                player.sendSystemMessage(Component.translatable("message.beyondorbit.launch_pad.missing_mining_module"));
                return false;
            }
            satelliteStack.shrink(1);
        }

        ResourceLocation satelliteId = launchPadSatelliteIdFor(level, pos);
        CelestialBodyDefinition definition = target.get();
        BeyondOrbitSavedData data = BeyondOrbitSavedData.get(serverLevel.getServer());
        data.getOrCreateState(definition);
        SatelliteMiningMissionState satellite = data.getOrCreateSatellite(satelliteId);
        int rolls = Math.min(Config.launchPadRollsPerExtraction, Config.maxExtractionRollsPerOperation);
        int intervalTicks = Config.launchPadTicksPerExtraction;
        satellite.startMining(definition.id(), rolls, intervalTicks);
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

    private static Optional<CelestialBodyDefinition> defaultMiningTarget() {
        return CelestialBodyRegistry.all().stream()
                .min(Comparator.comparingInt(CelestialBodyDefinition::tier).thenComparing(CelestialBodyDefinition::id));
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
