package com.beckytidus.girlfriendmod.registry;

import net.minecraft.util.math.random.Random;

import java.util.List;

public final class GirlfriendSkins {
    private static final List<String> SKIN_OWNERS = List.of(
        "Unstable_Owl", "Cosmic0504", "StxrlightLuna", "soosoo212", "JassuHassu", "SofiMarinova",
        "itz_Marie20", "cookieenderman0", "Len0115", "Devent", "locus8964", "bxnd", "togipi",
        "BloomiiBee", "By7h00m4s", "EmilyErdbeerMaus", "Notch", "Margarita_1", "DiamondIq", "x4v8",
        "hzroto", "Bombussal", "Azwal", "rawinput_", "Ryu_19", "Conetic", "Alirivie", "D34DGRL2000",
        "AriaofStars", "Prude", "xLmtshadow30"
    );

    private static final int TEXTURE_COUNT = 20;

    public static String pickRandom(Random random) {
        return SKIN_OWNERS.get(random.nextInt(SKIN_OWNERS.size()));
    }

    public static String pickRandomTextureVariant(Random random) {
        return String.valueOf(random.nextBetween(1, TEXTURE_COUNT));
    }
}
