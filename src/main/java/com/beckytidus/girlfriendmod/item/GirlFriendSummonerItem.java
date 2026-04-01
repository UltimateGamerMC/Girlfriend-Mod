package com.beckytidus.girlfriendmod.item;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class GirlFriendSummonerItem extends Item {
    public GirlFriendSummonerItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (EntityRegistry.GIRLFRIEND != null) {
                GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, level);
                girlfriend.setPos(user.getX(), user.getY(), user.getZ());
                girlfriend.setOwner(user);
                girlfriend.setPlayerCustomName(FemaleNames.pickRandom(level.getRandom()));
                girlfriend.setTextureVariant(GirlfriendSkins.pickRandomTextureVariant(level.getRandom()));
                level.addFreshEntity(girlfriend);

                if (user instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.literal("GirlFriend has been summoned!"));
                }

                if (!user.isCreative()) {
                    user.getItemInHand(hand).shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> textConsumer, TooltipFlag type) {
        textConsumer.accept(Component.literal("Summons a girlfriend (random texture 1-20). Stats: Lv, Mood, Affection, Hunger, HP"));
    }
}
