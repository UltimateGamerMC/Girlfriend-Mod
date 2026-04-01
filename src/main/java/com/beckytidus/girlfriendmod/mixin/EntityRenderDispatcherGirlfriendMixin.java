package com.beckytidus.girlfriendmod.mixin;

import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherGirlfriendMixin {
    @Shadow @Final private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("HEAD"), cancellable = true)
    private void girlfriendMod$useRendererByEntityType(EntityRenderState state, CallbackInfoReturnable<EntityRenderer<?, ? super EntityRenderState>> cir) {
        if (state.entityType == EntityRegistry.GIRLFRIEND) {
            EntityRenderer<?, ?> r = renderers.get(state.entityType);
            if (r != null) {
                cir.setReturnValue((EntityRenderer<?, ? super EntityRenderState>) r);
            }
        }
    }
}
