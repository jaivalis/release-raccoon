package com.raccoon.taste;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Collection;

import lombok.val;

public class Util {

    private Util() {
        // hide implicit constructor
    }

    public static <T> Collection<MutablePair<T, Float>>  normalizeWeights(Collection<MutablePair<T, Float>> taste) {
        float max = 0;
        for (val pair : taste) {
            max = Math.max(max, pair.getRight());
        }
        if (max == 0) {
            taste.forEach(pair -> pair.setRight(1.0f));
        } else {
            final float maxWeight = max;
            taste.forEach(pair -> pair.setRight(pair.right / maxWeight));
        }
        return taste;
    }

}
