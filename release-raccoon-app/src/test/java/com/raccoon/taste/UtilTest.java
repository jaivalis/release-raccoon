package com.raccoon.taste;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilTest {

    @Test
    @DisplayName("Zero weights for all artists should result in equal preference of 100%")
    void testNormalizeWeightsZeroes() {
        Collection<MutablePair<String, Float>> taste = List.of(
                new MutablePair<>("a", 0.f),
                new MutablePair<>("b", 0.f),
                new MutablePair<>("c", 0.f)
        );
        final var normalized = Util.normalizeWeights(taste);

        assertTrue(normalized.contains(new MutablePair<>("a", 1f)));
        assertTrue(normalized.contains(new MutablePair<>("b", 1f)));
        assertTrue(normalized.contains(new MutablePair<>("c", 1f)));
    }

    @Test
    void testNormalizeWeights() {
        Collection<MutablePair<String, Float>> taste = List.of(
                new MutablePair<>("a", 100.f),
                new MutablePair<>("b", 0.f),
                new MutablePair<>("c", 50.f)
        );
        final var normalized = Util.normalizeWeights(taste);

        assertTrue(normalized.contains(new MutablePair<>("a", 1f)));
        assertTrue(normalized.contains(new MutablePair<>("b", 0f)));
        assertTrue(normalized.contains(new MutablePair<>("c", 0.5f)));
    }
}