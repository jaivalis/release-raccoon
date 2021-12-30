package com.raccoon.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"a", "aa"})
    void isNullOrEmpty_nonNull(String str) {
        assertFalse(StringUtil.isNullOrEmpty(str));
    }

    @Test
    void isNullOrEmpty_null() {
        assertTrue(StringUtil.isNullOrEmpty(null));
    }

    @Test
    void isNullOrEmpty_empty() {
        assertTrue(StringUtil.isNullOrEmpty(""));
    }

}
