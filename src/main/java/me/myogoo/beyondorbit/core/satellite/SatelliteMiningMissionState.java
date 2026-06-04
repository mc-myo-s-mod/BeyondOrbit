package me.myogoo.beyondorbit.core.satellite;

import me.myogoo.beyondorbit.core.celestial.CelestialBodyDefinition;
import me.myogoo.beyondorbit.core.celestial.CelestialBodyState;
import me.myogoo.beyondorbit.core.celestial.CelestialResourceExtractor;
import me.myogoo.beyondorbit.core.celestial.ExtractedResourceStack;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionRequest;
import me.myogoo.beyondorbit.core.celestial.ResourceExtractionResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SatelliteMiningMissionState {
    private static final String SATELLITE_ID_TAG = "satellite_id";
    private static final String TARGET_BODY_TAG = "target_body";
    private static final String ACTIVE_TAG = "active";
    private static final String TICKS_UNTIL_NEXT_EXTRACTION_TAG = "ticks_until_next_extraction";
    private static final String TICKS_PER_EXTRACTION_TAG = "ticks_per_extraction";
    private static final String ROLLS_PER_EXTRACTION_TAG = "rolls_per_extraction";
    private static final String COMPLETED_EXTRACTIONS_TAG = "completed_extractions";
    private static final String TOTAL_EXTRACTED_TAG = "total_extracted";
    private static final String RESOURCE_ID_TAG = "id";
    private static final String AMOUNT_TAG = "amount";

    private final ResourceLocation satelliteId;
    private ResourceLocation targetBody;
    private boolean active;
    private int ticksUntilNextExtraction;
    private int ticksPerExtraction = 200;
    private int rollsPerExtraction = 1;
    private long completedExtractions;
    private final Map<ResourceLocation, Long> totalExtracted = new LinkedHashMap<>();

    public SatelliteMiningMissionState(ResourceLocation satelliteId) {
        this.satelliteId = satelliteId;
    }

    public static SatelliteMiningMissionState load(CompoundTag tag) {
        SatelliteMiningMissionState state = new SatelliteMiningMissionState(ResourceLocation.parse(tag.getString(SATELLITE_ID_TAG)));
        if (tag.contains(TARGET_BODY_TAG, Tag.TAG_STRING)) {
            state.targetBody = ResourceLocation.parse(tag.getString(TARGET_BODY_TAG));
        }
        state.active = tag.getBoolean(ACTIVE_TAG);
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
        return state;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(SATELLITE_ID_TAG, satelliteId.toString());
        if (targetBody != null) {
            tag.putString(TARGET_BODY_TAG, targetBody.toString());
        }
        tag.putBoolean(ACTIVE_TAG, active);
        tag.putInt(TICKS_UNTIL_NEXT_EXTRACTION_TAG, ticksUntilNextExtraction);
        tag.putInt(TICKS_PER_EXTRACTION_TAG, ticksPerExtraction);
        tag.putInt(ROLLS_PER_EXTRACTION_TAG, rollsPerExtraction);
        tag.putLong(COMPLETED_EXTRACTIONS_TAG, completedExtractions);

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
        return tag;
    }

    public void startMining(ResourceLocation targetBody, int rollsPerExtraction, int ticksPerExtraction) {
        this.targetBody = targetBody;
        this.rollsPerExtraction = Math.max(1, rollsPerExtraction);
        this.ticksPerExtraction = Math.max(1, ticksPerExtraction);
        this.ticksUntilNextExtraction = this.ticksPerExtraction;
        this.active = true;
    }

    public void stopMining() {
        this.active = false;
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
