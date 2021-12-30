package com.raccoon.common.util;

public class StringUtil {

    private StringUtil() {
        // hide implicit constructor
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
