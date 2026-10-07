package com.beckytidus.girlfriendmod.dialogue;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;

public final class WaitAndFollowLines {
    private static final String[] WAIT_HERE = {
        "Okay, I'll wait right here! Don't forget about me~",
        "I'll guard this spot. Come back soon, okay?",
        "Staying put! *salutes*",
        "I'll be here when you get back. Be careful!",
        "Hurry back~ I'll miss you.",
        "Okay! I'll just... admire the view.",
        "Go on, hero. I'll hold down the fort.",
    };

    private static final String[] FOLLOW_YOU = {
        "Right behind you!",
        "Wherever you go, I go~",
        "Coming! Wait for me!",
        "Lead the way, partner!",
        "Adventure time! *grabs your hand*",
        "Yay, together again!",
        "I'm with you. Always.",
    };

    private WaitAndFollowLines() {
    }

    public static String pickWaitHere(GirlFriendEntity gf) {
        return WAIT_HERE[gf.getRandom().nextInt(WAIT_HERE.length)];
    }

    public static String pickFollowYou(GirlFriendEntity gf) {
        return FOLLOW_YOU[gf.getRandom().nextInt(FOLLOW_YOU.length)];
    }
}
