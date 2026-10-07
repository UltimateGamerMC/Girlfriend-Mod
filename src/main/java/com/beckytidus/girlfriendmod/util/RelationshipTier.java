package com.beckytidus.girlfriendmod.util;

import net.minecraft.ChatFormatting;

public enum RelationshipTier {
    JUST_MET(0, "Just Met", ChatFormatting.GRAY, "She's a little shy around you."),
    CRUSH(10, "Crush", ChatFormatting.YELLOW, "She blushes when you talk to her. Gifts get better."),
    DATING(25, "Dating", ChatFormatting.LIGHT_PURPLE, "She hits harder when protecting you."),
    PARTNER(50, "Partner", ChatFormatting.RED, "She patches you up when you're hurt."),
    SOULMATE(75, "Soulmate", ChatFormatting.DARK_RED, "Once a day, she'll pull you back from death."),
    FOREVER(100, "Forever Yours", ChatFormatting.GOLD, "Rare gifts, max stats, and endless devotion.");

    public final int minLevel;
    public final String title;
    public final ChatFormatting color;
    public final String perk;

    RelationshipTier(int minLevel, String title, ChatFormatting color, String perk) {
        this.minLevel = minLevel;
        this.title = title;
        this.color = color;
        this.perk = perk;
    }

    public static RelationshipTier of(int level) {
        RelationshipTier result = JUST_MET;
        for (RelationshipTier tier : values()) {
            if (level >= tier.minLevel) result = tier;
        }
        return result;
    }

    public boolean atLeast(RelationshipTier other) {
        return this.ordinal() >= other.ordinal();
    }
}
