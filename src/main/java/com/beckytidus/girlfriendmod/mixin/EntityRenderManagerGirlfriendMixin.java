package com.beckytidus.girlfriendmod.mixin;

import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderManager.class)
public abstract class EntityRenderManagerGirlfriendMixin {
    @Shadow @Final private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer(Lnet/minecraft/client/render/entity/state/EntityRenderState;)Lnet/minecraft/client/render/entity/EntityRenderer;", at = @At("HEAD"), cancellable = true)
    private void girlfriendMod$useRendererByEntityType(EntityRenderState state, CallbackInfoReturnable<EntityRenderer<?, ? super EntityRenderState>> cir) {
        if (state.entityType == EntityRegistry.GIRLFRIEND) {
            EntityRenderer<?, ?> r = renderers.get(state.entityType);
            if (r != null) {
                cir.setReturnValue((EntityRenderer<?, ? super EntityRenderState>) r);
            }
        }
    }
}
