package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GirlFriendEntityRenderer extends BipedEntityRenderer<GirlFriendEntity, GirlFriendEntityRenderState, GirlFriendEntityModel> {
    private static final Identifier TEXTURE_DEFAULT = Identifier.of("girlfriend-mod", "entity/girlfriend");
    private static final Identifier TEXTURE_ALT = Identifier.of("girlfriend-mod", "entity/girlfriend_alt");
    private final EntityRendererFactory.Context ctx;

    public GirlFriendEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new GirlFriendEntityModel(context.getPart(EntityModelLayers.PLAYER), true), 0.5f);
        this.ctx = context;
    }

    @Override
    public GirlFriendEntityRenderState createRenderState() {
        return new GirlFriendEntityRenderState();
    }

    @Override
    protected boolean hasLabel(GirlFriendEntity entity, double squaredDistanceToCamera) {
        if (squaredDistanceToCamera >= 4096.0) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return false;
        return !entity.isInvisibleTo(player);
    }

    @Override
    public void updateRenderState(GirlFriendEntity entity, GirlFriendEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        if (state.nameLabelPos == null) {
            state.nameLabelPos = new Vec3d(0, entity.getHeight(), 0);
        }
        state.statsLabel = entity.getStatsDisplayText();
        String skinName = entity.getSkinOwnerName();
        if (skinName != null && !skinName.isEmpty()) {
            try {
                state.skinTextures = ctx.getPlayerSkinCache().get(ProfileComponent.ofDynamic(skinName)).getTextures();
            } catch (Exception e) {
                applyDefaultTexture(state, entity);
            }
        } else {
            applyDefaultTexture(state, entity);
        }
        int emoteType = entity.getEmoteType();
        int emoteTicks = entity.getEmoteTicks();
        if (emoteType == GirlFriendEntity.EMOTE_CROUCH && emoteTicks > 0) {
            state.pose = EntityPose.CROUCHING;
            state.isInSneakingPose = true;
        } else if (emoteType == GirlFriendEntity.EMOTE_NOD && emoteTicks > 0) {
            float t = (20 - emoteTicks) + tickDelta;
            state.pitch = MathHelper.sin(t * 0.8f) * 15f * (float) Math.PI / 180f;
        }
    }

    @Override
    protected void renderLabelIfPresent(GirlFriendEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.displayName != null) {
            queue.submitLabel(matrices, state.nameLabelPos, 0, state.displayName, !state.sneaking, state.light, state.squaredDistanceToCamera, cameraState);
        }
        if (state.statsLabel != null) {
            queue.submitLabel(matrices, state.nameLabelPos, 9, state.statsLabel, !state.sneaking, state.light, state.squaredDistanceToCamera, cameraState);
        }
    }

    private void applyDefaultTexture(PlayerEntityRenderState state, GirlFriendEntity entity) {
        String v = entity.getTextureVariant();
        Identifier tex;
        if ("alt".equals(v)) {
            tex = TEXTURE_ALT;
        } else if (v != null && !v.isEmpty()) {
            try {
                int n = Integer.parseInt(v);
                if (n >= 1 && n <= 20) {
                    tex = Identifier.of("girlfriend-mod", "entity/girlfriend_" + n);
                } else {
                    tex = TEXTURE_DEFAULT;
                }
            } catch (NumberFormatException e) {
                tex = TEXTURE_DEFAULT;
            }
        } else {
            tex = TEXTURE_DEFAULT;
        }
        state.skinTextures = new net.minecraft.entity.player.SkinTextures(
            new net.minecraft.util.AssetInfo.TextureAssetInfo(tex),
            null,
            null,
            net.minecraft.entity.player.PlayerSkinType.SLIM,
            false
        );
    }

    @Override
    public Identifier getTexture(GirlFriendEntityRenderState state) {
        return state.skinTextures != null ? state.skinTextures.body().texturePath() : TEXTURE_DEFAULT;
    }
}