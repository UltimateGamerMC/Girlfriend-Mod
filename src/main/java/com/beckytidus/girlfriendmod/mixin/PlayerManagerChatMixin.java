package com.beckytidus.girlfriendmod.mixin;

import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.dialogue.DialogueData;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Locale;

@Mixin(PlayerManager.class)
public class PlayerManagerChatMixin {
    private static final double RESPONSE_RANGE = 16.0;

    @Inject(method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At("TAIL"))
    private void onChatBroadcast(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
        if (sender == null) return;
        List<GirlFriendEntity> nearby = sender.getEntityWorld().getEntitiesByClass(
            GirlFriendEntity.class,
            sender.getBoundingBox().expand(RESPONSE_RANGE),
            g -> g.getOwner() == sender && !g.isAngeredAtOwner()
        );
        if (nearby.isEmpty()) return;
        GirlFriendEntity gf = nearby.get(sender.getEntityWorld().getRandom().nextInt(nearby.size()));
        String content = message.getSignedContent().toLowerCase(Locale.ROOT).trim();
        if (content.isEmpty()) return;
        float roll = sender.getEntityWorld().getRandom().nextFloat();
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
