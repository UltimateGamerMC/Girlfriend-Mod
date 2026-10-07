package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.GirlfriendMod;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

@Environment(EnvType.CLIENT)
public class GirlFriendEntityRenderer extends HumanoidMobRenderer<GirlFriendEntity, AvatarRenderState, PlayerModel> {
    private final EntityRendererProvider.Context ctx;

    public GirlFriendEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new GirlfriendModel(context.bakeLayer(ModelLayers.PLAYER_SLIM)), 0.5f);
        this.ctx = context;
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new GirlfriendRenderState();
    }

    @Override
    public void extractRenderState(GirlFriendEntity entity, AvatarRenderState avatar, float partialTick) {
        super.extractRenderState(entity, avatar, partialTick);
        GirlfriendRenderState state = (GirlfriendRenderState) avatar;
        ResolvableProfile profile = entity.getSkinProfile();
        if (profile != ResolvableProfile.Static.EMPTY && profile.name().isPresent()) {
            state.skin = ctx.getPlayerSkinRenderCache().getOrDefault(profile).playerSkin();
        } else {
            state.skin = builtInSkin(entity.getTextureVariant());
        }
        state.emote = entity.getEmoteTicks() > 0 ? entity.getEmoteType() : GirlFriendEntity.EMOTE_NONE;
        state.emoteTime = entity.tickCount + partialTick;
        state.sitting = entity.isSitting();
        state.sulking = entity.isSulking();
        state.isCrouching = state.emote == GirlFriendEntity.EMOTE_CROUCH;
    }

    private static PlayerSkin builtInSkin(String variant) {
        String path = "entity/girlfriend";
        if ("alt".equals(variant)) {
            path = "entity/girlfriend_alt";
        } else {
            try {
                int n = Integer.parseInt(variant);
                if (n >= 1 && n <= GirlfriendSkins.TEXTURE_COUNT) path = "entity/girlfriend_" + n;
            } catch (NumberFormatException ignored) {
            }
        }
        Identifier tex = Identifier.fromNamespaceAndPath(GirlfriendMod.MOD_ID, path);
        return new PlayerSkin(new ClientAsset.ResourceTexture(tex), null, null, PlayerModelType.SLIM, true);
    }

    @Override
    protected void scale(AvatarRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        if (state instanceof GirlfriendRenderState g && g.sitting) poseStack.translate(0.0F, 0.6F, 0.0F);
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        return state.skin.body().texturePath();
    }
}
