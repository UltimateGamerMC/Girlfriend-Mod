package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;

/** Player model plus a few cute emote poses. */
@Environment(EnvType.CLIENT)
public class GirlfriendModel extends PlayerModel {
    public GirlfriendModel(ModelPart root) {
        super(root, true);
    }

    @Override
    public void setupAnim(AvatarRenderState avatar) {
        super.setupAnim(avatar);
        if (!(avatar instanceof GirlfriendRenderState state)) return;
        float t = state.emoteTime;
        int emote = state.sulking ? GirlFriendEntity.EMOTE_POUT : state.emote;
        switch (emote) {
            case GirlFriendEntity.EMOTE_HUG, GirlFriendEntity.EMOTE_KISS -> {
                rightArm.xRot = -1.35f;
                leftArm.xRot = -1.35f;
                rightArm.yRot = -0.55f;
                leftArm.yRot = 0.55f;
                if (emote == GirlFriendEntity.EMOTE_KISS) {
                    head.xRot = 0.15f;
                    head.zRot = 0.25f;
                }
            }
            case GirlFriendEntity.EMOTE_WAVE -> {
                rightArm.xRot = -2.9f;
                rightArm.zRot = 0.25f + Mth.sin(t * 0.9f) * 0.35f;
            }
            case GirlFriendEntity.EMOTE_DANCE -> {
                float beat = Mth.sin(t * 0.5f);
                rightArm.xRot = -2.6f + beat * 0.5f;
                leftArm.xRot = -2.6f - beat * 0.5f;
                rightArm.zRot = 0.3f;
                leftArm.zRot = -0.3f;
                body.zRot = beat * 0.12f;
                head.zRot = -beat * 0.15f;
            }
            case GirlFriendEntity.EMOTE_NOD -> head.xRot = Mth.sin(t * 0.8f) * 0.3f;
            case GirlFriendEntity.EMOTE_HIGHFIVE -> {
                rightArm.xRot = -2.4f;
                rightArm.zRot = -0.2f;
            }
            case GirlFriendEntity.EMOTE_POUT -> {
                rightArm.xRot = -0.85f;
                leftArm.xRot = -0.85f;
                rightArm.yRot = 0.75f;
                leftArm.yRot = -0.75f;
                head.yRot += 0.5f;
                head.xRot = -0.15f;
            }
            default -> {
            }
        }
        if (state.sitting) {
            rightLeg.xRot = -1.45f;
            leftLeg.xRot = -1.45f;
            rightLeg.yRot = 0.2f;
            leftLeg.yRot = -0.2f;
        }
    }
}
