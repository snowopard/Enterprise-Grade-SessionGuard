package com.hyping.sessionguard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ComponentUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer LEGACY_SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();

    private ComponentUtil() {
        // Utility class
    }

    /**
     * Parse a legacy color code string (&) into a Component
     */
    public static Component parseLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return LEGACY_SERIALIZER.deserialize(text);
    }

    /**
     * Parse a legacy section symbol string (§) into a Component
     */
    public static Component parseLegacySection(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return LEGACY_SECTION_SERIALIZER.deserialize(text);
    }

    /**
     * Convert a Component back to legacy string
     */
    public static String toLegacyString(Component component) {
        if (component == null) {
            return "";
        }
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Create a simple text Component
     */
    public static Component text(String text) {
        return Component.text(text);
    }

    /**
     * Create a colored Component
     */
    public static Component colored(String text, String colorCode) {
        return parseLegacy(colorCode + text);
    }
}