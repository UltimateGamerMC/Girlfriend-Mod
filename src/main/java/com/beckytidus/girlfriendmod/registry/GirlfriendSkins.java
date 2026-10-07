package com.beckytidus.girlfriendmod.registry;

import net.minecraft.util.RandomSource;

public final class GirlfriendSkins {
    public static final int TEXTURE_COUNT = 20;

    private GirlfriendSkins() {
    }

    public static String pickRandomTextureVariant(RandomSource random) {
        return String.valueOf(random.nextInt(TEXTURE_COUNT) + 1);
    }
}
