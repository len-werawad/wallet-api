package com.lbk.wallet.common.util;

import java.util.UUID;

/**
 * Utility class for generating trace IDs for request tracking and correlation
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
        // Prevent instantiation
    }

    /**
     * Generate a unique trace ID
     * Format: UUID without hyphens (e.g., "a1b2c3d4e5f6...")
     */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate a short trace ID (first 16 characters)
     * Format: First segment of UUID (e.g., "a1b2c3d4e5f67890")
     */
    public static String generateShort() {
        return generate().substring(0, 16);
    }
}

