package com.beckytidus.girlfriendmod.dialogue;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.util.RelationshipTier;

public final class HugAndHitResponses {
    private static final String[] HUG_SHY = {
        "Oh! Um... hi. *hugs back awkwardly*",
        "*blushes* W-we're hugging now? Okay~",
        "That was... nice. You can do that again.",
        "*squeaks* You surprised me!",
        "I'm still a little shy, but... I liked that.",
    };

    private static final String[] HUG_CLOSE = {
        "*hugs you back* I needed that.",
        "*squeezes you tight* Don't let go yet.",
        "You're so warm. I could stay here forever.",
        "*nuzzles into your shoulder* Mine.",
        "Hold me a little longer?",
        "You always know when I need a hug.",
        "*happy humming*",
        "This is my favorite place in the whole world.",
        "*kisses your cheek* Hi, you.",
        "Heehee, again! Again!",
        "My heart's doing the fluttery thing.",
        "I'm so lucky you're mine.",
    };

    private static final String[] HEADPAT = {
        "*leans into the headpat* Mmmh~",
        "Heehee, that tickles!",
        "*purrs* ...What? I didn't purr.",
        "More pats. That's an order.",
        "Am I a good girlfriend? *beams*",
        "*closes eyes happily*",
    };

    private static final String[] BONK = {
        "Hey! No bonking!",
        "*pouts* Rude.",
        "Ow-- okay that didn't hurt, but still!",
        "Do that again and no cookies for you.",
        "*crosses arms* Hmph!",
        "Excuse me?! I'm your girlfriend, not a zombie!",
        "Missed me~ Nice try.",
    };

    private static final String[] HIT_BY_OWNER = {
        "Ow! That actually hurt... I'm not talking to you for a bit.",
        "Hey! What was that for?! *turns away*",
        "...I'm going to go sulk now.",
        "That was mean. I need a minute.",
        "*sniffles* Why would you do that?",
    };

    private static final String[] HIT_BY_OTHER = {
        "Ow! Hey!",
        "That hurt! Watch it!",
        "I'm okay! I'm okay!",
        "You'll pay for that!",
        "Is that all you've got?",
        "Eek! Help me out here!",
        "Nobody hits me in front of my partner!",
    };

    private HugAndHitResponses() {
    }

    private static String pick(GirlFriendEntity gf, String[] lines) {
        return lines[gf.getRandom().nextInt(lines.length)];
    }

    public static String pickHug(GirlFriendEntity gf) {
        return pick(gf, gf.getRelationshipTier().atLeast(RelationshipTier.CRUSH) ? HUG_CLOSE : HUG_SHY);
    }

    public static String pickHeadpat(GirlFriendEntity gf) {
        return pick(gf, HEADPAT);
    }

    public static String pickBonk(GirlFriendEntity gf) {
        return pick(gf, BONK);
    }

    public static String pickHitByOwner(GirlFriendEntity gf) {
        return pick(gf, HIT_BY_OWNER);
    }

    public static String pickHitByOther(GirlFriendEntity gf) {
        return pick(gf, HIT_BY_OTHER);
    }
}
