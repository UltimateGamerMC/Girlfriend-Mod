package com.beckytidus.girlfriendmod.chat;

import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.dialogue.DialogueData;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

public final class GirlfriendChatHandler {
    private static final double RESPONSE_RANGE = 24.0;
    /** Unprompted replies are rate limited so she doesn't answer every single message. */
    private static final int REPLY_COOLDOWN_TICKS = 200;
    private static final Pattern AFFECTION = Pattern.compile("(?<![a-z])(i love you|ily|love you|<3|i missed you|you're cute|youre cute)(?![a-z])");
    private static final Map<UUID, Integer> LAST_REPLY = new WeakHashMap<>();

    private GirlfriendChatHandler() {
    }

    public static void register() {
        ServerMessageEvents.CHAT_MESSAGE.register(GirlfriendChatHandler::onChatMessage);
    }

    private static void onChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
        String content = message.signedContent().toLowerCase(Locale.ROOT).trim();
        if (content.isEmpty()) return;
        List<GirlFriendEntity> nearby = sender.level().getEntitiesOfClass(
            GirlFriendEntity.class,
            sender.getBoundingBox().inflate(RESPONSE_RANGE),
            g -> g.isOwnedBy(sender) && !g.isSulking()
        );
        if (nearby.isEmpty()) return;

        // Talking to one girlfriend by name only gets her attention.
        GirlFriendEntity gf = nearby.stream()
            .filter(g -> !g.getPlayerCustomName().isEmpty() && Pattern.compile("(?<![a-z])" + Pattern.quote(g.getPlayerCustomName().toLowerCase(Locale.ROOT)) + "(?![a-z])").matcher(content).find())
            .findFirst()
            .orElseGet(() -> nearby.stream().min(Comparator.comparingDouble(g -> g.distanceToSqr(sender))).orElseThrow());
        boolean addressed = !gf.getPlayerCustomName().isEmpty() && content.contains(gf.getPlayerCustomName().toLowerCase(Locale.ROOT));

        String command = DialogueData.commandIn(content);
        if (command != null && (addressed || nearby.size() == 1)) {
            boolean wantFollow = "follow".equals(command);
            if (gf.isFollowingOwner() != wantFollow) {
                sender.level().getServer().execute(gf::toggle);
                return;
            }
        }

        int now = sender.level().getServer().getTickCount();
        boolean affectionate = AFFECTION.matcher(content).find();
        Integer last = LAST_REPLY.get(gf.getUUID());
        if (!addressed && !affectionate && last != null && now - last < REPLY_COOLDOWN_TICKS) return;

        float roll = sender.level().getRandom().nextFloat();
        String reply = DialogueData.pickReply(content, gf, roll);
        if (reply == null && addressed) reply = DialogueData.pickCallReply(roll);
        if (reply == null) return;
        LAST_REPLY.put(gf.getUUID(), now);
        DelayedChatReply.schedule(sender, gf, reply);
    }
}
