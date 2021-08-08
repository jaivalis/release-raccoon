package com.raccoon.taste;

import com.raccoon.entity.Artist;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Collection;
import java.util.Collections;

import lombok.val;

public class Util {

    private Util() {
        // hide implicit constructor
    }

    public static Collection<MutablePair<Artist, Float>> normalizeWeights(Collection<MutablePair<Artist, Float>> taste) {
        float max = 0;
        for (val pair : taste) {
            max = Math.max(max, pair.getRight());
        }
        if (max == 0) {
            return Collections.emptyList();
        }
        final float maxWeight = max;
        taste.forEach(pair -> pair.setRight(pair.right / maxWeight));
        return taste;
    }

}
