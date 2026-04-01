package com.beckytidus.girlfriendmod.interaction;

import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.dialogue.HugAndHitResponses;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityInteractionHandler {
    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof GirlFriendEntity gf) {
                return handleGirlFriendInteraction(player, gf, hand);
            }
            return InteractionResult.PASS;
        });
    }

    public static InteractionResult handleGirlFriendInteraction(Player player, GirlFriendEntity girlfriend, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!girlfriend.canInteract(player)) return InteractionResult.PASS;
        boolean isOwner = girlfriend.getOwner() == player;
        if (player.getItemInHand(hand).isEmpty()) {
            if (player instanceof ServerPlayer spe) {
                spe.sendSystemMessage(girlfriend.getStatsDisplayText(), true);
            }
            if (player.isCrouching() && isOwner && !girlfriend.isAngeredAtOwner()) {
                handleDeepInteraction(player, girlfriend);
            } else {
                girlfriend.toggle();
            }
            girlfriend.setLastInteractionTick(player.level() instanceof ServerLevel sl ? sl.getGameTime() : 0);
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (isFood(stack)) {
            girlfriend.feedEntity(stack);
            girlfriend.setLastInteractionTick(player.level() instanceof ServerLevel sl ? sl.getGameTime() : 0);
            if (!player.isCreative()) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (isGift(stack)) {
            girlfriend.addAffection(5);
            girlfriend.setMoodLevel(Math.min(100, girlfriend.getMoodLevel() + 5));
            girlfriend.spawnHeartParticles();
            if (player instanceof ServerPlayer spe) {
                spe.sendSystemMessage(Component.literal("♥ " + girlfriend.getDisplayNameForChat() + ": " + getGiftResponse(girlfriend)));
            }
            girlfriend.setLastInteractionTick(player.level() instanceof ServerLevel sl ? sl.getGameTime() : 0);
            if (!player.isCreative()) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.NAME_TAG)) {
            String name = stack.getHoverName().getString();
            if (name.isEmpty() || name.equals("Name Tag")) name = "Girlfriend";
            girlfriend.setPlayerCustomName(name);
            girlfriend.setLastInteractionTick(player.level() instanceof ServerLevel sl ? sl.getGameTime() : 0);
            if (!player.isCreative()) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void handleDeepInteraction(Player player, GirlFriendEntity girlfriend) {
        int mood = girlfriend.getMoodLevel();
        int hunger = girlfriend.getHunger();
        float roll = girlfriend.level().getRandom().nextFloat();
        if (roll < 0.25f) girlfriend.triggerEmote(GirlFriendEntity.EMOTE_HUG, 50);
        else if (roll < 0.5f) girlfriend.triggerEmote(GirlFriendEntity.EMOTE_KISS, 40);
        girlfriend.addAffection(2);
        girlfriend.setMoodLevel(Math.min(100, mood + 3));
        if (hunger < 40) {
            if (player.level().isClientSide()) return;
            if (player instanceof ServerPlayer spe) {
                spe.sendSystemMessage(Component.literal("♥ " + girlfriend.getDisplayNameForChat() + ": I'm a little hungry..."));
            }
        }
        girlfriend.spawnHeartParticles();
        Level world = player.level();
        if (!world.isClientSide() && player instanceof ServerPlayer spe) {
            DelayedChatReply.sendImmediate(spe, girlfriend, HugAndHitResponses.pickHug(girlfriend), false);
        }
    }

    private static String getGiftResponse(GirlFriendEntity girlfriend) {
        String[] responses = {
            "You remembered what I like! Thank you!",
            "For me? You're too kind!",
            "I'll treasure this. Really.",
            "This is so thoughtful of you.",
            "You always know how to make me smile."
        };
        return responses[girlfriend.level().getRandom().nextInt(responses.length)];
    }

    private static boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.GOLDEN_APPLE) ||
               stack.is(Items.WHEAT) || stack.is(Items.BREAD) ||
               stack.is(Items.CARROT) || stack.is(Items.POTATO) ||
               stack.is(Items.BAKED_POTATO) || stack.is(Items.PUMPKIN_PIE) ||
               stack.is(Items.CAKE) || stack.is(Items.HONEY_BOTTLE) ||
               stack.is(Items.MELON_SLICE) || stack.is(Items.BEEF) ||
               stack.is(Items.COOKED_BEEF) || stack.is(Items.PORKCHOP) ||
               stack.is(Items.COOKED_PORKCHOP) || stack.is(Items.CHICKEN) ||
               stack.is(Items.COOKED_CHICKEN);
    }

    private static boolean isGift(ItemStack stack) {
        return stack.is(Items.POPPY) || stack.is(Items.DANDELION) || stack.is(Items.BLUE_ORCHID) ||
               stack.is(Items.ALLIUM) || stack.is(Items.AZURE_BLUET) || stack.is(Items.RED_TULIP) ||
               stack.is(Items.ORANGE_TULIP) || stack.is(Items.PINK_TULIP) || stack.is(Items.WHITE_TULIP) ||
               stack.is(Items.LILAC) || stack.is(Items.ROSE_BUSH) || stack.is(Items.PEONY) ||
               stack.is(Items.SUNFLOWER) || stack.is(Items.CORNFLOWER) || stack.is(Items.LILY_OF_THE_VALLEY) ||
               stack.is(Items.TORCHFLOWER) || stack.is(Items.PITCHER_PLANT) || stack.is(Items.EMERALD) ||
               stack.is(Items.DIAMOND) || stack.is(Items.AMETHYST_SHARD);
    }
}
