package com.beckytidus.girlfriendmod.interaction;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.util.GirlfriendText;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;

import java.util.function.Consumer;

public class EntityInteractionHandler {
    /** Set by the client entrypoint; opens the girlfriend menu. No-op on dedicated servers. */
    public static Consumer<GirlFriendEntity> openMenu = gf -> {
    };

    public static InteractionResult handleGirlFriendInteraction(Player player, GirlFriendEntity girlfriend) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean client = player.level().isClientSide();

        if (!girlfriend.hasOwner()) {
            if (!client) {
                girlfriend.setOwner(player);
                girlfriend.say("Oh! Hi there~ I'm " + girlfriend.getDisplayNameForChat() + ". Can I... tag along with you?");
                girlfriend.spawnHeartParticles();
            }
            return InteractionResult.SUCCESS;
        }
        if (!girlfriend.isOwnedBy(player)) {
            if (!client && player instanceof ServerPlayer sp) {
                sp.sendSystemMessage(GirlfriendText.info(girlfriend.getDisplayNameForChat() + " smiles politely. She's already taken."), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            if (player.isShiftKeyDown()) {
                if (client) openMenu.accept(girlfriend);
                return InteractionResult.SUCCESS;
            }
            if (!client && girlfriend.canInteract(player)) {
                girlfriend.toggle();
                girlfriend.setLastInteractionTick(player.level().getGameTime());
            }
            return InteractionResult.SUCCESS;
        }

        if (client) {
            return isFood(stack) || isGift(stack) || stack.is(Items.NAME_TAG) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!girlfriend.canInteract(player)) return InteractionResult.PASS;
        girlfriend.setLastInteractionTick(player.level().getGameTime());

        if (isFood(stack)) {
            if (girlfriend.getHunger() >= 100) {
                girlfriend.say("I'm stuffed! Maybe later~");
                return InteractionResult.SUCCESS;
            }
            girlfriend.feedEntity(stack);
            if (girlfriend.isSulking()) girlfriend.forgive();
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        if (isGift(stack)) {
            girlfriend.addAffection(5);
            girlfriend.addRelationship(2);
            girlfriend.setMoodLevel(girlfriend.getMoodLevel() + 8);
            girlfriend.triggerEmote(GirlFriendEntity.EMOTE_DANCE, 40);
            girlfriend.spawnHeartParticles();
            girlfriend.say(giftResponse(girlfriend, stack));
            if (girlfriend.isSulking()) girlfriend.forgive();
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.NAME_TAG) && stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            girlfriend.setPlayerCustomName(stack.getHoverName().getString());
            girlfriend.say("\"" + girlfriend.getDisplayNameForChat() + "\"? I love it!");
            stack.consume(1, player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static String giftResponse(GirlFriendEntity gf, ItemStack stack) {
        if (stack.is(net.minecraft.tags.BlockItemTags.FLOWERS.item())) {
            String[] lines = {"Flowers?! For me? *tucks one behind her ear*", "They're beautiful... like you.", "I'm going to press these and keep them forever!"};
            return lines[gf.getRandom().nextInt(lines.length)];
        }
        if (stack.is(Items.DIAMOND) || stack.is(Items.EMERALD) || stack.is(Items.AMETHYST_SHARD)) {
            String[] lines = {"Is this... shiny?! You're spoiling me!", "It sparkles like your eyes~", "I'll make it into a necklace!"};
            return lines[gf.getRandom().nextInt(lines.length)];
        }
        return "For me? You're too sweet!";
    }

    private static boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.GOLDEN_APPLE) || stack.is(Items.BREAD) || stack.is(Items.COOKIE)
            || stack.is(Items.CARROT) || stack.is(Items.BAKED_POTATO) || stack.is(Items.PUMPKIN_PIE) || stack.is(Items.CAKE)
            || stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.GLOW_BERRIES)
            || stack.is(Items.COOKED_BEEF) || stack.is(Items.COOKED_PORKCHOP) || stack.is(Items.COOKED_CHICKEN)
            || stack.is(Items.COOKED_MUTTON) || stack.is(Items.COOKED_SALMON) || stack.is(Items.COOKED_COD);
    }

    private static boolean isGift(ItemStack stack) {
        return stack.is(net.minecraft.tags.BlockItemTags.FLOWERS.item()) || stack.is(Items.EMERALD) || stack.is(Items.DIAMOND) || stack.is(Items.AMETHYST_SHARD);
    }
}
