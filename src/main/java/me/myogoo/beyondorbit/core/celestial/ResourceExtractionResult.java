package me.myogoo.beyondorbit.core.celestial;

import java.util.List;

public record ResourceExtractionResult(
        List<ExtractedResourceStack> resources,
        boolean bodyDepleted
) {
    public static final ResourceExtractionResult EMPTY = new ResourceExtractionResult(List.of(), false);

    public ResourceExtractionResult {
        resources = List.copyOf(resources);
    }

    public boolean extractedAny() {
        return !resources.isEmpty();
    }
}
