package com.beckytidus.girlfriendmod.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;

import java.util.function.Consumer;

public class GirlFriendSummonerItem extends Item {
    public GirlFriendSummonerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            if (EntityRegistry.GIRLFRIEND != null) {
                GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, world);
                girlfriend.setPosition(user.getX(), user.getY(), user.getZ());
                girlfriend.setOwner(user);
                girlfriend.setPlayerCustomName(FemaleNames.pickRandom(world.getRandom()));
                girlfriend.setTextureVariant(GirlfriendSkins.pickRandomTextureVariant(world.getRandom()));
                world.spawnEntity(girlfriend);

                user.sendMessage(Text.literal("GirlFriend has been summoned!"), false);

                if (!user.isCreative()) {
                    user.getStackInHand(hand).decrement(1);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.literal("Summons a girlfriend (random texture 1-20). Stats: Lv, ♥Mood, Affection, Hunger, HP"));
    }
}
