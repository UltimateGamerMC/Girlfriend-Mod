package com.beckytidus.girlfriendmod.interaction;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemUseHandler {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);

            if (stack.getItem() == ItemRegistry.GIRLFRIEND_SUMMONER && !world.isClientSide()) {
                GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, world);
                girlfriend.setPos(player.getX(), player.getY(), player.getZ());
                girlfriend.setOwner(player);
                world.addFreshEntity(girlfriend);

                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.literal("♥ GirlFriend has been summoned!"));
                }

                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        });
    }
}
