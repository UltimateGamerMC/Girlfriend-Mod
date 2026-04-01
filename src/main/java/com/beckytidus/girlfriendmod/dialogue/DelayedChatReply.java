package com.beckytidus.girlfriendmod.dialogue;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class DelayedChatReply {
    private static final int TYPING_DELAY_TICKS = 20;
    private static final List<PendingReply> pending = new ArrayList<>();

    public static void schedule(ServerPlayer player, GirlFriendEntity gf, String message) {
        schedule(player, gf, message, true);
    }

    public static void schedule(ServerPlayer player, GirlFriendEntity gf, String message, boolean runChatResponse) {
        MinecraftServer server = player.level().getServer();
        if (server == null) return;
        pending.add(new PendingReply(server.getTickCount() + TYPING_DELAY_TICKS, player, gf, message, runChatResponse));
    }

    public static void sendImmediate(ServerPlayer player, GirlFriendEntity gf, String message, boolean runChatResponse) {
        if (player.hasDisconnected() || !gf.isAlive()) return;
        if (gf.level() != player.level()) return;
        player.sendSystemMessage(Component.literal("♥ " + gf.getDisplayNameForChat() + ": " + message));
        if (runChatResponse) gf.onChatResponse();
    }

    public static void tick(MinecraftServer server) {
        int now = server.getTickCount();
        Iterator<PendingReply> it = pending.iterator();
        while (it.hasNext()) {
            PendingReply p = it.next();
            if (now >= p.dueTick) {
                it.remove();
                if (p.player.hasDisconnected() || !p.gf.isAlive()) continue;
                if (p.gf.level() != p.player.level()) continue;
                p.player.sendSystemMessage(Component.literal("♥ " + p.gf.getDisplayNameForChat() + ": " + p.message));
                if (p.runChatResponse) p.gf.onChatResponse();
            }
        }
    }

    private record PendingReply(int dueTick, ServerPlayer player, GirlFriendEntity gf, String message, boolean runChatResponse) {}
}
