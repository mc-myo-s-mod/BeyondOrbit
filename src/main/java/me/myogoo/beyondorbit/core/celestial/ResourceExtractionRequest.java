package me.myogoo.beyondorbit.core.celestial;

public record ResourceExtractionRequest(
        int rolls,
        long yieldMultiplierNumerator,
        long yieldMultiplierDenominator
) {
    public ResourceExtractionRequest {
        if (rolls <= 0) {
            throw new IllegalArgumentException("rolls must be positive");
        }
        if (yieldMultiplierNumerator <= 0L) {
            throw new IllegalArgumentException("yieldMultiplierNumerator must be positive");
        }
        if (yieldMultiplierDenominator <= 0L) {
            throw new IllegalArgumentException("yieldMultiplierDenominator must be positive");
        }
    }

    public static ResourceExtractionRequest singleRoll() {
        return new ResourceExtractionRequest(1, 1L, 1L);
    }
}
