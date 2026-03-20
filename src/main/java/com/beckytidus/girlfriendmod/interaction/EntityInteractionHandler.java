package com.beckytidus.girlfriendmod.interaction;

import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.dialogue.HugAndHitResponses;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;

public class EntityInteractionHandler {
    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof GirlFriendEntity gf) {
                return handleGirlFriendInteraction(player, gf, hand);
            }
            return ActionResult.PASS;
        });
    }

    public static ActionResult handleGirlFriendInteraction(PlayerEntity player, GirlFriendEntity girlfriend, Hand hand) {
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
        if (!girlfriend.canInteract(player)) return ActionResult.PASS;
        boolean isOwner = girlfriend.getOwner() == player;
        if (player.getStackInHand(hand).isEmpty()) {
            if (player instanceof ServerPlayerEntity spe) {
                spe.sendMessage(girlfriend.getStatsDisplayText(), true);
            }
            if (player.isSneaking() && isOwner && !girlfriend.isAngeredAtOwner()) {
                handleDeepInteraction(player, girlfriend);
            } else {
                girlfriend.toggle();
            }
            girlfriend.setLastInteractionTick(player.getEntityWorld().getTime());
            return ActionResult.SUCCESS;
        }

        ItemStack stack = player.getStackInHand(hand);
        if (isFood(stack)) {
            girlfriend.feedEntity(stack);
            girlfriend.setLastInteractionTick(player.getEntityWorld().getTime());
            if (!player.isCreative()) stack.decrement(1);
            return ActionResult.SUCCESS;
        }
        if (isGift(stack)) {
            girlfriend.addAffection(5);
            girlfriend.setMoodLevel(Math.min(100, girlfriend.getMoodLevel() + 5));
            girlfriend.spawnHeartParticles();
            player.sendMessage(Text.literal("♥ " + girlfriend.getDisplayNameForChat() + ": " + getGiftResponse(girlfriend)), false);
            girlfriend.setLastInteractionTick(player.getEntityWorld().getTime());
            if (!player.isCreative()) stack.decrement(1);
            return ActionResult.SUCCESS;
        }
        if (stack.isOf(Items.NAME_TAG)) {
            String name = stack.getName().getString();
            if (name.isEmpty() || name.equals("Name Tag")) name = "Girlfriend";
            girlfriend.setPlayerCustomName(name);
            girlfriend.setLastInteractionTick(player.getEntityWorld().getTime());
            if (!player.isCreative()) stack.decrement(1);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static void handleDeepInteraction(PlayerEntity player, GirlFriendEntity girlfriend) {
        int mood = girlfriend.getMoodLevel();
        int hunger = girlfriend.getHunger();
        float roll = girlfriend.getEntityWorld().getRandom().nextFloat();
        if (roll < 0.25f) girlfriend.triggerEmote(GirlFriendEntity.EMOTE_HUG, 50);
        else if (roll < 0.5f) girlfriend.triggerEmote(GirlFriendEntity.EMOTE_KISS, 40);
        girlfriend.addAffection(2);
        girlfriend.setMoodLevel(Math.min(100, mood + 3));
        if (hunger < 40) {
            if (player.getEntityWorld().isClient()) return;
            if (player instanceof ServerPlayerEntity spe) {
                spe.sendMessage(Text.literal("♥ " + girlfriend.getDisplayNameForChat() + ": I'm a little hungry..."), false);
            }
        }
        girlfriend.spawnHeartParticles();
        World world = player.getEntityWorld();
        if (!world.isClient() && player instanceof ServerPlayerEntity spe) {
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
        return responses[girlfriend.getEntityWorld().getRandom().nextInt(responses.length)];
    }

    private static boolean isFood(ItemStack stack) {
        return stack.isOf(Items.APPLE) || stack.isOf(Items.GOLDEN_APPLE) ||
               stack.isOf(Items.WHEAT) || stack.isOf(Items.BREAD) ||
               stack.isOf(Items.CARROT) || stack.isOf(Items.POTATO) ||
               stack.isOf(Items.BAKED_POTATO) || stack.isOf(Items.PUMPKIN_PIE) ||
               stack.isOf(Items.CAKE) || stack.isOf(Items.HONEY_BOTTLE) ||
               stack.isOf(Items.MELON_SLICE) || stack.isOf(Items.BEEF) ||
               stack.isOf(Items.COOKED_BEEF) || stack.isOf(Items.PORKCHOP) ||
               stack.isOf(Items.COOKED_PORKCHOP) || stack.isOf(Items.CHICKEN) ||
               stack.isOf(Items.COOKED_CHICKEN);
    }

    private static boolean isGift(ItemStack stack) {
        return stack.isOf(Items.POPPY) || stack.isOf(Items.DANDELION) || stack.isOf(Items.BLUE_ORCHID) ||
               stack.isOf(Items.ALLIUM) || stack.isOf(Items.AZURE_BLUET) || stack.isOf(Items.RED_TULIP) ||
               stack.isOf(Items.ORANGE_TULIP) || stack.isOf(Items.PINK_TULIP) || stack.isOf(Items.WHITE_TULIP) ||
               stack.isOf(Items.LILAC) || stack.isOf(Items.ROSE_BUSH) || stack.isOf(Items.PEONY) ||
               stack.isOf(Items.SUNFLOWER) || stack.isOf(Items.CORNFLOWER) || stack.isOf(Items.LILY_OF_THE_VALLEY) ||
               stack.isOf(Items.TORCHFLOWER) || stack.isOf(Items.PITCHER_PLANT) || stack.isOf(Items.EMERALD) ||
               stack.isOf(Items.DIAMOND) || stack.isOf(Items.AMETHYST_SHARD);
    }
}
