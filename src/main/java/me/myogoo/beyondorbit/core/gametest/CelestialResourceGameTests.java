package me.myogoo.beyondorbit.core.gametest;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.blockentity.OrbitalReceiverBlockEntity;
import me.myogoo.beyondorbit.core.blockentity.SatelliteUplinkBlockEntity;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyRegistry;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyState;
import me.myogoo.beyondorbit.core.celestial.CelestialResourceExtractor;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionRequest;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionResult;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.menu.OrbitalReceiverMenu;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
import me.myogoo.beyondorbit.core.satellite.SatelliteUplinkService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(BeyondOrbitCore.MODID)
@PrefixGameTestTemplate(false)
public final class CelestialResourceGameTests {
    private CelestialResourceGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void finiteAsteroidExtractionConsumesSavedResourceState(GameTestHelper helper) {
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");
        ResourceLocation ironOre = ResourceLocation.withDefaultNamespace("iron_ore");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        CelestialBodyState state = savedData.resetState(definition);

        long before = state.remaining(ironOre);
        if (before <= 0L) {
            throw new AssertionError("Expected crimson_asteroid iron reserve to start positive, got " + before);
        }

        ResourceExtractionResult result = CelestialResourceExtractor.extract(
                definition,
                state,
                new ResourceExtractionRequest(32, 1L, 1L),
                RandomSource.create(12345L)
        );
        savedData.setDirty();

        long after = state.remaining(ironOre);
        if (!result.extractedAny()) {
            throw new AssertionError("Expected extraction to produce at least one resource");
        }
        if (after >= before) {
            throw new AssertionError("Expected finite iron reserve to decrease, before=" + before + ", after=" + after);
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void satelliteMiningMissionTicksAndAccumulatesResources(GameTestHelper helper) {
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");
        ResourceLocation satelliteId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "test_satellite");
        ResourceLocation ironOre = ResourceLocation.withDefaultNamespace("iron_ore");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        CelestialBodyState bodyState = savedData.resetState(definition);
        long before = bodyState.remaining(ironOre);

        SatelliteMiningMissionState satellite = savedData.getOrCreateSatellite(satelliteId);
        satellite.startMining(bodyId, 16, 1);

        int extractions = 0;
        for (int i = 0; i < 4; i++) {
            extractions += savedData.tickSatellites(RandomSource.create(1000L + i));
        }

        long after = bodyState.remaining(ironOre);
        if (extractions <= 0) {
            throw new AssertionError("Expected satellite mining loop to execute at least one extraction");
        }
        if (satellite.completedExtractions() <= 0L) {
            throw new AssertionError("Expected satellite to record completed extraction count");
        }
        if (satellite.totalExtractedView().isEmpty()) {
            throw new AssertionError("Expected satellite to accumulate extracted resource totals");
        }
        if (after >= before) {
            throw new AssertionError("Expected satellite mining to decrease finite iron reserve, before=" + before + ", after=" + after);
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void registeredPlayableContentExists(GameTestHelper helper) {
        ResourceLocation satelliteItemId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "basic_satellite");
        ResourceLocation uplinkBlockId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "satellite_uplink");
        ResourceLocation launchPadBlockId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "launch_pad");
        ResourceLocation receiverBlockId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "orbital_receiver");

