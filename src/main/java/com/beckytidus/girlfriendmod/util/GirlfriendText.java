package com.beckytidus.girlfriendmod.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class GirlfriendText {
    private static final int PINK = 0xFF7EB6;
    private static final int NAME_COLOR = 0xF5A9F2;

    private GirlfriendText() {
    }

    /** "♥ Luna: message" with a pink heart and name. */
    public static MutableComponent speech(String name, String message) {
        return Component.literal("♥ ").withColor(PINK)
            .append(Component.literal(name).withColor(NAME_COLOR))
            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(message).withStyle(ChatFormatting.WHITE));
    }

    /** "♥ Luna picked you a Poppy!" style narration lines. */
    public static MutableComponent action(String name, String rest) {
        return Component.literal("♥ ").withColor(PINK)
            .append(Component.literal(name).withColor(NAME_COLOR))
            .append(Component.literal(" " + rest).withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    }

    public static MutableComponent info(String message) {
        return Component.literal("♥ ").withColor(PINK).append(Component.literal(message).withStyle(ChatFormatting.GRAY));
    }

    public static MutableComponent hearts(int value, int max, int slots) {
        int filled = Math.round(slots * (float) value / max);
        MutableComponent out = Component.empty();
        out.append(Component.literal("♥".repeat(Math.max(0, filled))).withColor(PINK));
        out.append(Component.literal("♥".repeat(Math.max(0, slots - filled))).withStyle(ChatFormatting.DARK_GRAY));
        return out;
    }
}
