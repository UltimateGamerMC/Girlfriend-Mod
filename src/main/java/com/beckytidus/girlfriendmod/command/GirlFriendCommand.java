package com.beckytidus.girlfriendmod.command;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.item.GirlFriendSummonerItem;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import com.beckytidus.girlfriendmod.util.GirlfriendText;
import com.beckytidus.girlfriendmod.util.SkinService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public class GirlFriendCommand {
    private static final double FIND_RANGE = 128.0;
    private static final SimpleCommandExceptionType NONE_NEARBY = new SimpleCommandExceptionType(Component.literal("You don't have a girlfriend nearby. Use /girlfriend summon or a Girlfriend Summoner!"));

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerGirlFriendCommand(dispatcher));
    }

    private static List<GirlFriendEntity> owned(ServerPlayer player) {
        return player.level().getEntitiesOfClass(GirlFriendEntity.class, player.getBoundingBox().inflate(FIND_RANGE), g -> g.isOwnedBy(player));
    }

    private static GirlFriendEntity closest(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return owned(player).stream().min(Comparator.comparingDouble(g -> g.distanceToSqr(player))).orElseThrow(NONE_NEARBY::create);
    }

    private static void reply(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> GirlfriendText.info(message), false);
    }

    private static void registerGirlFriendCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("girlfriend")
            .executes(context -> {
                reply(context, "/girlfriend summon | stats | list | name <name> | skin <username|reset> | texture <1-20> | friendlyfire <true|false>");
                reply(context, "Sneak + right-click her for the menu. Feed her, give her flowers, talk to her in chat!");
                return 1;
            })
            .then(Commands.literal("summon").executes(context -> {
                GirlFriendSummonerItem.summon(context.getSource().getLevel(), context.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("give").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(context -> {
                ServerPlayer target = context.getSource().getPlayerOrException();
                ItemStack stack = new ItemStack(ItemRegistry.GIRLFRIEND_SUMMONER);
                if (!target.getInventory().add(stack)) target.drop(stack, false, Prediction.SERVER_ONLY);
                reply(context, "Gave a Girlfriend Summoner to " + target.getName().getString());
                return 1;
            }))
            .then(Commands.literal("stats").executes(context -> {
                context.getSource().sendSystemMessage(closest(context).getStatsDisplayText());
                return 1;
            }))
            .then(Commands.literal("list").executes(context -> {
                List<GirlFriendEntity> list = owned(context.getSource().getPlayerOrException());
                reply(context, "Girlfriends nearby: " + list.size());
                list.forEach(gf -> context.getSource().sendSystemMessage(gf.getStatsDisplayText()));
                return list.size();
            }))
            .then(Commands.literal("name").then(Commands.argument("name", StringArgumentType.greedyString()).executes(context -> {
                GirlFriendEntity gf = closest(context);
                gf.setPlayerCustomName(StringArgumentType.getString(context, "name"));
                gf.say("\"" + gf.getDisplayNameForChat() + "\"... I love it!");
                return 1;
            })))
            .then(Commands.literal("texture").then(Commands.argument("variant", IntegerArgumentType.integer(1, 20)).executes(context -> {
                GirlFriendEntity gf = closest(context);
                gf.clearSkinProfile();
                gf.setTextureVariant(String.valueOf(IntegerArgumentType.getInteger(context, "variant")));
                return 1;
            })))
            .then(Commands.literal("skin")
                .then(Commands.literal("reset").executes(context -> {
                    closest(context).clearSkinProfile();
                    reply(context, "Back to her own look.");
                    return 1;
                }))
                .then(Commands.argument("username", StringArgumentType.word()).executes(context -> {
                    SkinService.applyPlayerSkin(context.getSource().getPlayerOrException(), closest(context), StringArgumentType.getString(context, "username"));
                    return 1;
                })))
            .then(Commands.literal("friendlyfire").then(Commands.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                GirlFriendEntity gf = closest(context);
                gf.setFriendlyFire(BoolArgumentType.getBool(context, "enabled"));
                reply(context, gf.isFriendlyFire() ? "Your hits can now hurt " + gf.getDisplayNameForChat() + " (she'll sulk, never fight back)." : gf.getDisplayNameForChat() + " is now immune to your hits.");
                return 1;
            })))
            .then(Commands.literal("relationship").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 100)).executes(context -> {
                    closest(context).setRelationshipLevel(IntegerArgumentType.getInteger(context, "level"));
                    reply(context, "Relationship set to " + IntegerArgumentType.getInteger(context, "level"));
                    return 1;
                })))
            .then(Commands.literal("mood").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 100)).executes(context -> {
                    closest(context).setMoodLevel(IntegerArgumentType.getInteger(context, "level"));
                    reply(context, "Mood set to " + IntegerArgumentType.getInteger(context, "level"));
                    return 1;
                })))
        );
    }
}