        if (!BuiltInRegistries.ITEM.getKey(BeyondOrbitContent.BASIC_SATELLITE.get()).equals(satelliteItemId)) {
            throw new AssertionError("Expected basic satellite item to be registered as " + satelliteItemId);
        }
        if (!BuiltInRegistries.BLOCK.getKey(BeyondOrbitContent.SATELLITE_UPLINK.get()).equals(uplinkBlockId)) {
            throw new AssertionError("Expected satellite uplink block to be registered as " + uplinkBlockId);
        }
        if (!BuiltInRegistries.BLOCK.getKey(BeyondOrbitContent.LAUNCH_PAD.get()).equals(launchPadBlockId)) {
            throw new AssertionError("Expected launch pad block to be registered as " + launchPadBlockId);
        }
        if (!BuiltInRegistries.BLOCK.getKey(BeyondOrbitContent.ORBITAL_RECEIVER.get()).equals(receiverBlockId)) {
            throw new AssertionError("Expected orbital receiver block to be registered as " + receiverBlockId);
        }
        if (!BeyondOrbitContent.SATELLITE_UPLINK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            throw new AssertionError("Expected satellite uplink to be tagged as mineable with a pickaxe");
        }
        if (!BeyondOrbitContent.SATELLITE_UPLINK.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL)) {
            throw new AssertionError("Expected satellite uplink to require an iron-tier tool");
        }
        if (!BeyondOrbitContent.LAUNCH_PAD.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            throw new AssertionError("Expected launch pad to be tagged as mineable with a pickaxe");
        }
        if (!BeyondOrbitContent.ORBITAL_RECEIVER.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            throw new AssertionError("Expected orbital receiver to be tagged as mineable with a pickaxe");
        }
        if (!BeyondOrbitContent.ORBITAL_RECEIVER.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL)) {
            throw new AssertionError("Expected orbital receiver to require an iron-tier tool");
        }
        if (BeyondOrbitContent.SATELLITE_UPLINK_BLOCK_ENTITY.get().getValidBlocks().isEmpty()) {
            throw new AssertionError("Expected satellite uplink block entity type to include at least one valid block");
        }
        if (BeyondOrbitContent.ORBITAL_RECEIVER_BLOCK_ENTITY.get().getValidBlocks().isEmpty()) {
            throw new AssertionError("Expected orbital receiver block entity type to include at least one valid block");
        }
        if (BeyondOrbitContent.SATELLITE_UPLINK_MENU.get() == null) {
            throw new AssertionError("Expected satellite uplink menu type to be registered");
        }
        if (BeyondOrbitContent.ORBITAL_RECEIVER_MENU.get() == null) {
            throw new AssertionError("Expected orbital receiver menu type to be registered");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void satelliteUplinkBlockDeploysMinesAndCollects(GameTestHelper helper) {
        BlockPos uplinkPos = new BlockPos(1, 2, 1);
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.resetState(definition);

        helper.setBlock(uplinkPos, BeyondOrbitContent.SATELLITE_UPLINK.get());
        SatelliteUplinkBlockEntity uplink = (SatelliteUplinkBlockEntity) helper.getBlockEntity(uplinkPos);
        for (int i = 0; i < 120; i++) {
            SatelliteUplinkBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(uplinkPos), uplink.getBlockState(), uplink);
        }
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack satelliteStack = new ItemStack(BeyondOrbitContent.BASIC_SATELLITE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, satelliteStack);

        helper.useBlock(uplinkPos, player);
        if (!satelliteStack.isEmpty()) {
            throw new AssertionError("Expected satellite uplink to consume one Basic Satellite in survival mode");
        }

        ResourceLocation satelliteId = SatelliteUplinkService.satelliteIdFor(helper.getLevel(), helper.absolutePos(uplinkPos));
        SatelliteMiningMissionState satellite = savedData.getSatellite(satelliteId)
                .orElseThrow(() -> new AssertionError("Expected uplink to create linked satellite " + satelliteId));
        if (!satellite.active()) {
            throw new AssertionError("Expected linked satellite to begin active mining mission");
        }

        // Other GameTests share the same server SavedData and may tick/reset unrelated orbital state in parallel.
        // Keep this assertion focused on the linked Uplink satellite by giving it a deterministic short mission.
        satellite.startMining(bodyId, Config.maxExtractionRollsPerOperation, 1);
        for (int i = 0; i < 20 && satellite.totalExtractedView().isEmpty(); i++) {
            satellite.tick(definition, savedData.getOrCreateState(definition), RandomSource.create(3000L + i));
        }
        if (satellite.totalExtractedView().isEmpty()) {
            throw new AssertionError("Expected uplink-linked satellite to collect resources after ticking");
        }
        Map<ResourceLocation, Long> minedBeforeCollect = Map.copyOf(satellite.totalExtractedView());

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        helper.useBlock(uplinkPos, player);
        player.setShiftKeyDown(false);
        if (!satellite.totalExtractedView().isEmpty()) {
            throw new AssertionError("Expected empty-hand uplink use to drain linked satellite buffer");
        }
        boolean collectedAnyMinedResource = minedBeforeCollect.keySet().stream()
                .map(BuiltInRegistries.ITEM::get)
                .filter(item -> item != net.minecraft.world.item.Items.AIR)
                .anyMatch(item -> player.getInventory().contains(new ItemStack(item)));
        if (!collectedAnyMinedResource) {
            throw new AssertionError("Expected collected satellite resources to reach the player's inventory");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void orbitalReceiverPullsSatelliteBufferAndStoresSolarEnergy(GameTestHelper helper) {
        BlockPos receiverPos = new BlockPos(1, 2, 1);
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");
        ResourceLocation satelliteId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "receiver_test_satellite");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.resetState(definition);
        SatelliteMiningMissionState satellite = savedData.getOrCreateSatellite(satelliteId);
        satellite.startMining(bodyId, 32, 1);
        for (int i = 0; i < 8; i++) {
            savedData.tickSatellites(RandomSource.create(4000L + i));
        }
        if (satellite.totalExtractedView().isEmpty()) {
            throw new AssertionError("Expected test satellite to have buffered mined resources before receiver tick");
        }

        helper.setBlock(receiverPos, BeyondOrbitContent.ORBITAL_RECEIVER.get());
        OrbitalReceiverBlockEntity receiver = (OrbitalReceiverBlockEntity) helper.getBlockEntity(receiverPos);
        OrbitalReceiverBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(receiverPos), receiver.getBlockState(), receiver);

        if (receiver.storedItemCount() <= 0) {
            throw new AssertionError("Expected Orbital Receiver to pull mined resources into its item buffer");
        }
        if (receiver.energyStored() <= 0) {
            throw new AssertionError("Expected Orbital Receiver to store FE from orbital solar panels");
        }

        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        OrbitalReceiverMenu menu = new OrbitalReceiverMenu(0, player.getInventory(), receiver);
        if (menu.energy() != receiver.energyStored()) {
            throw new AssertionError("Expected receiver menu energy to mirror block entity energy");
        }
        if (menu.storedItemCount() != receiver.storedItemCount()) {
            throw new AssertionError("Expected receiver menu item count to mirror block entity item storage");
        }
        if (menu.occupiedSlots() != receiver.occupiedSlots()) {
            throw new AssertionError("Expected receiver menu occupied slots to mirror block entity item storage");
        }
        if (menu.slotCount() != OrbitalReceiverBlockEntity.SLOT_COUNT) {
            throw new AssertionError("Expected receiver menu to expose receiver slot capacity");
        }

        if (!satellite.totalExtractedView().isEmpty() && receiver.storedItemCount() >= Config.orbitalReceiverMaxItemsPerTick) {
            // Partial pulls are allowed when the per-tick budget is reached.
            helper.succeed();
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void launchPadConsumesPayloadAndStartsMission(GameTestHelper helper) {
        BlockPos padPos = new BlockPos(1, 2, 1);
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.resetState(definition);

        helper.setBlock(padPos, BeyondOrbitContent.LAUNCH_PAD.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack satelliteStack = new ItemStack(BeyondOrbitContent.BASIC_SATELLITE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, satelliteStack);
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ORBITAL_MINING_MODULE.get()));

        helper.useBlock(padPos, player);
        if (!satelliteStack.isEmpty()) {
            throw new AssertionError("Expected launch pad to consume one Basic Satellite");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()))) {
            throw new AssertionError("Expected launch pad to consume one Rocket Frame");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ORBITAL_MINING_MODULE.get()))) {
            throw new AssertionError("Expected launch pad to consume one Orbital Mining Module");
        }

        ResourceLocation satelliteId = SatelliteUplinkService.launchPadSatelliteIdFor(helper.getLevel(), helper.absolutePos(padPos));
        SatelliteMiningMissionState satellite = savedData.getSatellite(satelliteId)
                .orElseThrow(() -> new AssertionError("Expected launch pad to create satellite " + satelliteId));
        if (!satellite.active()) {
            throw new AssertionError("Expected launch pad satellite to start an active mission");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void beyondOrbitCommandSurfaceExecutes(GameTestHelper helper) {
        net.minecraft.commands.CommandSourceStack source = helper.getLevel().getServer()
                .createCommandSourceStack()
                .withPermission(4)
                .withSuppressedOutput();

        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit status");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit config");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit planets");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit celestial detail beyondorbit:crimson_asteroid");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit celestial resources beyondorbit:crimson_asteroid");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit satellite startMining beyondorbit:command_test beyondorbit:crimson_asteroid 4 2");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit satellites");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit satellite status beyondorbit:command_test");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit satellite tick 4");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit satellite clearBuffer beyondorbit:command_test");
        helper.getLevel().getServer().getCommands().performPrefixedCommand(source, "beyondorbit satellite stop beyondorbit:command_test");

        helper.succeed();
    }
}
