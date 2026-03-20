package com.beckytidus.girlfriendmod.dialogue;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class DelayedChatReply {
    private static final int TYPING_DELAY_TICKS = 20;
    private static final List<PendingReply> pending = new ArrayList<>();

    public static void schedule(ServerPlayerEntity player, GirlFriendEntity gf, String message) {
        schedule(player, gf, message, true);
    }

    public static void schedule(ServerPlayerEntity player, GirlFriendEntity gf, String message, boolean runChatResponse) {
        if (!(gf.getEntityWorld() instanceof ServerWorld sw)) return;
        MinecraftServer server = sw.getServer();
        pending.add(new PendingReply(server.getTicks() + TYPING_DELAY_TICKS, player, gf, message, runChatResponse));
    }

    public static void sendImmediate(ServerPlayerEntity player, GirlFriendEntity gf, String message, boolean runChatResponse) {
        if (player.isDisconnected() || !gf.isAlive()) return;
        if (gf.getEntityWorld() != player.getEntityWorld()) return;
        player.sendMessage(Text.literal("♥ " + gf.getDisplayNameForChat() + ": " + message), false);
        if (runChatResponse) gf.onChatResponse();
    }

    public static void tick(MinecraftServer server) {
        int now = server.getTicks();
        Iterator<PendingReply> it = pending.iterator();
        while (it.hasNext()) {
            PendingReply p = it.next();
            if (now >= p.dueTick) {
                it.remove();
                if (p.player.isDisconnected() || !p.gf.isAlive()) continue;
                if (p.gf.getEntityWorld() != p.player.getEntityWorld()) continue;
                p.player.sendMessage(Text.literal("♥ " + p.gf.getDisplayNameForChat() + ": " + p.message), false);
                if (p.runChatResponse) p.gf.onChatResponse();
            }
        }
    }

    private record PendingReply(int dueTick, ServerPlayerEntity player, GirlFriendEntity gf, String message, boolean runChatResponse) {}
}
