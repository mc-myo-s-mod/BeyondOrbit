package me.myogoo.beyondorbit.core.satellite;

import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyState;
import me.myogoo.beyondorbit.core.celestial.CelestialResourceExtractor;
import me.myogoo.beyondorbit.core.celestial.ExtractedResourceStack;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionRequest;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import me.myogoo.beyondorbit.core.module.OrbitalModuleTier;
import me.myogoo.beyondorbit.core.module.OrbitalModuleType;
import me.myogoo.beyondorbit.core.solar.SolarPanelTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SatelliteMiningMissionState {
    private static final String SATELLITE_ID_TAG = "satellite_id";
    private static final String KIND_TAG = "kind";
    private static final String TARGET_BODY_TAG = "target_body";
    private static final String ACTIVE_TAG = "active";
    private static final String MISSION_PHASE_TAG = "mission_phase";
    private static final String PHASE_TICKS_REMAINING_TAG = "phase_ticks_remaining";
    private static final String TRANSIT_TICKS_TAG = "transit_ticks";
    private static final String SOLAR_PANEL_TIER_TAG = "solar_panel_tier";
    private static final String ORBIT_DISTANCE_KM_TAG = "orbit_distance_km";
    private static final String TICKS_UNTIL_NEXT_EXTRACTION_TAG = "ticks_until_next_extraction";
    private static final String TICKS_PER_EXTRACTION_TAG = "ticks_per_extraction";
    private static final String ROLLS_PER_EXTRACTION_TAG = "rolls_per_extraction";
    private static final String COMPLETED_EXTRACTIONS_TAG = "completed_extractions";
    private static final String STORED_ENERGY_TAG = "stored_energy";
    private static final String ENERGY_CAPACITY_TAG = "energy_capacity";
    private static final String TOTAL_EXTRACTED_TAG = "total_extracted";
    private static final String EQUIPPED_MODULES_TAG = "equipped_modules";
    private static final String MODULE_TYPE_TAG = "type";
    private static final String MODULE_TIER_TAG = "tier";
    private static final String RESOURCE_ID_TAG = "id";
    private static final String AMOUNT_TAG = "amount";

    public enum SatelliteKind {
        MINING("mining"),
        LOW_ORBIT_SOLAR("low_orbit_solar"),
        ENERGY_STORAGE("energy_storage");

        private final String serializedName;

        SatelliteKind(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        public static SatelliteKind bySerializedName(String serializedName) {
            for (SatelliteKind kind : values()) {
                if (kind.serializedName.equals(serializedName)) {
                    return kind;
                }
            }
            return null;
        }
    }

    public enum MissionPhase {
        LAUNCHING("launching"),
        IN_TRANSIT("in_transit"),
        DEPLOYING("deploying"),
        ACTIVE("active"),
        IDLE("idle");

        private final String serializedName;

        MissionPhase(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        public static MissionPhase bySerializedName(String serializedName) {
            for (MissionPhase phase : values()) {
                if (phase.serializedName.equals(serializedName)) {
                    return phase;
                }
            }
            return null;
        }
    }

    private final ResourceLocation satelliteId;
    private SatelliteKind kind = SatelliteKind.MINING;
    private ResourceLocation targetBody;
    private boolean active;
    private MissionPhase missionPhase = MissionPhase.IDLE;
    private int phaseTicksRemaining;
    private int transitTicks;
    private SolarPanelTier solarPanelTier = SolarPanelTier.BASIC;
    private int orbitDistanceKm;
    private int ticksUntilNextExtraction;
    private int ticksPerExtraction = 200;
    private int rollsPerExtraction = 1;
    private long completedExtractions;
    private int storedEnergy;
    private int energyCapacity;
    private final Map<ResourceLocation, Long> totalExtracted = new LinkedHashMap<>();
    private final EnumMap<OrbitalModuleType, OrbitalModuleTier> equippedModules = new EnumMap<>(OrbitalModuleType.class);

    public SatelliteMiningMissionState(ResourceLocation satelliteId) {
        this.satelliteId = satelliteId;
    }

    public static SatelliteMiningMissionState load(CompoundTag tag) {
        SatelliteMiningMissionState state = new SatelliteMiningMissionState(ResourceLocation.parse(tag.getString(SATELLITE_ID_TAG)));
        if (tag.contains(KIND_TAG, Tag.TAG_STRING)) {
            SatelliteKind kind = SatelliteKind.bySerializedName(tag.getString(KIND_TAG));
            if (kind != null) {
                state.kind = kind;
            }
        }
        if (tag.contains(TARGET_BODY_TAG, Tag.TAG_STRING)) {
            state.targetBody = ResourceLocation.parse(tag.getString(TARGET_BODY_TAG));
        }
        state.active = tag.getBoolean(ACTIVE_TAG);
        if (tag.contains(MISSION_PHASE_TAG, Tag.TAG_STRING)) {
            MissionPhase phase = MissionPhase.bySerializedName(tag.getString(MISSION_PHASE_TAG));
            if (phase != null) {
                state.missionPhase = phase;
            }
        } else if (state.isLowOrbitSolar()) {
            // Saved worlds from the pre-phase solar implementation stored LOW_ORBIT_SOLAR
            // satellites with active=false, while receivers counted every deployed solar
            // satellite. Keep those existing satellites productive after migration.
            state.missionPhase = MissionPhase.ACTIVE;
        } else {
            state.missionPhase = state.active ? MissionPhase.ACTIVE : MissionPhase.IDLE;
        }
        state.phaseTicksRemaining = Math.max(0, tag.getInt(PHASE_TICKS_REMAINING_TAG));
        state.transitTicks = Math.max(0, tag.getInt(TRANSIT_TICKS_TAG));
        if (tag.contains(SOLAR_PANEL_TIER_TAG, Tag.TAG_STRING)) {
            SolarPanelTier tier = SolarPanelTier.bySerializedName(tag.getString(SOLAR_PANEL_TIER_TAG));
            if (tier != null) {
                state.solarPanelTier = tier;
            }
        }
        if (tag.contains(ORBIT_DISTANCE_KM_TAG, Tag.TAG_INT)) {
            state.orbitDistanceKm = Math.max(0, tag.getInt(ORBIT_DISTANCE_KM_TAG));
        } else if (state.isLowOrbitSolar()) {
            state.orbitDistanceKm = Math.max(0, Config.lowOrbitSolarDistanceKm);
        }
        state.active = state.missionPhase == MissionPhase.ACTIVE && (state.active || state.isLowOrbitSolar() || state.isEnergyStorage());
        state.storedEnergy = Math.max(0, tag.getInt(STORED_ENERGY_TAG));
        state.energyCapacity = Math.max(0, tag.getInt(ENERGY_CAPACITY_TAG));
        if (state.isEnergyStorage() && state.energyCapacity <= 0) {
            state.energyCapacity = Math.max(0, Config.orbitalEnergyStorageCapacity);
        }
        state.storedEnergy = Math.min(state.storedEnergy, state.energyCapacity);
        state.ticksUntilNextExtraction = Math.max(0, tag.getInt(TICKS_UNTIL_NEXT_EXTRACTION_TAG));
        state.ticksPerExtraction = Math.max(1, tag.getInt(TICKS_PER_EXTRACTION_TAG));
        state.rollsPerExtraction = Math.max(1, tag.getInt(ROLLS_PER_EXTRACTION_TAG));
        state.completedExtractions = Math.max(0L, tag.getLong(COMPLETED_EXTRACTIONS_TAG));

        ListTag totals = tag.getList(TOTAL_EXTRACTED_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < totals.size(); i++) {
            CompoundTag resourceTag = totals.getCompound(i);
            ResourceLocation resourceId = ResourceLocation.parse(resourceTag.getString(RESOURCE_ID_TAG));
            long amount = Math.max(0L, resourceTag.getLong(AMOUNT_TAG));
            if (amount > 0L) {
                state.totalExtracted.put(resourceId, amount);
            }
        }

        ListTag modules = tag.getList(EQUIPPED_MODULES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < modules.size(); i++) {
            CompoundTag moduleTag = modules.getCompound(i);
            OrbitalModuleType type = OrbitalModuleType.bySerializedName(moduleTag.getString(MODULE_TYPE_TAG));
            OrbitalModuleTier tier = OrbitalModuleTier.bySerializedName(moduleTag.getString(MODULE_TIER_TAG));
            if (type != null && tier != null) {
                state.equippedModules.put(type, tier);
            }
        }
        return state;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(SATELLITE_ID_TAG, satelliteId.toString());
        tag.putString(KIND_TAG, kind.serializedName());
        if (targetBody != null) {
            tag.putString(TARGET_BODY_TAG, targetBody.toString());
        }
        tag.putBoolean(ACTIVE_TAG, active);
        tag.putString(MISSION_PHASE_TAG, missionPhase.serializedName());
        tag.putInt(PHASE_TICKS_REMAINING_TAG, phaseTicksRemaining);
        tag.putInt(TRANSIT_TICKS_TAG, transitTicks);
        tag.putString(SOLAR_PANEL_TIER_TAG, solarPanelTier.serializedName());
        tag.putInt(ORBIT_DISTANCE_KM_TAG, orbitDistanceKm);
        tag.putInt(TICKS_UNTIL_NEXT_EXTRACTION_TAG, ticksUntilNextExtraction);
        tag.putInt(TICKS_PER_EXTRACTION_TAG, ticksPerExtraction);
        tag.putInt(ROLLS_PER_EXTRACTION_TAG, rollsPerExtraction);
        tag.putLong(COMPLETED_EXTRACTIONS_TAG, completedExtractions);
        tag.putInt(STORED_ENERGY_TAG, storedEnergy);
        tag.putInt(ENERGY_CAPACITY_TAG, energyCapacity);

        ListTag totals = new ListTag();
        totalExtracted.forEach((id, amount) -> {
            if (amount > 0L) {
                CompoundTag resourceTag = new CompoundTag();
                resourceTag.putString(RESOURCE_ID_TAG, id.toString());
                resourceTag.putLong(AMOUNT_TAG, amount);
                totals.add(resourceTag);
            }
        });
        tag.put(TOTAL_EXTRACTED_TAG, totals);

        ListTag modules = new ListTag();
        equippedModules.forEach((type, tier) -> {
            CompoundTag moduleTag = new CompoundTag();
            moduleTag.putString(MODULE_TYPE_TAG, type.serializedName());
            moduleTag.putString(MODULE_TIER_TAG, tier.serializedName());
            modules.add(moduleTag);
        });
        tag.put(EQUIPPED_MODULES_TAG, modules);
        return tag;
    }

    public void startMining(ResourceLocation targetBody, int rollsPerExtraction, int ticksPerExtraction) {
        this.kind = SatelliteKind.MINING;
        this.targetBody = targetBody;
        updateMiningProfile(rollsPerExtraction, ticksPerExtraction);
        this.missionPhase = MissionPhase.ACTIVE;
        this.phaseTicksRemaining = 0;
        this.transitTicks = 0;
        this.active = true;
    }

    public void startLaunchPadMining(ResourceLocation targetBody, int rollsPerExtraction, int ticksPerExtraction, int launchTicks) {
        startLaunchPadMining(targetBody, rollsPerExtraction, ticksPerExtraction, launchTicks, 0);
    }

    public void startLaunchPadMining(ResourceLocation targetBody, int rollsPerExtraction, int ticksPerExtraction, int launchTicks, int transitTicks) {
        this.kind = SatelliteKind.MINING;
        this.targetBody = targetBody;
        this.rollsPerExtraction = Math.max(1, rollsPerExtraction);
        this.ticksPerExtraction = Math.max(1, ticksPerExtraction);
        this.ticksUntilNextExtraction = this.ticksPerExtraction;
        this.transitTicks = Math.max(0, transitTicks);
        int safeLaunchTicks = Math.max(0, launchTicks);
        if (safeLaunchTicks > 0) {
            this.phaseTicksRemaining = safeLaunchTicks;
            this.missionPhase = MissionPhase.LAUNCHING;
            this.active = false;
        } else if (this.transitTicks > 0) {
            this.phaseTicksRemaining = this.transitTicks;
            this.transitTicks = 0;
            this.missionPhase = MissionPhase.IN_TRANSIT;
            this.active = false;
        } else {
            this.phaseTicksRemaining = 0;
            this.missionPhase = MissionPhase.ACTIVE;
            this.active = true;
        }
    }

    public void updateMiningProfile(int rollsPerExtraction, int ticksPerExtraction) {
        this.rollsPerExtraction = Math.max(1, rollsPerExtraction);
        this.ticksPerExtraction = Math.max(1, ticksPerExtraction);
        if (this.active) {
            this.ticksUntilNextExtraction = Math.min(Math.max(1, this.ticksUntilNextExtraction), this.ticksPerExtraction);
        } else {
            this.ticksUntilNextExtraction = this.ticksPerExtraction;
        }
    }

    public void stopMining() {
        this.active = false;
        this.missionPhase = MissionPhase.IDLE;
        this.phaseTicksRemaining = 0;
        this.transitTicks = 0;
    }

    public void markLowOrbitSolar() {
        markLowOrbitSolar(0, SolarPanelTier.BASIC, 0);
    }

    public void markLowOrbitSolar(int deploymentTicks) {
        markLowOrbitSolar(deploymentTicks, SolarPanelTier.BASIC, 0);
    }

    public void markLowOrbitSolar(int deploymentTicks, SolarPanelTier panelTier, int orbitDistanceKm) {
        this.kind = SatelliteKind.LOW_ORBIT_SOLAR;
        this.targetBody = null;
        this.solarPanelTier = panelTier == null ? SolarPanelTier.BASIC : panelTier;
        this.orbitDistanceKm = Math.max(0, orbitDistanceKm);
        this.phaseTicksRemaining = Math.max(0, deploymentTicks);
        this.transitTicks = 0;
        this.missionPhase = this.phaseTicksRemaining > 0 ? MissionPhase.DEPLOYING : MissionPhase.ACTIVE;
        this.active = this.missionPhase == MissionPhase.ACTIVE;
        this.ticksUntilNextExtraction = 0;
    }

    public void markEnergyStorage(int deploymentTicks, int energyCapacity) {
        this.kind = SatelliteKind.ENERGY_STORAGE;
        this.targetBody = null;
        this.solarPanelTier = SolarPanelTier.BASIC;
        this.orbitDistanceKm = 0;
        this.energyCapacity = Math.max(0, energyCapacity);
        this.storedEnergy = Math.min(this.storedEnergy, this.energyCapacity);
        this.phaseTicksRemaining = Math.max(0, deploymentTicks);
        this.transitTicks = 0;
        this.missionPhase = this.phaseTicksRemaining > 0 ? MissionPhase.DEPLOYING : MissionPhase.ACTIVE;
        this.active = this.missionPhase == MissionPhase.ACTIVE;
        this.ticksUntilNextExtraction = 0;
    }

    public int receiveEnergy(int amount, int maxTransfer) {
        if (!isEnergyStorage() || missionPhase != MissionPhase.ACTIVE || energyCapacity <= 0 || amount <= 0 || maxTransfer <= 0) {
            return 0;
        }
        int accepted = Math.min(Math.min(amount, maxTransfer), energyCapacity - storedEnergy);
        if (accepted > 0) {
            storedEnergy += accepted;
        }
        return accepted;
    }

    public int extractEnergy(int amount, int maxTransfer) {
        if (!isEnergyStorage() || missionPhase != MissionPhase.ACTIVE || amount <= 0 || maxTransfer <= 0) {
            return 0;
        }
        int extracted = Math.min(Math.min(amount, maxTransfer), storedEnergy);
        if (extracted > 0) {
            storedEnergy -= extracted;
        }
        return extracted;
    }

    public boolean advanceMissionPhase() {
        if (missionPhase == MissionPhase.ACTIVE || missionPhase == MissionPhase.IDLE) {
            return false;
        }
        if (phaseTicksRemaining > 0) {
            phaseTicksRemaining--;
        }
        if (phaseTicksRemaining > 0) {
            return true;
        }
        if (missionPhase == MissionPhase.LAUNCHING && transitTicks > 0) {
            missionPhase = MissionPhase.IN_TRANSIT;
            phaseTicksRemaining = transitTicks;
            transitTicks = 0;
            active = false;
            return true;
        }
        missionPhase = MissionPhase.ACTIVE;
        active = true;
        return true;
    }

    public boolean isLowOrbitSolar() {
        return kind == SatelliteKind.LOW_ORBIT_SOLAR;
    }

    public boolean isEnergyStorage() {
        return kind == SatelliteKind.ENERGY_STORAGE;
    }

    public SatelliteKind kind() {
        return kind;
    }

    public OrbitalModuleTier equipModule(OrbitalModuleType type, OrbitalModuleTier tier) {
        if (type == null || tier == null) {
            return null;
        }
        return equippedModules.put(type, tier);
    }

    public void replaceEquippedModules(OrbitalModuleTier miningTier, OrbitalModuleTier speedTier, OrbitalModuleTier efficiencyTier) {
        equippedModules.clear();
        equipModule(OrbitalModuleType.MINING, miningTier);
        equipModule(OrbitalModuleType.SPEED, speedTier);
        equipModule(OrbitalModuleType.EFFICIENCY, efficiencyTier);
    }

    public OrbitalModuleTier equippedModuleTier(OrbitalModuleType type) {
        return equippedModules.get(type);
    }

    public int equippedModuleTierLevel(OrbitalModuleType type) {
        OrbitalModuleTier tier = equippedModuleTier(type);
        return tier == null ? 0 : tier.level();
    }

    public Map<OrbitalModuleType, OrbitalModuleTier> equippedModulesView() {
        return Collections.unmodifiableMap(equippedModules);
    }

    public void clearExtracted() {
        this.totalExtracted.clear();
    }

    public long removeExtracted(ResourceLocation resourceId, long maxAmount) {
        if (maxAmount <= 0L) {
            return 0L;
        }
        long available = this.totalExtracted.getOrDefault(resourceId, 0L);
        if (available <= 0L) {
            return 0L;
        }
        long removed = Math.min(available, maxAmount);
        long remaining = available - removed;
        if (remaining <= 0L) {
            this.totalExtracted.remove(resourceId);
        } else {
            this.totalExtracted.put(resourceId, remaining);
        }
        return removed;
    }

    public ResourceExtractionResult tick(CelestialBodyDefinition definition, CelestialBodyState bodyState, RandomSource random) {
        if (isLowOrbitSolar()) {
            return ResourceExtractionResult.EMPTY;
        }
        if (!active || targetBody == null || !targetBody.equals(definition.id())) {
            return ResourceExtractionResult.EMPTY;
        }

        if (ticksUntilNextExtraction > 0) {
            ticksUntilNextExtraction--;
        }
        if (ticksUntilNextExtraction > 0) {
            return ResourceExtractionResult.EMPTY;
        }

        ResourceExtractionResult result = CelestialResourceExtractor.extract(
                definition,
                bodyState,
                new ResourceExtractionRequest(rollsPerExtraction, 1L, 1L),
                random
        );
        ticksUntilNextExtraction = ticksPerExtraction;
        if (result.extractedAny()) {
            completedExtractions++;
            for (ExtractedResourceStack stack : result.resources()) {
                totalExtracted.merge(stack.id(), stack.amount(), Long::sum);
            }
        }
        if (result.bodyDepleted()) {
            active = false;
            missionPhase = MissionPhase.IDLE;
            transitTicks = 0;
        }
        return result;
    }

    public ResourceLocation satelliteId() {
        return satelliteId;
    }

    public ResourceLocation targetBody() {
        return targetBody;
    }

    public boolean active() {
        return active;
    }

    public MissionPhase missionPhase() {
        return missionPhase;
    }

    public int phaseTicksRemaining() {
        return phaseTicksRemaining;
    }

    public int transitTicks() {
        return transitTicks;
    }

    public SolarPanelTier solarPanelTier() {
        return solarPanelTier;
    }

    public int orbitDistanceKm() {
        return orbitDistanceKm;
    }

    public int ticksUntilNextExtraction() {
        return ticksUntilNextExtraction;
    }

    public int ticksPerExtraction() {
        return ticksPerExtraction;
    }

    public int rollsPerExtraction() {
        return rollsPerExtraction;
    }

    public long completedExtractions() {
        return completedExtractions;
    }

    public int storedEnergy() {
        return storedEnergy;
    }

    public int energyCapacity() {
        return energyCapacity;
    }

    public Map<ResourceLocation, Long> drainTotalExtracted() {
        if (totalExtracted.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<ResourceLocation, Long> drained = new LinkedHashMap<>(totalExtracted);
        totalExtracted.clear();
        return drained;
    }

    public Map<ResourceLocation, Long> totalExtractedView() {
        return Collections.unmodifiableMap(totalExtracted);
    }
}
