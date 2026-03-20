package com.beckytidus.girlfriendmod.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

@Environment(EnvType.CLIENT)
public class GirlFriendEntityModel extends BipedEntityModel<GirlFriendEntityRenderState> {
    private final PlayerEntityModel inner;

    public GirlFriendEntityModel(ModelPart root, boolean thinArms) {
        super(root);
        this.inner = new PlayerEntityModel(root, thinArms);
    }

    @Override
    public void setAngles(GirlFriendEntityRenderState state) {
        inner.setAngles(state);
    }
}
