package me.myogoo.beyondorbit.core.celestial;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CelestialBodyState {
    private static final String BODY_ID_TAG = "body_id";
    private static final String RESOURCES_TAG = "resources";
    private static final String RESOURCE_ID_TAG = "id";
    private static final String REMAINING_TAG = "remaining";
    private static final String DEPLETED_TAG = "depleted";

    private final ResourceLocation bodyId;
    private final Map<ResourceLocation, Long> remainingResources = new HashMap<>();
    private boolean depleted;

    public CelestialBodyState(ResourceLocation bodyId) {
        this.bodyId = bodyId;
    }

    public static CelestialBodyState createInitial(CelestialBodyDefinition definition, double amountMultiplier) {
        CelestialBodyState state = new CelestialBodyState(definition.id());
        for (CelestialResourceDefinition resource : definition.resources()) {
            long amount = Math.max(0L, Math.round(resource.amount() * amountMultiplier));
            state.remainingResources.put(resource.id(), amount);
        }
        state.recomputeDepleted();
        return state;
    }

    public static CelestialBodyState load(CompoundTag tag) {
        CelestialBodyState state = new CelestialBodyState(ResourceLocation.parse(tag.getString(BODY_ID_TAG)));
        ListTag resources = tag.getList(RESOURCES_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < resources.size(); i++) {
            CompoundTag resourceTag = resources.getCompound(i);
            ResourceLocation resourceId = ResourceLocation.parse(resourceTag.getString(RESOURCE_ID_TAG));
            long remaining = resourceTag.getLong(REMAINING_TAG);
            state.remainingResources.put(resourceId, Math.max(0L, remaining));
        }
        state.depleted = tag.getBoolean(DEPLETED_TAG);
        state.recomputeDepleted();
        return state;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString(BODY_ID_TAG, bodyId.toString());
        tag.putBoolean(DEPLETED_TAG, depleted);

        ListTag resources = new ListTag();
        remainingResources.forEach((id, remaining) -> {
            CompoundTag resourceTag = new CompoundTag();
            resourceTag.putString(RESOURCE_ID_TAG, id.toString());
            resourceTag.putLong(REMAINING_TAG, Math.max(0L, remaining));
            resources.add(resourceTag);
        });
        tag.put(RESOURCES_TAG, resources);
        return tag;
    }

    public void syncDefinition(CelestialBodyDefinition definition, double amountMultiplier) {
        for (CelestialResourceDefinition resource : definition.resources()) {
            remainingResources.computeIfAbsent(resource.id(), ignored -> Math.max(0L, Math.round(resource.amount() * amountMultiplier)));
        }
        recomputeDepleted();
    }

    public ResourceLocation bodyId() {
        return bodyId;
    }

    public long remaining(ResourceLocation resourceId) {
        return remainingResources.getOrDefault(resourceId, 0L);
    }

    public Map<ResourceLocation, Long> remainingResourcesView() {
        return Collections.unmodifiableMap(remainingResources);
    }

    public boolean isResourceDepleted(ResourceLocation resourceId) {
        return remaining(resourceId) <= 0L;
    }

    public boolean isDepleted() {
        return depleted;
    }

    long consume(ResourceLocation resourceId, long requestedAmount) {
        if (requestedAmount <= 0L) {
            return 0L;
        }
        long current = remaining(resourceId);
        long consumed = Math.min(current, requestedAmount);
        remainingResources.put(resourceId, current - consumed);
        recomputeDepleted();
        return consumed;
    }

    private void recomputeDepleted() {
        depleted = !remainingResources.isEmpty() && remainingResources.values().stream().allMatch(value -> value <= 0L);
    }
}
