package com.beckytidus.girlfriendmod.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

@Environment(EnvType.CLIENT)
public class GirlfriendRenderState extends AvatarRenderState {
    public int emote;
    /** Ticks the current emote has been playing, with partial tick. */
    public float emoteTime;
    public boolean sitting;
    public boolean sulking;
}
