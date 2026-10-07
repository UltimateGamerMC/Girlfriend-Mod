package com.beckytidus.girlfriendmod.item;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class GirlFriendSummonerItem extends Item {
    private static final String[] INTRODUCTIONS = {
        "Hi! I'm %s. I've been waiting to meet you~",
        "Oh! Hello! I'm %s... nice to meet you. *waves shyly*",
        "Hey there~ I'm %s. Mind if I stick around?",
        "*gasps* It's you! I'm %s. Let's go on adventures together!",
    };

    public GirlFriendSummonerItem(Properties settings) {
        super(settings);
    }

    public static GirlFriendEntity summon(ServerLevel level, Player owner) {
        GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, level);
        girlfriend.snapTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot() + 180, 0);
        girlfriend.setOwner(owner);
        girlfriend.setPlayerCustomName(FemaleNames.pickRandom(level.getRandom()));
        girlfriend.setTextureVariant(GirlfriendSkins.pickRandomTextureVariant(level.getRandom()));
        level.addFreshEntity(girlfriend);
        level.sendParticles(ParticleTypes.HEART, owner.getX(), owner.getY() + 1, owner.getZ(), 12, 0.6, 0.6, 0.6, 0.0);
        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0f, 1.2f);
        girlfriend.triggerEmote(GirlFriendEntity.EMOTE_WAVE, 40);
        girlfriend.say(String.format(INTRODUCTIONS[level.getRandom().nextInt(INTRODUCTIONS.length)], girlfriend.getDisplayNameForChat()));
        return girlfriend;
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            summon(serverLevel, user);
            user.getItemInHand(hand).consume(1, user);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag type) {
        out.accept(Component.literal("Use to meet your new companion ♥").withStyle(ChatFormatting.LIGHT_PURPLE));
        out.accept(Component.literal("Sneak + right-click her to open her menu").withStyle(ChatFormatting.GRAY));
    }
}
