package com.uber.springserver.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class EnvUtil {

    private EnvUtil() {
    }

    public static String get(String key, String defaultValue) {
        String fromProperty = System.getProperty(key);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }

        String fromEnv = System.getenv(key);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }

        return defaultValue;
    }

    public static List<String> getCsv(String key, String defaultCsv) {
        return Arrays.stream(get(key, defaultCsv).split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }
}
