package com.raccoon.common;

import java.util.Objects;

public class StringUtil {

    private StringUtil() {
        // Hiding constructor
    }

    public static boolean isNullOrEmpty(String str) {
        return Objects.isNull(str) || str.isBlank();
    }
}
