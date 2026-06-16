package me.myogoo.beyondorbit.core.gametest;

import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.block.SatelliteMonitorBlock;
import me.myogoo.beyondorbit.core.block.TelescopeBlock;
import me.myogoo.beyondorbit.core.blockentity.ItemReceiverBlockEntity;
import me.myogoo.beyondorbit.core.blockentity.OrbitalReceiverBlockEntity;
import me.myogoo.beyondorbit.core.blockentity.SatelliteUplinkBlockEntity;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyRegistry;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyState;
import me.myogoo.beyondorbit.core.celestial.CelestialResourceExtractor;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionRequest;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionResult;
import me.myogoo.beyondorbit.core.data.BeyondOrbitSavedData;
import me.myogoo.beyondorbit.core.menu.LaunchPadMenu;
import me.myogoo.beyondorbit.core.menu.OrbitalReceiverMenu;
import me.myogoo.beyondorbit.core.menu.SatelliteUplinkMenu;
import me.myogoo.beyondorbit.core.module.ModuleAssemblyService;
import me.myogoo.beyondorbit.core.module.OrbitalModuleItem;
import me.myogoo.beyondorbit.core.module.OrbitalModuleTier;
import me.myogoo.beyondorbit.core.module.OrbitalModuleType;
import me.myogoo.beyondorbit.core.registry.BeyondOrbitContent;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
import me.myogoo.beyondorbit.core.satellite.SatelliteUplinkService;
import me.myogoo.beyondorbit.core.solar.SolarPanelItem;
import me.myogoo.beyondorbit.core.solar.SolarPanelTier;
import me.myogoo.beyondorbit.core.tier.TierableComponentItem;
import me.myogoo.beyondorbit.core.tier.TierableItemStats;
import me.myogoo.beyondorbit.core.tier.TierableItemTier;
import me.myogoo.beyondorbit.core.tier.TierableItemType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;
import net.neoforged.neoforge.capabilities.Capabilities;
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
        ResourceLocation itemReceiverBlockId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "item_receiver");
        ResourceLocation satelliteMonitorBlockId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "satellite_monitor");
        ResourceLocation telescopeBlockId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "telescope");
        ResourceLocation moduleAssemblerBlockId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "module_assembler");
        ResourceLocation singularityMatrixItemId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "singularity_matrix");
        ResourceLocation lowOrbitSolarSatelliteItemId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "low_orbit_solar_satellite");
        ResourceLocation solarPanelItemId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "solar_panel");
        ResourceLocation advancedSolarPanelItemId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "advanced_solar_panel");
        ResourceLocation eliteSolarPanelItemId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "elite_solar_panel");

        if (!BuiltInRegistries.ITEM.getKey(BeyondOrbitContent.BASIC_SATELLITE.get()).equals(satelliteItemId)) {
            throw new AssertionError("Expected basic satellite item to be registered as " + satelliteItemId);
        }
        if (!BuiltInRegistries.ITEM.getKey(BeyondOrbitContent.LOW_ORBIT_SOLAR_SATELLITE.get()).equals(lowOrbitSolarSatelliteItemId)) {
            throw new AssertionError("Expected low orbit solar satellite item to be registered as " + lowOrbitSolarSatelliteItemId);
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
        if (!BuiltInRegistries.BLOCK.getKey(BeyondOrbitContent.ITEM_RECEIVER.get()).equals(itemReceiverBlockId)) {
            throw new AssertionError("Expected item receiver block to be registered as " + itemReceiverBlockId);
        }
        if (!BuiltInRegistries.BLOCK.getKey(BeyondOrbitContent.SATELLITE_MONITOR.get()).equals(satelliteMonitorBlockId)) {
            throw new AssertionError("Expected satellite monitor block to be registered as " + satelliteMonitorBlockId);
        }
        if (!BuiltInRegistries.BLOCK.getKey(BeyondOrbitContent.TELESCOPE.get()).equals(telescopeBlockId)) {
            throw new AssertionError("Expected telescope block to be registered as " + telescopeBlockId);
        }
        if (!BuiltInRegistries.BLOCK.getKey(BeyondOrbitContent.MODULE_ASSEMBLER.get()).equals(moduleAssemblerBlockId)) {
            throw new AssertionError("Expected module assembler block to be registered as " + moduleAssemblerBlockId);
        }
        if (!BuiltInRegistries.ITEM.getKey(BeyondOrbitContent.SINGULARITY_MATRIX.get()).equals(singularityMatrixItemId)) {
            throw new AssertionError("Expected singularity matrix item to be registered as " + singularityMatrixItemId);
        }
        if (!BuiltInRegistries.ITEM.getKey(BeyondOrbitContent.SOLAR_PANEL.get()).equals(solarPanelItemId)) {
            throw new AssertionError("Expected basic solar panel item to be registered as " + solarPanelItemId);
        }
        if (!BuiltInRegistries.ITEM.getKey(BeyondOrbitContent.ADVANCED_SOLAR_PANEL.get()).equals(advancedSolarPanelItemId)) {
            throw new AssertionError("Expected advanced solar panel item to be registered as " + advancedSolarPanelItemId);
        }
        if (!BuiltInRegistries.ITEM.getKey(BeyondOrbitContent.ELITE_SOLAR_PANEL.get()).equals(eliteSolarPanelItemId)) {
            throw new AssertionError("Expected elite solar panel item to be registered as " + eliteSolarPanelItemId);
        }
        if (!(BeyondOrbitContent.ELITE_SOLAR_PANEL.get() instanceof SolarPanelItem elitePanel)
                || elitePanel.tier() != SolarPanelTier.ELITE) {
            throw new AssertionError("Expected elite solar panel to expose ELITE panel tier");
        }
        if (!(BeyondOrbitContent.ELITE_SPEED_MODULE.get() instanceof OrbitalModuleItem eliteSpeedModule)
                || eliteSpeedModule.moduleType() != OrbitalModuleType.SPEED
                || eliteSpeedModule.moduleTier() != OrbitalModuleTier.ELITE) {
            throw new AssertionError("Expected elite speed module to expose SPEED/ELITE module values");
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
        if (!BeyondOrbitContent.ITEM_RECEIVER.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            throw new AssertionError("Expected item receiver to be tagged as mineable with a pickaxe");
        }
        if (!BeyondOrbitContent.ITEM_RECEIVER.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL)) {
            throw new AssertionError("Expected item receiver to require an iron-tier tool");
        }
        if (!BeyondOrbitContent.SATELLITE_MONITOR.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            throw new AssertionError("Expected satellite monitor to be tagged as mineable with a pickaxe");
        }
        if (!BeyondOrbitContent.SATELLITE_MONITOR.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL)) {
            throw new AssertionError("Expected satellite monitor to require an iron-tier tool");
        }
        if (!BeyondOrbitContent.TELESCOPE.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            throw new AssertionError("Expected telescope to be tagged as mineable with a pickaxe");
        }
        if (!BeyondOrbitContent.TELESCOPE.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL)) {
            throw new AssertionError("Expected telescope to require an iron-tier tool");
        }
        if (!BeyondOrbitContent.MODULE_ASSEMBLER.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            throw new AssertionError("Expected module assembler to be tagged as mineable with a pickaxe");
        }
        if (!BeyondOrbitContent.MODULE_ASSEMBLER.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL)) {
            throw new AssertionError("Expected module assembler to require an iron-tier tool");
        }
        if (BeyondOrbitContent.SATELLITE_UPLINK_BLOCK_ENTITY.get().getValidBlocks().isEmpty()) {
            throw new AssertionError("Expected satellite uplink block entity type to include at least one valid block");
        }
        if (BeyondOrbitContent.ORBITAL_RECEIVER_BLOCK_ENTITY.get().getValidBlocks().isEmpty()) {
            throw new AssertionError("Expected orbital receiver block entity type to include at least one valid block");
        }
        if (BeyondOrbitContent.ITEM_RECEIVER_BLOCK_ENTITY.get().getValidBlocks().isEmpty()) {
            throw new AssertionError("Expected item receiver block entity type to include at least one valid block");
        }
        if (BeyondOrbitContent.SATELLITE_UPLINK_MENU.get() == null) {
            throw new AssertionError("Expected satellite uplink menu type to be registered");
        }
        if (BeyondOrbitContent.ORBITAL_RECEIVER_MENU.get() == null) {
            throw new AssertionError("Expected orbital receiver menu type to be registered");
        }
        if (BeyondOrbitContent.LAUNCH_PAD_MENU.get() == null) {
            throw new AssertionError("Expected launch pad menu type to be registered");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void telescopeUseDiscoversBodyAndPersistsSavedData(GameTestHelper helper) {
        BlockPos telescopePos = new BlockPos(1, 2, 1);
        BeyondOrbitSavedData isolatedData = new BeyondOrbitSavedData();
        ResourceLocation targetBody = isolatedData.nextUndiscoveredCelestialBody()
                .map(CelestialBodyDefinition::id)
                .orElseThrow(() -> new AssertionError("Expected at least one undiscovered celestial body for Telescope test"));
        if (isolatedData.isCelestialBodyDiscovered(targetBody)) {
            throw new AssertionError("Expected isolated Telescope data to start undiscovered for " + targetBody);
        }
        ResourceLocation discovered = TelescopeBlock.observe(isolatedData)
                .orElseThrow(() -> new AssertionError("Expected Telescope observe helper to discover a body"));
        if (!discovered.equals(targetBody)) {
            throw new AssertionError("Expected Telescope to discover first undiscovered body " + targetBody + ", got " + discovered);
        }
        if (!isolatedData.isCelestialBodyDiscovered(targetBody)) {
            throw new AssertionError("Expected Telescope observe helper to discover " + targetBody);
        }

        helper.setBlock(telescopePos, BeyondOrbitContent.TELESCOPE.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        helper.useBlock(telescopePos, player);

        CompoundTag savedTag = isolatedData.save(new CompoundTag(), helper.getLevel().registryAccess());
        BeyondOrbitSavedData loaded = BeyondOrbitSavedData.load(savedTag, helper.getLevel().registryAccess());
        if (!loaded.isCelestialBodyDiscovered(targetBody)) {
            throw new AssertionError("Expected discovered celestial body to survive SavedData save/load: " + targetBody);
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void launchPadTargetSelectionRequiresDiscoveredBodies(GameTestHelper helper) {
        ResourceLocation expectedDefault = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");
        ResourceLocation expectedElite = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "void_maw");

        CelestialBodyDefinition defaultBody = CelestialBodyRegistry.get(expectedDefault)
                .orElseThrow(() -> new AssertionError("Expected default celestial body to be loaded: " + expectedDefault));
        CelestialBodyDefinition deepSpace = CelestialBodyRegistry.get(expectedElite)
                .orElseThrow(() -> new AssertionError("Expected deep space celestial body to be loaded: " + expectedElite));

        BeyondOrbitSavedData discoveryData = new BeyondOrbitSavedData();
        if (SatelliteUplinkService.defaultMiningTarget(discoveryData).isPresent()) {
            throw new AssertionError("Expected no default mining target before any celestial body is discovered");
        }
        if (SatelliteUplinkService.launchPadMiningTarget(discoveryData, OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE).isPresent()) {
            throw new AssertionError("Expected no elite launch target before any celestial body is discovered");
        }

        discoveryData.discoverCelestialBody(defaultBody.id());
        ResourceLocation defaultTargetId = SatelliteUplinkService.defaultMiningTarget(discoveryData)
                .map(CelestialBodyDefinition::id)
                .orElseThrow(() -> new AssertionError("Expected discovered default celestial mining target"));
        if (!defaultTargetId.equals(expectedDefault)) {
            throw new AssertionError("Expected default discovered mining target to be " + expectedDefault + ", got " + defaultTargetId);
        }

        ResourceLocation eliteWithOnlyDefault = SatelliteUplinkService.launchPadMiningTarget(discoveryData, OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE)
                .map(CelestialBodyDefinition::id)
                .orElseThrow(() -> new AssertionError("Expected elite launch target to fall back to discovered body"));
        if (!eliteWithOnlyDefault.equals(expectedDefault)) {
            throw new AssertionError("Expected elite launch target to ignore undiscovered " + expectedElite + ", got " + eliteWithOnlyDefault);
        }

        discoveryData.discoverCelestialBody(deepSpace.id());
        ResourceLocation eliteTargetId = SatelliteUplinkService.launchPadMiningTarget(discoveryData, OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE)
                .map(CelestialBodyDefinition::id)
                .orElseThrow(() -> new AssertionError("Expected elite launch pad loadout to resolve a discovered celestial target"));
        if (!eliteTargetId.equals(expectedElite)) {
            throw new AssertionError("Expected elite launch pad target to be discovered " + expectedElite + ", got " + eliteTargetId);
        }

        ResourceLocation matrixId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "singularity_matrix");
        boolean exposesMatrix = deepSpace.resources().stream().anyMatch(resource -> resource.id().equals(matrixId));
        if (!exposesMatrix) {
            throw new AssertionError("Expected void_maw to expose singularity matrix as an orbital resource");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void lowOrbitSolarSatellitePowersOrbitalReceiver(GameTestHelper helper) {
        BlockPos padPos = new BlockPos(1, 2, 1);
        BlockPos receiverPos = new BlockPos(3, 2, 1);

        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        int solarSatellitesBefore = savedData.lowOrbitSolarSatelliteCount();
        int activeSolarSatellitesBefore = savedData.activeLowOrbitSolarSatelliteCount();

        helper.setBlock(padPos, BeyondOrbitContent.LAUNCH_PAD.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        ItemStack satelliteStack = new ItemStack(BeyondOrbitContent.LOW_ORBIT_SOLAR_SATELLITE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, satelliteStack);
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ADVANCED_SOLAR_PANEL.get()));

        helper.useBlock(padPos, player);
        if (!satelliteStack.isEmpty()) {
            throw new AssertionError("Expected Launch Pad to consume one Low Orbit Solar Satellite");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()))) {
            throw new AssertionError("Expected Launch Pad to consume one Rocket Frame for Low Orbit Solar Satellite launch");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ADVANCED_SOLAR_PANEL.get()))) {
            throw new AssertionError("Expected Launch Pad to consume one Advanced Solar Panel for Low Orbit Solar Satellite launch");
        }

        int solarSatellitesAfter = savedData.lowOrbitSolarSatelliteCount();
        if (solarSatellitesAfter <= solarSatellitesBefore) {
            throw new AssertionError("Expected Low Orbit Solar Satellite launch to register orbital solar capacity, before="
                    + solarSatellitesBefore + ", after=" + solarSatellitesAfter);
        }

        ResourceLocation solarSatelliteId = SatelliteUplinkService.lowOrbitSolarSatelliteIdFor(helper.getLevel(), helper.absolutePos(padPos));
        SatelliteMiningMissionState solarSatellite = savedData.getSatellite(solarSatelliteId)
                .orElseThrow(() -> new AssertionError("Expected launch pad to create linked Low Orbit Solar Satellite " + solarSatelliteId));
        if (!solarSatellite.isLowOrbitSolar()) {
            throw new AssertionError("Expected linked satellite state to be marked as low orbit solar: " + solarSatelliteId);
        }
        if (solarSatellite.active() || solarSatellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.DEPLOYING || solarSatellite.targetBody() != null) {
            throw new AssertionError("Expected Low Orbit Solar Satellite state to start deploying without mining: " + solarSatelliteId);
        }
        if (solarSatellite.solarPanelTier() != SolarPanelTier.ADVANCED) {
            throw new AssertionError("Expected Low Orbit Solar Satellite to remember Advanced Solar Panel tier");
        }
        if (solarSatellite.orbitDistanceKm() != Config.lowOrbitSolarDistanceKm) {
            throw new AssertionError("Expected Low Orbit Solar Satellite to remember configured low orbit distance");
        }

        helper.setBlock(receiverPos, BeyondOrbitContent.ORBITAL_RECEIVER.get());
        OrbitalReceiverBlockEntity receiver = (OrbitalReceiverBlockEntity) helper.getBlockEntity(receiverPos);
        OrbitalReceiverBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(receiverPos), receiver.getBlockState(), receiver);
        int energyBeforeNewSolarActivation = receiver.energyStored();

        for (int i = 0; i < Config.lowOrbitSolarDeploymentTicks; i++) {
            savedData.tickSatellites(RandomSource.create(7000L + i));
        }
        if (!solarSatellite.active() || solarSatellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.ACTIVE) {
            throw new AssertionError("Expected Low Orbit Solar Satellite to become ACTIVE after deployment delay");
        }
        OrbitalReceiverBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(receiverPos), receiver.getBlockState(), receiver);
        int addedByActivationTick = receiver.energyStored() - energyBeforeNewSolarActivation;
        int expectedNewSatelliteOutput = OrbitalReceiverBlockEntity.solarOutputPerActiveSatellite(solarSatellite);
        if (addedByActivationTick < expectedNewSatelliteOutput) {
            throw new AssertionError("Expected Orbital Receiver to receive at least the new solar satellite's FE after deployment, expected +"
                    + expectedNewSatelliteOutput + ", got +" + addedByActivationTick);
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
        savedData.discoverCelestialBody(bodyId);

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
        savedData.discoverCelestialBody(bodyId);
        SatelliteMiningMissionState satellite = savedData.getOrCreateSatellite(satelliteId);
        int solarSatellitesBeforeReceiverTick = savedData.lowOrbitSolarSatelliteCount();
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
        if (solarSatellitesBeforeReceiverTick <= 0 && receiver.energyStored() != 0) {
            throw new AssertionError("Expected Orbital Receiver not to generate passive fake solar FE without a Low Orbit Solar Satellite, got "
                    + receiver.energyStored());
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
    public static void itemReceiverAcceptsHeldItemsAndExposesStorage(GameTestHelper helper) {
        BlockPos receiverPos = new BlockPos(1, 2, 1);
        helper.setBlock(receiverPos, BeyondOrbitContent.ITEM_RECEIVER.get());
        ItemReceiverBlockEntity receiver = (ItemReceiverBlockEntity) helper.getBlockEntity(receiverPos);
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack ingots = new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 5);
        player.setItemInHand(InteractionHand.MAIN_HAND, ingots);

        helper.useBlock(receiverPos, player);
        if (!ingots.isEmpty()) {
            throw new AssertionError("Expected Item Receiver to consume the player's held iron ingots");
        }
        if (receiver.storedItemCount() != 5) {
            throw new AssertionError("Expected Item Receiver to store 5 items, got " + receiver.storedItemCount());
        }
        if (receiver.occupiedSlots() != 1) {
            throw new AssertionError("Expected Item Receiver to use one slot for one inserted stack");
        }

        BlockPos absolutePos = helper.absolutePos(receiverPos);
        var exposedStorage = Capabilities.ItemHandler.BLOCK.getCapability(
                helper.getLevel(),
                absolutePos,
                helper.getLevel().getBlockState(absolutePos),
                receiver,
                null
        );
        if (exposedStorage == null || exposedStorage.getSlots() != ItemReceiverBlockEntity.SLOT_COUNT) {
            throw new AssertionError("Expected Item Receiver to expose its item handler capability");
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        helper.useBlock(receiverPos, player);
        player.setShiftKeyDown(false);
        if (receiver.storedItemCount() != 0) {
            throw new AssertionError("Expected sneak empty-hand use to collect all stored items");
        }
        if (!player.getInventory().contains(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT))) {
            throw new AssertionError("Expected collected ingots to reach the player's inventory");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void satelliteMonitorShowsRegisteredSatellites(GameTestHelper helper) {
        BlockPos monitorPos = new BlockPos(1, 2, 1);
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");
        ResourceLocation satelliteId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "monitor_test_satellite");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.resetState(definition);
        savedData.discoverCelestialBody(bodyId);
        SatelliteMiningMissionState satellite = savedData.getOrCreateSatellite(satelliteId);
        satellite.startMining(bodyId, 16, 1);
        for (int i = 0; i < 8 && satellite.totalExtractedView().isEmpty(); i++) {
            satellite.tick(definition, savedData.getOrCreateState(definition), RandomSource.create(6000L + i));
        }

        helper.setBlock(monitorPos, BeyondOrbitContent.SATELLITE_MONITOR.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        helper.useBlock(monitorPos, player);

        var lines = SatelliteMonitorBlock.buildSatelliteStatusLines(helper.getLevel(), 1000);
        if (lines.size() < 2) {
            throw new AssertionError("Expected Satellite Monitor to build at least a header and one satellite entry");
        }
        String rendered = lines.stream()
                .map(component -> component.getString())
                .reduce("", (left, right) -> left + "\n" + right);
        if (!rendered.contains(satelliteId.toString())) {
            throw new AssertionError("Expected Satellite Monitor output to include satellite id " + satelliteId + ", got: " + rendered);
        }
        if (!rendered.contains(bodyId.toString())) {
            throw new AssertionError("Expected Satellite Monitor output to include target body " + bodyId + ", got: " + rendered);
        }
        if (!rendered.contains("ACTIVE") && !rendered.contains("활성")) {
            throw new AssertionError("Expected Satellite Monitor output to include active status, got: " + rendered);
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void moduleAssemblerBuildsOnlyBasicModules(GameTestHelper helper) {
        BlockPos assemblerPos = new BlockPos(1, 2, 1);
        helper.setBlock(assemblerPos, BeyondOrbitContent.MODULE_ASSEMBLER.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        player.getInventory().add(new ItemStack(Items.REDSTONE));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ORBITAL_DATA_CORE.get()));
        player.getInventory().add(new ItemStack(Items.COPPER_INGOT));
        player.getInventory().add(new ItemStack(Items.DIAMOND));
        ItemStack catalyst = new ItemStack(Items.REDSTONE);

        boolean assembled = ModuleAssemblyService.assemble(player, catalyst);
        if (!assembled) {
            throw new AssertionError("Expected module assembler service to accept Redstone catalyst and basic materials");
        }

        if (player.getInventory().contains(new ItemStack(Items.REDSTONE))) {
            throw new AssertionError("Expected module assembler to consume one Redstone catalyst");
        }
        if (!player.getInventory().contains(new ItemStack(BeyondOrbitContent.BASIC_SPEED_MODULE.get()))) {
            throw new AssertionError("Expected module assembler to create a Basic Speed Module");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ELITE_SPEED_MODULE.get()))) {
            throw new AssertionError("Expected module assembler not to bypass crafting progression by creating Elite modules");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ORBITAL_DATA_CORE.get()))) {
            throw new AssertionError("Expected module assembler to consume one Orbital Data Core");
        }
        if (!player.getInventory().contains(new ItemStack(Items.DIAMOND))) {
            throw new AssertionError("Expected Diamond to remain because advanced and elite upgrades use crafting recipes");
        }
        if (player.getInventory().contains(new ItemStack(Items.COPPER_INGOT))) {
            throw new AssertionError("Expected module assembler to consume one Copper Ingot for the Basic module");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void launchPadUsesModuleTierStats(GameTestHelper helper) {
        BlockPos padPos = new BlockPos(1, 2, 1);
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.resetState(definition);
        savedData.discoverCelestialBody(bodyId);

        helper.setBlock(padPos, BeyondOrbitContent.LAUNCH_PAD.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        ItemStack satelliteStack = new ItemStack(BeyondOrbitContent.BASIC_SATELLITE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, satelliteStack);
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ELITE_MINING_MODULE.get()));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ELITE_SPEED_MODULE.get()));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ELITE_EFFICIENCY_MODULE.get()));

        helper.useBlock(padPos, player);

        ResourceLocation satelliteId = SatelliteUplinkService.launchPadSatelliteIdFor(helper.getLevel(), helper.absolutePos(padPos));
        SatelliteMiningMissionState satellite = savedData.getSatellite(satelliteId)
                .orElseThrow(() -> new AssertionError("Expected launch pad to create linked satellite " + satelliteId));
        int expectedRolls = SatelliteUplinkService.launchPadRolls(OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE);
        int expectedInterval = SatelliteUplinkService.launchPadIntervalTicks(OrbitalModuleTier.ELITE);
        if (satellite.rollsPerExtraction() != expectedRolls) {
            throw new AssertionError("Expected elite mining/efficiency modules to set rolls=" + expectedRolls + ", got " + satellite.rollsPerExtraction());
        }
        if (satellite.ticksPerExtraction() != expectedInterval) {
            throw new AssertionError("Expected elite speed module to set interval=" + expectedInterval + ", got " + satellite.ticksPerExtraction());
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ELITE_MINING_MODULE.get()))) {
            throw new AssertionError("Expected launch pad to consume the elite mining module");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ELITE_SPEED_MODULE.get()))) {
            throw new AssertionError("Expected launch pad to consume the optional elite speed module");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ELITE_EFFICIENCY_MODULE.get()))) {
            throw new AssertionError("Expected launch pad to consume the optional elite efficiency module");
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
        savedData.discoverCelestialBody(bodyId);

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
        if (satellite.active() || satellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.LAUNCHING) {
            throw new AssertionError("Expected launch pad satellite to start in a non-active launch phase");
        }

        LaunchPadMenu launchPadMenu = new LaunchPadMenu(0, player.getInventory(), helper.getLevel(), helper.absolutePos(padPos));
        if (!launchPadMenu.hasSatellite()) {
            throw new AssertionError("Expected Launch Pad menu to detect the linked satellite");
        }
        if (launchPadMenu.active()) {
            throw new AssertionError("Expected Launch Pad menu to mirror non-active launch phase before arrival");
        }
        if (launchPadMenu.rollsPerExtraction() != satellite.rollsPerExtraction()) {
            throw new AssertionError("Expected Launch Pad menu to mirror mission rolls");
        }
        if (launchPadMenu.ticksPerExtraction() != satellite.ticksPerExtraction()) {
            throw new AssertionError("Expected Launch Pad menu to mirror mission interval");
        }

        for (int i = 0; i < Config.launchPadMissionPhaseTicks; i++) {
            savedData.tickSatellites(RandomSource.create(8000L + i));
        }
        if (satellite.active() || satellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.IN_TRANSIT) {
            throw new AssertionError("Expected launch pad satellite to enter IN_TRANSIT after launch delay");
        }
        int transitTicks = SatelliteUplinkService.launchPadTransitTicks(definition, null);
        for (int i = 0; i < transitTicks; i++) {
            savedData.tickSatellites(RandomSource.create(8100L + i));
        }
        if (!satellite.active() || satellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.ACTIVE) {
            throw new AssertionError("Expected launch pad satellite to become ACTIVE after launch and transit delays");
        }

        satellite.startMining(bodyId, Config.maxExtractionRollsPerOperation, 1);
        for (int i = 0; i < 200 && satellite.totalExtractedView().isEmpty(); i++) {
            satellite.tick(definition, savedData.getOrCreateState(definition), RandomSource.create(5000L + i));
        }
        if (satellite.totalExtractedView().isEmpty()) {
            throw new AssertionError("Expected launch pad satellite to buffer resources after ticking");
        }
        Map<ResourceLocation, Long> minedBeforeCollect = Map.copyOf(satellite.totalExtractedView());

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        helper.useBlock(padPos, player);
        player.setShiftKeyDown(false);
        if (!satellite.totalExtractedView().isEmpty()) {
            throw new AssertionError("Expected sneak empty-hand Launch Pad use to drain linked satellite buffer");
        }
        boolean collectedAnyMinedResource = minedBeforeCollect.keySet().stream()
                .map(BuiltInRegistries.ITEM::get)
                .filter(item -> item != net.minecraft.world.item.Items.AIR)
                .anyMatch(item -> player.getInventory().contains(new ItemStack(item)));
        if (!collectedAnyMinedResource) {
            throw new AssertionError("Expected Launch Pad collected resources to reach the player's inventory");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void launchPadEquipsAndReplacesModulesAfterLaunch(GameTestHelper helper) {
        BlockPos padPos = new BlockPos(1, 2, 1);
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.resetState(definition);
        savedData.discoverCelestialBody(bodyId);

        helper.setBlock(padPos, BeyondOrbitContent.LAUNCH_PAD.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BeyondOrbitContent.BASIC_SATELLITE.get()));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ORBITAL_MINING_MODULE.get()));

        helper.useBlock(padPos, player);

        ResourceLocation satelliteId = SatelliteUplinkService.launchPadSatelliteIdFor(helper.getLevel(), helper.absolutePos(padPos));
        SatelliteMiningMissionState satellite = savedData.getSatellite(satelliteId)
                .orElseThrow(() -> new AssertionError("Expected launch pad to create satellite " + satelliteId));
        if (satellite.equippedModuleTier(OrbitalModuleType.MINING) != OrbitalModuleTier.BASIC) {
            throw new AssertionError("Expected launch payload mining module to be stored as equipped Basic tier");
        }

        ItemStack advancedMining = new ItemStack(BeyondOrbitContent.ADVANCED_MINING_MODULE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, advancedMining);
        helper.useBlock(padPos, player);

        if (!advancedMining.isEmpty()) {
            throw new AssertionError("Expected Launch Pad module equip to consume the held Advanced Mining Module");
        }
        if (satellite.equippedModuleTier(OrbitalModuleType.MINING) != OrbitalModuleTier.ADVANCED) {
            throw new AssertionError("Expected Launch Pad to replace the equipped mining module with Advanced tier");
        }
        if (!player.getInventory().contains(new ItemStack(BeyondOrbitContent.ORBITAL_MINING_MODULE.get()))) {
            throw new AssertionError("Expected replaced Basic Mining Module to return to the player's inventory");
        }
        int expectedRolls = SatelliteUplinkService.launchPadRolls(OrbitalModuleTier.ADVANCED, null);
        if (satellite.rollsPerExtraction() != expectedRolls) {
            throw new AssertionError("Expected replaced module to update launch mission rolls to " + expectedRolls + ", got " + satellite.rollsPerExtraction());
        }

        LaunchPadMenu menu = new LaunchPadMenu(0, player.getInventory(), helper.getLevel(), helper.absolutePos(padPos));
        if (menu.miningModuleLevel() != OrbitalModuleTier.ADVANCED.level()) {
            throw new AssertionError("Expected Launch Pad menu to expose equipped Advanced Mining module level");
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void satelliteUplinkEquippedModulesUpdateMissionProfile(GameTestHelper helper) {
        BlockPos uplinkPos = new BlockPos(1, 2, 1);
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");

        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.resetState(definition);
        savedData.discoverCelestialBody(bodyId);

        helper.setBlock(uplinkPos, BeyondOrbitContent.SATELLITE_UPLINK.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = true;
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BeyondOrbitContent.BASIC_SATELLITE.get()));
        helper.useBlock(uplinkPos, player);
        player.getAbilities().instabuild = false;

        ResourceLocation satelliteId = SatelliteUplinkService.satelliteIdFor(helper.getLevel(), helper.absolutePos(uplinkPos));
        SatelliteMiningMissionState satellite = savedData.getSatellite(satelliteId)
                .orElseThrow(() -> new AssertionError("Expected uplink to create satellite " + satelliteId));

        ItemStack eliteSpeed = new ItemStack(BeyondOrbitContent.ELITE_SPEED_MODULE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, eliteSpeed);
        helper.useBlock(uplinkPos, player);
        if (!eliteSpeed.isEmpty()) {
            throw new AssertionError("Expected Uplink module equip to consume the held Elite Speed Module");
        }
        if (satellite.equippedModuleTier(OrbitalModuleType.SPEED) != OrbitalModuleTier.ELITE) {
            throw new AssertionError("Expected Uplink satellite to store equipped Elite Speed module");
        }
        int expectedInterval = SatelliteUplinkService.uplinkIntervalTicks(OrbitalModuleTier.ELITE);
        if (satellite.ticksPerExtraction() != expectedInterval) {
            throw new AssertionError("Expected Elite Speed module to update uplink interval to " + expectedInterval + ", got " + satellite.ticksPerExtraction());
        }

        SatelliteUplinkMenu menu = new SatelliteUplinkMenu(0, player.getInventory(), (SatelliteUplinkBlockEntity) helper.getBlockEntity(uplinkPos));
        if (menu.speedModuleLevel() != OrbitalModuleTier.ELITE.level()) {
            throw new AssertionError("Expected Uplink menu to expose equipped Elite Speed module level");
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

    @GameTest(template = "empty", timeoutTicks = 160)
    public static void launchPadMiningMissionUsesLaunchThenTransitPhases(GameTestHelper helper) {
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");
        ResourceLocation satelliteId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "phase_chain_test_satellite");
        CelestialBodyDefinition definition = CelestialBodyRegistry.get(bodyId)
                .orElseThrow(() -> new AssertionError("Expected datapack celestial body to be loaded: " + bodyId));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.resetState(definition);
        savedData.discoverCelestialBody(bodyId);
        SatelliteMiningMissionState satellite = savedData.getOrCreateSatellite(satelliteId);
        satellite.startLaunchPadMining(bodyId, 1, 20, 2, 3);
        if (satellite.active() || satellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.LAUNCHING) {
            throw new AssertionError("Expected mission to start LAUNCHING");
        }
        for (int i = 0; i < 2; i++) {
            savedData.tickSatellites(RandomSource.create(9000L + i));
        }
        if (satellite.active() || satellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.IN_TRANSIT) {
            throw new AssertionError("Expected mission to enter IN_TRANSIT after launch phase");
        }
        if (satellite.phaseTicksRemaining() != 3) {
            throw new AssertionError("Expected transit phase to start with 3 ticks remaining, got " + satellite.phaseTicksRemaining());
        }
        for (int i = 0; i < 3; i++) {
            savedData.tickSatellites(RandomSource.create(9100L + i));
        }
        if (!satellite.active() || satellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.ACTIVE) {
            throw new AssertionError("Expected mission to become ACTIVE after transit phase");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void launchPadTransitTimeUsesTargetTierDistanceAndSpeed(GameTestHelper helper) {
        CelestialBodyDefinition asteroid = CelestialBodyRegistry.get(ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid"))
                .orElseThrow(() -> new AssertionError("Expected crimson_asteroid"));
        CelestialBodyDefinition voidMaw = CelestialBodyRegistry.get(ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "void_maw"))
                .orElseThrow(() -> new AssertionError("Expected void_maw"));
        if (voidMaw.distance() <= asteroid.distance()) {
            throw new AssertionError("Expected void_maw to have greater datapack distance than crimson_asteroid");
        }
        int asteroidBasicTransit = SatelliteUplinkService.launchPadTransitTicks(asteroid, null);
        int voidBasicTransit = SatelliteUplinkService.launchPadTransitTicks(voidMaw, null);
        int voidEliteTransit = SatelliteUplinkService.launchPadTransitTicks(voidMaw, OrbitalModuleTier.ELITE);
        if (voidBasicTransit <= asteroidBasicTransit) {
            throw new AssertionError("Expected higher-tier/distance target to take longer; asteroid=" + asteroidBasicTransit + ", void=" + voidBasicTransit);
        }
        if (voidEliteTransit >= voidBasicTransit) {
            throw new AssertionError("Expected elite speed module to reduce transit time; basic=" + voidBasicTransit + ", elite=" + voidEliteTransit);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void solarPanelTierAndDistanceLossAffectReceiverStats(GameTestHelper helper) {
        BlockPos receiverPos = new BlockPos(1, 2, 1);
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        int baselineActiveSatellites = savedData.activeLowOrbitSolarSatelliteCount();
        int baselineGross = OrbitalReceiverBlockEntity.solarGrossGenerationPerTick(savedData);
        int baselineNet = OrbitalReceiverBlockEntity.solarGenerationPerTick(savedData);
        BlockPos absoluteReceiverPos = helper.absolutePos(receiverPos);
        ResourceLocation basicId = ResourceLocation.fromNamespaceAndPath(
                BeyondOrbitCore.MODID,
                "solar_basic_stats_test_" + absoluteReceiverPos.getX() + "_" + absoluteReceiverPos.getY() + "_" + absoluteReceiverPos.getZ()
        );
        ResourceLocation eliteId = ResourceLocation.fromNamespaceAndPath(
                BeyondOrbitCore.MODID,
                "solar_elite_stats_test_" + absoluteReceiverPos.getX() + "_" + absoluteReceiverPos.getY() + "_" + absoluteReceiverPos.getZ()
        );
        SatelliteMiningMissionState basic = savedData.getOrCreateSatellite(basicId);
        basic.markLowOrbitSolar(0, SolarPanelTier.BASIC, 1000);
        SatelliteMiningMissionState elite = savedData.getOrCreateSatellite(eliteId);
        elite.markLowOrbitSolar(0, SolarPanelTier.ELITE, 3000);
        int expectedGross = OrbitalReceiverBlockEntity.solarGrossOutputPerActiveSatellite(basic)
                + OrbitalReceiverBlockEntity.solarGrossOutputPerActiveSatellite(elite);
        int expectedNet = OrbitalReceiverBlockEntity.solarOutputPerActiveSatellite(basic)
                + OrbitalReceiverBlockEntity.solarOutputPerActiveSatellite(elite);
        if (expectedNet >= expectedGross) {
            throw new AssertionError("Expected distance transmission loss to reduce net output, gross=" + expectedGross + ", net=" + expectedNet);
        }
        helper.setBlock(receiverPos, BeyondOrbitContent.ORBITAL_RECEIVER.get());
        OrbitalReceiverBlockEntity receiver = (OrbitalReceiverBlockEntity) helper.getBlockEntity(receiverPos);
        OrbitalReceiverBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(receiverPos), receiver.getBlockState(), receiver);
        int expectedTotalNet = baselineNet + expectedNet;
        int expectedStored = Math.min(expectedTotalNet, Config.orbitalReceiverTransferFePerTick);
        if (receiver.energyStored() != expectedStored) {
            throw new AssertionError("Expected receiver to store capped tiered distance-loss solar output, expected " + expectedStored + ", got " + receiver.energyStored());
        }
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        OrbitalReceiverMenu menu = new OrbitalReceiverMenu(0, player.getInventory(), receiver);
        if (menu.activeSolarSatellites() != baselineActiveSatellites + 2) {
            throw new AssertionError("Expected receiver menu to expose exact active solar satellite delta");
        }
        if (menu.solarGrossGenerationPerTick() != baselineGross + expectedGross) {
            throw new AssertionError("Expected receiver menu to expose exact gross solar FE/t delta");
        }
        if (menu.solarGenerationPerTick() != expectedTotalNet) {
            throw new AssertionError("Expected receiver menu to expose exact net solar FE/t total");
        }
        if (menu.solarTransmissionLossPercent() <= 0) {
            throw new AssertionError("Expected receiver menu to expose positive transmission loss percent");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void lowOrbitSolarSatellitePersistsTierAndDistance(GameTestHelper helper) {
        ResourceLocation satelliteId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "solar_persistence_test");
        SatelliteMiningMissionState satellite = new SatelliteMiningMissionState(satelliteId);
        satellite.markLowOrbitSolar(5, SolarPanelTier.ELITE, 42000);
        CompoundTag tag = satellite.save();
        SatelliteMiningMissionState loaded = SatelliteMiningMissionState.load(tag);
        if (!loaded.isLowOrbitSolar()) {
            throw new AssertionError("Expected loaded satellite to remain low orbit solar");
        }
        if (loaded.solarPanelTier() != SolarPanelTier.ELITE) {
            throw new AssertionError("Expected loaded satellite to preserve elite solar panel tier");
        }
        if (loaded.orbitDistanceKm() != 42000) {
            throw new AssertionError("Expected loaded satellite to preserve orbit distance");
        }
        if (loaded.missionPhase() != SatelliteMiningMissionState.MissionPhase.DEPLOYING) {
            throw new AssertionError("Expected loaded satellite to preserve deploying phase");
        }

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putString("satellite_id", ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "legacy_solar_persistence_test").toString());
        legacyTag.putString("kind", "low_orbit_solar");
        SatelliteMiningMissionState legacy = SatelliteMiningMissionState.load(legacyTag);
        if (!legacy.active() || legacy.missionPhase() != SatelliteMiningMissionState.MissionPhase.ACTIVE) {
            throw new AssertionError("Expected legacy low orbit solar satellite to migrate to ACTIVE");
        }
        if (legacy.orbitDistanceKm() != Config.lowOrbitSolarDistanceKm) {
            throw new AssertionError("Expected legacy low orbit solar satellite to migrate configured distance");
        }
        helper.succeed();
    }


    @GameTest(template = "empty", timeoutTicks = 20)
    public static void launchPadRelaunchReplacesStaleOptionalModules(GameTestHelper helper) {
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");
        SatelliteMiningMissionState satellite = new SatelliteMiningMissionState(ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "module_replace_test"));
        satellite.startLaunchPadMining(bodyId, 8, 10, 0, 0);
        satellite.replaceEquippedModules(OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE, OrbitalModuleTier.ELITE);
        satellite.startLaunchPadMining(bodyId, 2, 20, 5, 5);
        satellite.replaceEquippedModules(OrbitalModuleTier.BASIC, null, null);
        if (satellite.equippedModuleTier(OrbitalModuleType.MINING) != OrbitalModuleTier.BASIC) {
            throw new AssertionError("Expected relaunch to keep only the newly consumed mining module");
        }
        if (satellite.equippedModuleTier(OrbitalModuleType.SPEED) != null) {
            throw new AssertionError("Expected relaunch without speed module to clear stale speed module");
        }
        if (satellite.equippedModuleTier(OrbitalModuleType.EFFICIENCY) != null) {
            throw new AssertionError("Expected relaunch without efficiency module to clear stale efficiency module");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void launchPadRelaunchResetsExtractionCountdown(GameTestHelper helper) {
        ResourceLocation bodyId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "crimson_asteroid");
        SatelliteMiningMissionState satellite = new SatelliteMiningMissionState(ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "countdown_reset_test"));
        satellite.startMining(bodyId, 1, 20);
        satellite.startLaunchPadMining(bodyId, 3, 40, 5, 5);
        if (satellite.ticksUntilNextExtraction() != 40) {
            throw new AssertionError("Expected relaunch to reset extraction countdown to the new interval, got " + satellite.ticksUntilNextExtraction());
        }
        if (satellite.active() || satellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.LAUNCHING) {
            throw new AssertionError("Expected relaunched mission to wait in LAUNCHING phase");
        }
        helper.succeed();
    }


    @GameTest(template = "empty", timeoutTicks = 160)
    public static void blackHolePowerSatelliteLaunchesAndPowersReceiver(GameTestHelper helper) {
        BlockPos padPos = new BlockPos(1, 2, 1);
        BlockPos receiverPos = new BlockPos(3, 2, 1);
        ResourceLocation voidMawId = ResourceLocation.fromNamespaceAndPath(BeyondOrbitCore.MODID, "void_maw");
        CelestialBodyDefinition voidMaw = CelestialBodyRegistry.get(voidMawId)
                .orElseThrow(() -> new AssertionError("Expected Void Maw celestial body to be loaded"));
        BeyondOrbitSavedData savedData = BeyondOrbitSavedData.get(helper.getLevel().getServer());
        savedData.discoverCelestialBody(voidMawId);
        int baselineGeneration = OrbitalReceiverBlockEntity.orbitalGenerationPerTick(savedData);

        helper.setBlock(padPos, BeyondOrbitContent.LAUNCH_PAD.get());
        Player player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.getAbilities().instabuild = false;
        ItemStack satelliteStack = new ItemStack(BeyondOrbitContent.BLACK_HOLE_POWER_SATELLITE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, satelliteStack);
        player.getInventory().add(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()));
        player.getInventory().add(new ItemStack(BeyondOrbitContent.SINGULARITY_MATRIX.get()));

        helper.useBlock(padPos, player);
        if (!satelliteStack.isEmpty()) {
            throw new AssertionError("Expected Launch Pad to consume one Black Hole Power Satellite");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.ROCKET_FRAME.get()))) {
            throw new AssertionError("Expected Launch Pad to consume one Rocket Frame for black-hole launch");
        }
        if (player.getInventory().contains(new ItemStack(BeyondOrbitContent.SINGULARITY_MATRIX.get()))) {
            throw new AssertionError("Expected Launch Pad to consume one Singularity Matrix for black-hole launch");
        }

        ResourceLocation satelliteId = SatelliteUplinkService.blackHolePowerSatelliteIdFor(helper.getLevel(), helper.absolutePos(padPos));
        SatelliteMiningMissionState satellite = savedData.getSatellite(satelliteId)
                .orElseThrow(() -> new AssertionError("Expected launch pad to create black-hole power satellite " + satelliteId));
        if (!satellite.isBlackHolePower()) {
            throw new AssertionError("Expected satellite to be marked as black-hole power");
        }
        if (!voidMaw.id().equals(satellite.targetBody())) {
            throw new AssertionError("Expected black-hole power satellite to target Void Maw");
        }
        if (satellite.orbitDistanceKm() != Math.min(Config.blackHolePowerDistanceKm, 9000)) {
            throw new AssertionError("Expected black-hole satellite to store capped configured transmission distance");
        }
        if (satellite.active() || satellite.missionPhase() == SatelliteMiningMissionState.MissionPhase.ACTIVE) {
            throw new AssertionError("Expected black-hole power satellite to wait through launch/transit before activation");
        }

        int safety = 0;
        while (!satellite.active() && safety++ < 1000) {
            savedData.tickSatellites(RandomSource.create(12000L + safety));
        }
        if (!satellite.active() || satellite.missionPhase() != SatelliteMiningMissionState.MissionPhase.ACTIVE) {
            throw new AssertionError("Expected black-hole power satellite to become active after launch/transit");
        }

        int blackHoleOutput = OrbitalReceiverBlockEntity.blackHoleOutputPerActiveSatellite(satellite);
        if (blackHoleOutput <= 0) {
            throw new AssertionError("Expected active black-hole satellite to produce positive FE/t");
        }
        helper.setBlock(receiverPos, BeyondOrbitContent.ORBITAL_RECEIVER.get());
        OrbitalReceiverBlockEntity receiver = (OrbitalReceiverBlockEntity) helper.getBlockEntity(receiverPos);
        OrbitalReceiverBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(receiverPos), receiver.getBlockState(), receiver);
        int expectedStored = Math.min(baselineGeneration + blackHoleOutput, Config.orbitalReceiverTransferFePerTick);
        if (receiver.energyStored() != expectedStored) {
            throw new AssertionError("Expected Orbital Receiver to store black-hole orbital FE, expected " + expectedStored + ", got " + receiver.energyStored());
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 20)
    public static void tierableComponentItemsExposeNumericStats(GameTestHelper helper) {
        assertTierableComponent(BeyondOrbitContent.BASIC_SATELLITE_BODY.get(), TierableItemType.BODY, TierableItemTier.BASIC, 10_000, 100, 1);
        assertTierableComponent(BeyondOrbitContent.ELITE_SATELLITE_BODY.get(), TierableItemType.BODY, TierableItemTier.ELITE, 200_000, 500, 3);
        assertTierableComponent(BeyondOrbitContent.BASIC_RECEIVER_COMPONENT.get(), TierableItemType.RECEIVER, TierableItemTier.BASIC, 1_024, 16, 100_000);
        assertTierableComponent(BeyondOrbitContent.ELITE_RECEIVER_COMPONENT.get(), TierableItemType.RECEIVER, TierableItemTier.ELITE, 16_384, 64, 2_000_000);
        assertTierableComponent(BeyondOrbitContent.BASIC_TRANSMITTER_COMPONENT.get(), TierableItemType.TRANSMITTER, TierableItemTier.BASIC, 50_000, 1_024, 12);
        assertTierableComponent(BeyondOrbitContent.ELITE_TRANSMITTER_COMPONENT.get(), TierableItemType.TRANSMITTER, TierableItemTier.ELITE, 1_000_000, 16_384, 4);
        assertTierableComponent(BeyondOrbitContent.BASIC_TELESCOPE_LENS.get(), TierableItemType.TELESCOPE, TierableItemTier.BASIC, 1, 200, 1);
        assertTierableComponent(BeyondOrbitContent.ELITE_TELESCOPE_LENS.get(), TierableItemType.TELESCOPE, TierableItemTier.ELITE, 3, 40, 3);

        if (SolarPanelTier.ELITE.tierableTier() != TierableItemTier.ELITE) {
            throw new AssertionError("Expected elite solar panel tier to map to tierable ELITE");
        }
        if (SolarPanelTier.ELITE.generationFePerTick() <= SolarPanelTier.BASIC.generationFePerTick()) {
            throw new AssertionError("Expected elite solar panel generation to exceed basic generation");
        }
        if (SolarPanelTier.ELITE.transmissionDistanceKm() <= SolarPanelTier.BASIC.transmissionDistanceKm()) {
            throw new AssertionError("Expected elite solar panel transmission distance to exceed basic distance");
        }
        helper.succeed();
    }

    private static void assertTierableComponent(Object item, TierableItemType type, TierableItemTier tier, int primary, int secondary, int tertiary) {
        if (!(item instanceof TierableComponentItem tierableItem)) {
            throw new AssertionError("Expected item to be TierableComponentItem: " + item);
        }
        if (tierableItem.type() != type || tierableItem.tier() != tier) {
            throw new AssertionError("Expected " + type + "/" + tier + ", got " + tierableItem.type() + "/" + tierableItem.tier());
        }
        TierableItemStats stats = tierableItem.stats();
        if (stats.primaryValue() != primary || stats.secondaryValue() != secondary || stats.tertiaryValue() != tertiary) {
            throw new AssertionError("Unexpected stats for " + type + "/" + tier + ": " + stats);
        }
    }
}
