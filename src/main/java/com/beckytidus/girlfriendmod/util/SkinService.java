package com.beckytidus.girlfriendmod.util;

import com.beckytidus.girlfriendmod.GirlfriendMod;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Looks a Minecraft account up on the server and stores the fully resolved profile (with its
 * texture signature) on the girlfriend, so every client renders the real skin instead of
 * guessing from a bare username and falling back to Steve/Alex.
 */
public final class SkinService {
    private SkinService() {
    }

    public static void applyPlayerSkin(ServerPlayer requester, GirlFriendEntity gf, String username) {
        String name = username.strip();
        if (!StringUtil.isValidPlayerName(name) || name.isEmpty()) {
            requester.sendSystemMessage(GirlfriendText.info("\"" + name + "\" isn't a valid Minecraft username."));
            return;
        }
        MinecraftServer server = requester.level().getServer();
        requester.sendSystemMessage(GirlfriendText.info("Looking up " + name + "'s skin..."));
        CompletableFuture.supplyAsync(() -> server.services().profileResolver().fetchByName(name), Util.nonCriticalIoPool())
            .exceptionally(error -> {
                GirlfriendMod.LOGGER.warn("Skin lookup for {} failed", name, error);
                return Optional.empty();
            })
            .thenAcceptAsync(result -> apply(requester, gf, name, result), server);
    }

    private static void apply(ServerPlayer requester, GirlFriendEntity gf, String name, Optional<GameProfile> result) {
        if (!gf.isAlive()) return;
        if (result.isEmpty()) {
            requester.sendSystemMessage(GirlfriendText.info("Couldn't find a Minecraft account named " + name + " (or the skin servers are offline)."));
            return;
        }
        GameProfile profile = result.get();
        gf.setSkinProfile(profile.name(), ResolvableProfile.createResolved(profile));
        gf.say("New look! Do you like it? *twirls*");
        gf.spawnHeartParticles();
    }
}
