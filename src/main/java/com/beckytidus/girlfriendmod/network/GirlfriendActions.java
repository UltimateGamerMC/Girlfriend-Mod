package com.beckytidus.girlfriendmod.network;

import com.beckytidus.girlfriendmod.dialogue.HugAndHitResponses;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.util.GirlfriendText;
import com.beckytidus.girlfriendmod.util.RelationshipTier;
import com.beckytidus.girlfriendmod.util.SkinService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

public final class GirlfriendActions {
    private static final double MAX_DISTANCE = 10.0;

    private GirlfriendActions() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(GirlfriendActionPayload.TYPE, GirlfriendActionPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(GirlfriendActionPayload.TYPE, (payload, context) -> handle(context.player(), payload));
    }

    private static void handle(ServerPlayer player, GirlfriendActionPayload payload) {
        if (!(player.level().getEntity(payload.entityId()) instanceof GirlFriendEntity gf)) return;
        if (!gf.isOwnedBy(player) || gf.distanceToSqr(player) > MAX_DISTANCE * MAX_DISTANCE) return;
        long now = ((ServerLevel) player.level()).getGameTime();
        if (!gf.canInteract(player)) return;
        gf.setLastInteractionTick(now);

        switch (payload.action()) {
            case "hug" -> affection(gf, GirlFriendEntity.EMOTE_HUG, HugAndHitResponses.pickHug(gf), 2);
            case "headpat" -> affection(gf, GirlFriendEntity.EMOTE_NOD, HugAndHitResponses.pickHeadpat(gf), 1);
            case "kiss" -> {
                if (gf.getRelationshipTier().atLeast(RelationshipTier.DATING)) {
                    affection(gf, GirlFriendEntity.EMOTE_KISS, "*kisses you* ...Hehe. Again?", 3);
                } else {
                    gf.triggerEmote(GirlFriendEntity.EMOTE_CROUCH, 20);
                    gf.say("W-wait! Let's go on a few more adventures first~ *blushes*");
                }
            }
            case "dance" -> {
                gf.triggerEmote(GirlFriendEntity.EMOTE_DANCE, 80);
                gf.say("Dance with me! *spins*");
                gf.playCute(SoundEvents.NOTE_BLOCK_BELL.value(), 1.2f);
                gf.setMoodLevel(gf.getMoodLevel() + 5);
            }
            case "follow" -> gf.toggle();
            case "sit" -> gf.toggleSit();
            case "forgive" -> gf.forgive();
            case "friendlyfire" -> {
                gf.setFriendlyFire(!gf.isFriendlyFire());
                player.sendSystemMessage(GirlfriendText.info(gf.isFriendlyFire()
                    ? "Friendly fire ON: your hits can hurt " + gf.getDisplayNameForChat() + " (she'll sulk, never fight back)."
                    : "Friendly fire OFF: your swings won't hurt " + gf.getDisplayNameForChat() + "."));
            }
            case "rename" -> {
                String name = payload.argument().strip();
                if (!name.isEmpty()) {
                    gf.setPlayerCustomName(name);
                    gf.say("\"" + gf.getDisplayNameForChat() + "\"... I love it!");
                    gf.spawnHeartParticles();
                }
            }
            case "skin_variant" -> {
                gf.clearSkinProfile();
                gf.setTextureVariant(payload.argument());
            }
            case "skin_player" -> SkinService.applyPlayerSkin(player, gf, payload.argument());
            case "skin_reset" -> gf.clearSkinProfile();
            default -> {
            }
        }
    }

    private static void affection(GirlFriendEntity gf, int emote, String line, int amount) {
        if (gf.isSulking()) {
            gf.say("*turns away* I'm still mad at you...");
            return;
        }
        gf.triggerEmote(emote, 40);
        gf.say(line);
        gf.addAffection(amount);
        gf.addRelationship(1);
        gf.setMoodLevel(gf.getMoodLevel() + 3);
        gf.spawnHeartParticles();
        gf.playCute(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5f);
        if (gf.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, gf.getX(), gf.getY() + 2.1, gf.getZ(), 2, 0.3, 0.1, 0.3, 0.0);
        }
    }
}
