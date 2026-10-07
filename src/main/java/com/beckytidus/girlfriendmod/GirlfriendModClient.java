package com.beckytidus.girlfriendmod;

import com.beckytidus.girlfriendmod.client.render.GirlFriendEntityRenderer;
import com.beckytidus.girlfriendmod.client.screen.GirlfriendScreen;
import com.beckytidus.girlfriendmod.interaction.EntityInteractionHandler;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import com.beckytidus.girlfriendmod.client.hud.GirlfriendHud;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;

public class GirlfriendModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntityRegistry.GIRLFRIEND, GirlFriendEntityRenderer::new);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(GirlfriendMod.MOD_ID, "bond_card"), GirlfriendHud::extract);
        EntityInteractionHandler.openMenu = gf -> Minecraft.getInstance().gui.setScreen(new GirlfriendScreen(gf));
    }
}
