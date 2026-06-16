package me.myogoo.beyondorbit.core;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = BeyondOrbitCore.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue INFINITE_CELESTIAL_RESOURCES;
    private static final ModConfigSpec.BooleanValue FORCE_FINITE_RESOURCES;
    private static final ModConfigSpec.BooleanValue FORCE_INFINITE_RESOURCES;
    private static final ModConfigSpec.DoubleValue RESOURCE_AMOUNT_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue RESOURCE_YIELD_MULTIPLIER;
    private static final ModConfigSpec.IntValue MAX_EXTRACTION_ROLLS_PER_OPERATION;
    private static final ModConfigSpec.IntValue SATELLITE_UPLINK_ROLLS_PER_EXTRACTION;
    private static final ModConfigSpec.IntValue SATELLITE_UPLINK_TICKS_PER_EXTRACTION;
    private static final ModConfigSpec.IntValue SATELLITE_UPLINK_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue SATELLITE_UPLINK_DEPLOY_ENERGY_COST;
    private static final ModConfigSpec.IntValue SATELLITE_UPLINK_ENERGY_GENERATED_PER_TICK;
    private static final ModConfigSpec.IntValue LAUNCH_PAD_ROLLS_PER_EXTRACTION;
    private static final ModConfigSpec.IntValue LAUNCH_PAD_TICKS_PER_EXTRACTION;
    private static final ModConfigSpec.IntValue LAUNCH_PAD_MISSION_PHASE_TICKS;
    private static final ModConfigSpec.IntValue LAUNCH_PAD_TRANSIT_BASE_TICKS;
    private static final ModConfigSpec.IntValue LAUNCH_PAD_TRANSIT_TICKS_PER_TIER;
    private static final ModConfigSpec.IntValue LAUNCH_PAD_TRANSIT_TICKS_PER_DISTANCE;
    private static final ModConfigSpec.IntValue LAUNCH_PAD_TRANSIT_SPEED_REDUCTION_PERCENT_PER_LEVEL;
    private static final ModConfigSpec.IntValue LOW_ORBIT_SOLAR_DEPLOYMENT_TICKS;
    private static final ModConfigSpec.IntValue LOW_ORBIT_SOLAR_DISTANCE_KM;
    private static final ModConfigSpec.IntValue ORBITAL_ENERGY_STORAGE_DEPLOYMENT_TICKS;
    private static final ModConfigSpec.IntValue ORBITAL_ENERGY_STORAGE_CAPACITY;
    private static final ModConfigSpec.IntValue ORBITAL_ENERGY_STORAGE_TRANSFER_FE_PER_TICK;
    private static final ModConfigSpec.IntValue BLACK_HOLE_POWER_DEPLOYMENT_TICKS;
    private static final ModConfigSpec.IntValue BLACK_HOLE_POWER_DISTANCE_KM;
    private static final ModConfigSpec.IntValue BLACK_HOLE_POWER_FE_PER_TICK;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_SOLAR_FE_PER_TICK;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_TRANSMISSION_LOSS_PERCENT_PER_1000_KM;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_BASIC_SOLAR_PANEL_OUTPUT_PERCENT;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_ADVANCED_SOLAR_PANEL_OUTPUT_PERCENT;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_ELITE_SOLAR_PANEL_OUTPUT_PERCENT;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_TRANSFER_FE_PER_TICK;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_MAX_ITEMS_PER_TICK;

    static {
        BUILDER.push("resources");

        INFINITE_CELESTIAL_RESOURCES = BUILDER
                .comment("Default resource mode for celestial resources whose datapack definition uses config_default.")
                .define("infiniteCelestialResources", false);

        FORCE_FINITE_RESOURCES = BUILDER
                .comment("Forces every celestial resource to deplete, ignoring datapack infinite modes.")
                .define("forceFiniteResources", false);

        FORCE_INFINITE_RESOURCES = BUILDER
                .comment("Forces every celestial resource to behave as infinite, ignoring datapack finite modes.")
                .define("forceInfiniteResources", false);

        RESOURCE_AMOUNT_MULTIPLIER = BUILDER
                .comment("Multiplier applied when initial finite reserves are created for a celestial body.")
                .defineInRange("resourceAmountMultiplier", 1.0D, 0.0D, 1_000_000.0D);

        RESOURCE_YIELD_MULTIPLIER = BUILDER
                .comment("Global multiplier for extracted resource yields. Fractional values are represented by extraction requests later; this is the server-wide default.")
                .defineInRange("resourceYieldMultiplier", 1.0D, 0.0D, 1_000_000.0D);

        MAX_EXTRACTION_ROLLS_PER_OPERATION = BUILDER
                .comment("Safety cap for how many weighted resource rolls one extraction operation may request.")
                .defineInRange("maxExtractionRollsPerOperation", 64, 1, 4096);

        BUILDER.pop();

        BUILDER.push("satellites");

        SATELLITE_UPLINK_ROLLS_PER_EXTRACTION = BUILDER
                .comment("How many weighted resource rolls a Basic Satellite performs each time a Satellite Uplink mission extracts resources.")
                .defineInRange("uplinkRollsPerExtraction", 16, 1, 4096);

        SATELLITE_UPLINK_TICKS_PER_EXTRACTION = BUILDER
                .comment("Server ticks between automatic extraction operations for satellites deployed from a Satellite Uplink.")
                .defineInRange("uplinkTicksPerExtraction", 200, 1, 72000);

        SATELLITE_UPLINK_ENERGY_CAPACITY = BUILDER
                .comment("Internal energy capacity for each Satellite Uplink. This is a simple first-pass energy buffer, not yet a full FE capability bridge.")
                .defineInRange("uplinkEnergyCapacity", 10000, 0, 1_000_000_000);

        SATELLITE_UPLINK_DEPLOY_ENERGY_COST = BUILDER
                .comment("Energy consumed when a Satellite Uplink deploys a Basic Satellite.")
                .defineInRange("uplinkDeployEnergyCost", 1000, 0, 1_000_000_000);

        SATELLITE_UPLINK_ENERGY_GENERATED_PER_TICK = BUILDER
                .comment("Passive energy generated per server tick by a Satellite Uplink for the first playable prototype.")
                .defineInRange("uplinkEnergyGeneratedPerTick", 10, 0, 1_000_000);

        LAUNCH_PAD_ROLLS_PER_EXTRACTION = BUILDER
                .comment("How many weighted resource rolls a launch-pad mission performs each extraction cycle.")
                .defineInRange("launchPadRollsPerExtraction", 32, 1, 4096);

        LAUNCH_PAD_TICKS_PER_EXTRACTION = BUILDER
                .comment("Server ticks between extraction operations for satellites launched from a Launch Pad.")
                .defineInRange("launchPadTicksPerExtraction", 160, 1, 72000);

        LAUNCH_PAD_MISSION_PHASE_TICKS = BUILDER
                .comment("Server ticks a Launch Pad mining satellite spends launching before entering transit.")
                .defineInRange("launchPadMissionPhaseTicks", 40, 0, 72000);

        LAUNCH_PAD_TRANSIT_BASE_TICKS = BUILDER
                .comment("Base server ticks a Launch Pad mining satellite spends in transit after launch.")
                .defineInRange("launchPadTransitBaseTicks", 20, 0, 72000);

        LAUNCH_PAD_TRANSIT_TICKS_PER_TIER = BUILDER
                .comment("Additional transit ticks per target celestial body tier.")
                .defineInRange("launchPadTransitTicksPerTier", 10, 0, 72000);

        LAUNCH_PAD_TRANSIT_TICKS_PER_DISTANCE = BUILDER
                .comment("Additional transit ticks per target celestial body distance value.")
                .defineInRange("launchPadTransitTicksPerDistance", 10, 0, 72000);

        LAUNCH_PAD_TRANSIT_SPEED_REDUCTION_PERCENT_PER_LEVEL = BUILDER
                .comment("Percent transit-time reduction per equipped Speed module level.")
                .defineInRange("launchPadTransitSpeedReductionPercentPerLevel", 10, 0, 100);

        LOW_ORBIT_SOLAR_DEPLOYMENT_TICKS = BUILDER
                .comment("Server ticks a Low Orbit Solar Satellite spends deploying before it powers Orbital Receivers.")
                .defineInRange("lowOrbitSolarDeploymentTicks", 40, 0, 72000);

        LOW_ORBIT_SOLAR_DISTANCE_KM = BUILDER
                .comment("Nominal low-orbit solar transmission distance in kilometers. Used to calculate receiver transmission loss.")
                .defineInRange("lowOrbitSolarDistanceKm", 2000, 0, 1_000_000);

        ORBITAL_ENERGY_STORAGE_DEPLOYMENT_TICKS = BUILDER
                .comment("Server ticks an Orbital Energy Storage Satellite spends deploying before it can buffer orbital FE.")
                .defineInRange("orbitalEnergyStorageDeploymentTicks", 60, 0, 72000);

        ORBITAL_ENERGY_STORAGE_CAPACITY = BUILDER
                .comment("FE capacity for one active Orbital Energy Storage Satellite.")
                .defineInRange("orbitalEnergyStorageCapacity", 1_000_000, 0, 1_000_000_000);

        ORBITAL_ENERGY_STORAGE_TRANSFER_FE_PER_TICK = BUILDER
                .comment("Maximum FE per tick each Orbital Energy Storage Satellite can accept from generators or provide to receivers.")
                .defineInRange("orbitalEnergyStorageTransferFePerTick", 4096, 0, 1_000_000_000);

        BLACK_HOLE_POWER_DEPLOYMENT_TICKS = BUILDER
                .comment("Server ticks a Black Hole Power Satellite spends stabilizing after transit before it powers Orbital Receivers.")
                .defineInRange("blackHolePowerDeploymentTicks", 80, 0, 72000);

        BLACK_HOLE_POWER_DISTANCE_KM = BUILDER
                .comment("Nominal black-hole power transmission distance in kilometers. Used to calculate receiver transmission loss.")
                .defineInRange("blackHolePowerDistanceKm", 6000, 0, 1_000_000);

        BLACK_HOLE_POWER_FE_PER_TICK = BUILDER
                .comment("Base FE per tick generated by one active Black Hole Power Satellite before distance/loss.")
                .defineInRange("blackHolePowerFePerTick", 1000, 0, 1_000_000_000);

        ORBITAL_RECEIVER_ENERGY_CAPACITY = BUILDER
                .comment("Internal FE buffer capacity for each Orbital Receiver.")
                .defineInRange("orbitalReceiverEnergyCapacity", 100000, 0, 1_000_000_000);

        ORBITAL_RECEIVER_SOLAR_FE_PER_TICK = BUILDER
                .comment("Base FE per tick generated by one active orbital solar satellite before panel tier and distance/loss multipliers.")
                .defineInRange("orbitalReceiverSolarFePerTick", 100, 0, 1_000_000_000);

        ORBITAL_RECEIVER_TRANSMISSION_LOSS_PERCENT_PER_1000_KM = BUILDER
                .comment("Percent FE lost per 1000 km between an orbital solar transmitter and an Orbital Receiver.")
                .defineInRange("orbitalReceiverTransmissionLossPercentPer1000Km", 10, 0, 100);

        ORBITAL_RECEIVER_BASIC_SOLAR_PANEL_OUTPUT_PERCENT = BUILDER
                .comment("Basic Solar Panel output multiplier as percent of orbitalReceiverSolarFePerTick.")
                .defineInRange("orbitalReceiverBasicSolarPanelOutputPercent", 100, 0, 10000);

        ORBITAL_RECEIVER_ADVANCED_SOLAR_PANEL_OUTPUT_PERCENT = BUILDER
                .comment("Advanced Solar Panel output multiplier as percent of orbitalReceiverSolarFePerTick.")
                .defineInRange("orbitalReceiverAdvancedSolarPanelOutputPercent", 150, 0, 10000);

        ORBITAL_RECEIVER_ELITE_SOLAR_PANEL_OUTPUT_PERCENT = BUILDER
                .comment("Elite Solar Panel output multiplier as percent of orbitalReceiverSolarFePerTick.")
                .defineInRange("orbitalReceiverEliteSolarPanelOutputPercent", 250, 0, 10000);

        ORBITAL_RECEIVER_TRANSFER_FE_PER_TICK = BUILDER
                .comment("Maximum FE per tick an Orbital Receiver pushes to adjacent energy consumers.")
                .defineInRange("orbitalReceiverTransferFePerTick", 1024, 0, 1_000_000_000);

        ORBITAL_RECEIVER_MAX_ITEMS_PER_TICK = BUILDER
                .comment("Maximum satellite-mined items an Orbital Receiver pulls from orbital buffers per server tick.")
                .defineInRange("orbitalReceiverMaxItemsPerTick", 64, 1, 4096);

        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean infiniteCelestialResources;
    public static boolean forceFiniteResources;
    public static boolean forceInfiniteResources;
    public static double resourceAmountMultiplier;
    public static double resourceYieldMultiplier;
    public static int maxExtractionRollsPerOperation;
    public static int satelliteUplinkRollsPerExtraction;
    public static int satelliteUplinkTicksPerExtraction;
    public static int satelliteUplinkEnergyCapacity;
    public static int satelliteUplinkDeployEnergyCost;
    public static int satelliteUplinkEnergyGeneratedPerTick;
    public static int launchPadRollsPerExtraction;
    public static int launchPadTicksPerExtraction;
    public static int launchPadMissionPhaseTicks;
    public static int launchPadTransitBaseTicks;
    public static int launchPadTransitTicksPerTier;
    public static int launchPadTransitTicksPerDistance;
    public static int launchPadTransitSpeedReductionPercentPerLevel;
    public static int lowOrbitSolarDeploymentTicks;
    public static int lowOrbitSolarDistanceKm;
    public static int orbitalEnergyStorageDeploymentTicks;
    public static int orbitalEnergyStorageCapacity;
    public static int orbitalEnergyStorageTransferFePerTick;
    public static int blackHolePowerDeploymentTicks;
    public static int blackHolePowerDistanceKm;
    public static int blackHolePowerFePerTick;
    public static int orbitalReceiverEnergyCapacity;
    public static int orbitalReceiverSolarFePerTick;
    public static int orbitalReceiverTransmissionLossPercentPer1000Km;
    public static int orbitalReceiverBasicSolarPanelOutputPercent;
    public static int orbitalReceiverAdvancedSolarPanelOutputPercent;
    public static int orbitalReceiverEliteSolarPanelOutputPercent;
    public static int orbitalReceiverTransferFePerTick;
    public static int orbitalReceiverMaxItemsPerTick;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        infiniteCelestialResources = INFINITE_CELESTIAL_RESOURCES.get();
        forceFiniteResources = FORCE_FINITE_RESOURCES.get();
        forceInfiniteResources = FORCE_INFINITE_RESOURCES.get();
        resourceAmountMultiplier = RESOURCE_AMOUNT_MULTIPLIER.get();
        resourceYieldMultiplier = RESOURCE_YIELD_MULTIPLIER.get();
        maxExtractionRollsPerOperation = MAX_EXTRACTION_ROLLS_PER_OPERATION.get();
        satelliteUplinkRollsPerExtraction = SATELLITE_UPLINK_ROLLS_PER_EXTRACTION.get();
        satelliteUplinkTicksPerExtraction = SATELLITE_UPLINK_TICKS_PER_EXTRACTION.get();
        satelliteUplinkEnergyCapacity = SATELLITE_UPLINK_ENERGY_CAPACITY.get();
        satelliteUplinkDeployEnergyCost = SATELLITE_UPLINK_DEPLOY_ENERGY_COST.get();
        satelliteUplinkEnergyGeneratedPerTick = SATELLITE_UPLINK_ENERGY_GENERATED_PER_TICK.get();
        launchPadRollsPerExtraction = LAUNCH_PAD_ROLLS_PER_EXTRACTION.get();
        launchPadTicksPerExtraction = LAUNCH_PAD_TICKS_PER_EXTRACTION.get();
        launchPadMissionPhaseTicks = LAUNCH_PAD_MISSION_PHASE_TICKS.get();
        launchPadTransitBaseTicks = LAUNCH_PAD_TRANSIT_BASE_TICKS.get();
        launchPadTransitTicksPerTier = LAUNCH_PAD_TRANSIT_TICKS_PER_TIER.get();
        launchPadTransitTicksPerDistance = LAUNCH_PAD_TRANSIT_TICKS_PER_DISTANCE.get();
        launchPadTransitSpeedReductionPercentPerLevel = LAUNCH_PAD_TRANSIT_SPEED_REDUCTION_PERCENT_PER_LEVEL.get();
        lowOrbitSolarDeploymentTicks = LOW_ORBIT_SOLAR_DEPLOYMENT_TICKS.get();
        lowOrbitSolarDistanceKm = LOW_ORBIT_SOLAR_DISTANCE_KM.get();
        orbitalEnergyStorageDeploymentTicks = ORBITAL_ENERGY_STORAGE_DEPLOYMENT_TICKS.get();
        orbitalEnergyStorageCapacity = ORBITAL_ENERGY_STORAGE_CAPACITY.get();
        orbitalEnergyStorageTransferFePerTick = ORBITAL_ENERGY_STORAGE_TRANSFER_FE_PER_TICK.get();
        blackHolePowerDeploymentTicks = BLACK_HOLE_POWER_DEPLOYMENT_TICKS.get();
        blackHolePowerDistanceKm = BLACK_HOLE_POWER_DISTANCE_KM.get();
        blackHolePowerFePerTick = BLACK_HOLE_POWER_FE_PER_TICK.get();
        orbitalReceiverEnergyCapacity = ORBITAL_RECEIVER_ENERGY_CAPACITY.get();
        orbitalReceiverSolarFePerTick = ORBITAL_RECEIVER_SOLAR_FE_PER_TICK.get();
        orbitalReceiverTransmissionLossPercentPer1000Km = ORBITAL_RECEIVER_TRANSMISSION_LOSS_PERCENT_PER_1000_KM.get();
        orbitalReceiverBasicSolarPanelOutputPercent = ORBITAL_RECEIVER_BASIC_SOLAR_PANEL_OUTPUT_PERCENT.get();
        orbitalReceiverAdvancedSolarPanelOutputPercent = ORBITAL_RECEIVER_ADVANCED_SOLAR_PANEL_OUTPUT_PERCENT.get();
        orbitalReceiverEliteSolarPanelOutputPercent = ORBITAL_RECEIVER_ELITE_SOLAR_PANEL_OUTPUT_PERCENT.get();
        orbitalReceiverTransferFePerTick = ORBITAL_RECEIVER_TRANSFER_FE_PER_TICK.get();
        orbitalReceiverMaxItemsPerTick = ORBITAL_RECEIVER_MAX_ITEMS_PER_TICK.get();

        if (forceFiniteResources && forceInfiniteResources) {
            throw new IllegalStateException("BeyondOrbit config cannot force both finite and infinite celestial resources.");
        }
    }
}
