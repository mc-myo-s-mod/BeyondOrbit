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
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_ENERGY_CAPACITY;
    private static final ModConfigSpec.IntValue ORBITAL_RECEIVER_SOLAR_FE_PER_TICK;
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

        ORBITAL_RECEIVER_ENERGY_CAPACITY = BUILDER
                .comment("Internal FE buffer capacity for each Orbital Receiver.")
                .defineInRange("orbitalReceiverEnergyCapacity", 100000, 0, 1_000_000_000);

        ORBITAL_RECEIVER_SOLAR_FE_PER_TICK = BUILDER
                .comment("FE per tick received from orbital solar panels by each Orbital Receiver.")
                .defineInRange("orbitalReceiverSolarFePerTick", 80, 0, 1_000_000_000);

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
    public static int orbitalReceiverEnergyCapacity;
    public static int orbitalReceiverSolarFePerTick;
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
        orbitalReceiverEnergyCapacity = ORBITAL_RECEIVER_ENERGY_CAPACITY.get();
        orbitalReceiverSolarFePerTick = ORBITAL_RECEIVER_SOLAR_FE_PER_TICK.get();
        orbitalReceiverTransferFePerTick = ORBITAL_RECEIVER_TRANSFER_FE_PER_TICK.get();
        orbitalReceiverMaxItemsPerTick = ORBITAL_RECEIVER_MAX_ITEMS_PER_TICK.get();

        if (forceFiniteResources && forceInfiniteResources) {
            throw new IllegalStateException("BeyondOrbit config cannot force both finite and infinite celestial resources.");
        }
    }
}
