package com.beckytidus.girlfriendmod.chat;

import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.dialogue.DialogueData;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;

public final class GirlfriendChatHandler {
    private static final double RESPONSE_RANGE = 16.0;

    private GirlfriendChatHandler() {
    }

    public static void register() {
        ServerMessageEvents.CHAT_MESSAGE.register(GirlfriendChatHandler::onChatMessage);
    }

    private static void onChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
        List<GirlFriendEntity> nearby = sender.level().getEntitiesOfClass(
            GirlFriendEntity.class,
            sender.getBoundingBox().inflate(RESPONSE_RANGE),
            g -> g.getOwner() == sender && !g.isAngeredAtOwner()
        );
        if (nearby.isEmpty()) return;
        GirlFriendEntity gf = nearby.get(sender.level().getRandom().nextInt(nearby.size()));
        String content = message.signedContent().toLowerCase(Locale.ROOT).trim();
        if (content.isEmpty()) return;
        float roll = sender.level().getRandom().nextFloat();
        String reply = null;
        String customName = gf.getPlayerCustomName();
        if (!customName.isEmpty() && content.contains(customName.toLowerCase(Locale.ROOT))) {
            reply = DialogueData.pickCallReply(roll);
        }
        if (reply == null) {
            reply = DialogueData.pickReply(content, gf, roll);
        }
        if (reply != null) {
            DelayedChatReply.schedule(sender, gf, reply);
        }
    }
}
