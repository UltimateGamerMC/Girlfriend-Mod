package com.beckytidus.girlfriendmod.mixin;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiGirlfriendStatsMixin {
    private static final int GIRLFRIEND_LOOK_RANGE = 10;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void girlfriendMod$renderGirlfriendStats(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Entity lookedAt = minecraft.crosshairPickEntity;
        if (!(lookedAt instanceof GirlFriendEntity) && minecraft.getCameraEntity() != null) {
            lookedAt = DebugRenderer.getTargetedEntity(minecraft.getCameraEntity(), GIRLFRIEND_LOOK_RANGE).orElse(null);
        }
        if (lookedAt instanceof GirlFriendEntity gf && gf.isAlive()) {
            int w = minecraft.getWindow().getGuiScaledWidth();
            int h = minecraft.getWindow().getGuiScaledHeight();
            int y = h / 2 - 60;
            String stats = gf.getStatsDisplayText().getString();
            graphics.text(minecraft.font, stats, (w - minecraft.font.width(stats)) / 2, y, 0xFFFFFF, true);
            String name = gf.getPlayerCustomName().isEmpty() ? "Girlfriend" : gf.getPlayerCustomName();
            graphics.text(minecraft.font, name, (w - minecraft.font.width(name)) / 2, y - 10, 0xFFAAFF, true);
        }
    }
}
