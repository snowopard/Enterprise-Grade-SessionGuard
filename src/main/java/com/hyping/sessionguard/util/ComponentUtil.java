package com.hyping.sessionguard.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

public class ComponentUtil {
    
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    public static @NotNull Component parseMiniMessage(@NotNull String text) {
        return MINI_MESSAGE.deserialize(text);
    }
    
    public static @NotNull String serializeMiniMessage(@NotNull Component component) {
        return MINI_MESSAGE.serialize(component);
    }
}