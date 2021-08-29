package com.raccoon.taste;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilTest {


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