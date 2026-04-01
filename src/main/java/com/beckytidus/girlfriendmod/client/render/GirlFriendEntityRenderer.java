package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

@Environment(EnvType.CLIENT)
public class GirlFriendEntityRenderer extends HumanoidMobRenderer<GirlFriendEntity, AvatarRenderState, PlayerModel> {
    private static final Identifier TEXTURE_DEFAULT = Identifier.fromNamespaceAndPath("girlfriend-mod", "entity/girlfriend");
    private static final Identifier TEXTURE_ALT = Identifier.fromNamespaceAndPath("girlfriend-mod", "entity/girlfriend_alt");
    private final EntityRendererProvider.Context ctx;

    public GirlFriendEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM), true), new PlayerModel(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM), true), 0.5f);
        this.ctx = context;
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    protected boolean shouldShowName(GirlFriendEntity entity, double squaredDistanceToCamera) {
        if (squaredDistanceToCamera >= 4096.0) return false;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return false;
        return !entity.isInvisibleTo(player);
    }

    @Override
    public void extractRenderState(GirlFriendEntity entity, AvatarRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        String skinName = entity.getSkinOwnerName();
        if (skinName != null && !skinName.isEmpty()) {
            try {
                state.skin = ctx.getPlayerSkinRenderCache().getOrDefault(ResolvableProfile.createUnresolved(skinName)).playerSkin();
            } catch (Exception e) {
                applyDefaultTexture(state, entity);
            }
        } else {
            applyDefaultTexture(state, entity);
        }
        int emoteType = entity.getEmoteType();
        int emoteTicks = entity.getEmoteTicks();
        if (emoteType == GirlFriendEntity.EMOTE_CROUCH && emoteTicks > 0) {
            state.isCrouching = true;
        } else if (emoteType == GirlFriendEntity.EMOTE_NOD && emoteTicks > 0) {
            float t = (20 - emoteTicks) + tickDelta;
            state.xRot = Mth.sin(t * 0.8f) * 15f * (float) Math.PI / 180f;
        }
    }

    @Override
    protected void scale(AvatarRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    private void applyDefaultTexture(AvatarRenderState state, GirlFriendEntity entity) {
        String v = entity.getTextureVariant();
        Identifier tex;
        if ("alt".equals(v)) {
            tex = TEXTURE_ALT;
        } else if (v != null && !v.isEmpty()) {
            try {
                int n = Integer.parseInt(v);
                if (n >= 1 && n <= 20) {
                    tex = Identifier.fromNamespaceAndPath("girlfriend-mod", "entity/girlfriend_" + n);
                } else {
                    tex = TEXTURE_DEFAULT;
                }
            } catch (NumberFormatException e) {
                tex = TEXTURE_DEFAULT;
            }
        } else {
            tex = TEXTURE_DEFAULT;
        }
        state.skin = new PlayerSkin(new ClientAsset.ResourceTexture(tex), null, null, PlayerModelType.SLIM, true);
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        return state.skin.body().texturePath();
    }
}
