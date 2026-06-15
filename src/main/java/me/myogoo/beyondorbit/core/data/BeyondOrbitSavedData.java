package me.myogoo.beyondorbit.core.data;

import me.myogoo.beyondorbit.core.Config;
import me.myogoo.beyondorbit.core.BeyondOrbitCore;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyRegistry;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyState;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionResult;
import me.myogoo.beyondorbit.core.satellite.SatelliteMiningMissionState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class BeyondOrbitSavedData extends SavedData {
    private static final String DATA_NAME = BeyondOrbitCore.MODID + "_orbital_data";
    private static final String CELESTIAL_STATES_TAG = "celestial_states";
    private static final String SATELLITES_TAG = "satellites";

    private final Map<ResourceLocation, CelestialBodyState> celestialStates = new HashMap<>();
    private final Map<ResourceLocation, SatelliteMiningMissionState> satellites = new HashMap<>();

    public static BeyondOrbitSavedData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Cannot access BeyondOrbit saved data before overworld is available");
        }
        SavedData.Factory<BeyondOrbitSavedData> factory = new SavedData.Factory<>(BeyondOrbitSavedData::new, BeyondOrbitSavedData::load);
        return overworld.getDataStorage().computeIfAbsent(factory, DATA_NAME);
    }

    public static BeyondOrbitSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BeyondOrbitSavedData data = new BeyondOrbitSavedData();
        ListTag states = tag.getList(CELESTIAL_STATES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < states.size(); i++) {
            CelestialBodyState state = CelestialBodyState.load(states.getCompound(i));
            data.celestialStates.put(state.bodyId(), state);
        }
        ListTag satellites = tag.getList(SATELLITES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < satellites.size(); i++) {
            SatelliteMiningMissionState satellite = SatelliteMiningMissionState.load(satellites.getCompound(i));
            data.satellites.put(satellite.satelliteId(), satellite);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag states = new ListTag();
        celestialStates.values().stream()
                .sorted((left, right) -> left.bodyId().compareTo(right.bodyId()))
                .map(CelestialBodyState::save)
                .forEach(states::add);
        tag.put(CELESTIAL_STATES_TAG, states);

        ListTag satelliteTags = new ListTag();
        satellites.values().stream()
                .sorted((left, right) -> left.satelliteId().compareTo(right.satelliteId()))
                .map(SatelliteMiningMissionState::save)
                .forEach(satelliteTags::add);
        tag.put(SATELLITES_TAG, satelliteTags);
        return tag;
    }

    public CelestialBodyState getOrCreateState(CelestialBodyDefinition definition) {
        CelestialBodyState state = celestialStates.computeIfAbsent(definition.id(), ignored -> CelestialBodyState.createInitial(definition, Config.resourceAmountMultiplier));
        state.syncDefinition(definition, Config.resourceAmountMultiplier);
        setDirty();
        return state;
    }

    public CelestialBodyState resetState(CelestialBodyDefinition definition) {
        CelestialBodyState state = CelestialBodyState.createInitial(definition, Config.resourceAmountMultiplier);
        celestialStates.put(definition.id(), state);
        setDirty();
        return state;
    }

    public Optional<CelestialBodyState> getState(ResourceLocation bodyId) {
        return Optional.ofNullable(celestialStates.get(bodyId));
    }

    public Collection<CelestialBodyState> states() {
        return Collections.unmodifiableCollection(celestialStates.values());
    }

    public SatelliteMiningMissionState getOrCreateSatellite(ResourceLocation satelliteId) {
        SatelliteMiningMissionState satellite = satellites.computeIfAbsent(satelliteId, SatelliteMiningMissionState::new);
        setDirty();
        return satellite;
    }

    public Optional<SatelliteMiningMissionState> getSatellite(ResourceLocation satelliteId) {
        return Optional.ofNullable(satellites.get(satelliteId));
    }

    public Collection<SatelliteMiningMissionState> satellites() {
        return Collections.unmodifiableCollection(satellites.values());
    }

    public int lowOrbitSolarSatelliteCount() {
        return (int) satellites.values().stream()
                .filter(SatelliteMiningMissionState::isLowOrbitSolar)
                .count();
    }

    public int tickSatellites(RandomSource random) {
        int activeExtractions = 0;
        for (SatelliteMiningMissionState satellite : satellites.values()) {
            if (satellite.isLowOrbitSolar()) {
                continue;
            }
            if (!satellite.active() || satellite.targetBody() == null) {
                continue;
            }
            Optional<CelestialBodyDefinition> maybeDefinition = CelestialBodyRegistry.get(satellite.targetBody());
            if (maybeDefinition.isEmpty()) {
                continue;
            }
            CelestialBodyDefinition definition = maybeDefinition.get();
            CelestialBodyState bodyState = getOrCreateState(definition);
            ResourceExtractionResult result = satellite.tick(definition, bodyState, random);
            if (result.extractedAny()) {
                activeExtractions++;
                setDirty();
            }
        }
        return activeExtractions;
    }
}
