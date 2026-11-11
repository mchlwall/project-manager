package com.example.projectmanager.apitests.config;

public final class TestConfig {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private TestConfig() {}

    /** -DbaseUrl wins over the BASE_URL environment variable, which wins over localhost. */
    public static String baseUrl() {
        String fromProperty = System.getProperty("baseUrl");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty;
        }
        String fromEnv = System.getenv("BASE_URL");
        return fromEnv != null && !fromEnv.isBlank() ? fromEnv : DEFAULT_BASE_URL;
    }
}
