package com.beckytidus.girlfriendmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GirlFriendCommand {
    private static final double FIND_RANGE = 128.0;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerGirlFriendCommand(dispatcher);
        });
    }

    @Nullable
    private static GirlFriendEntity getClosestGirlfriend(CommandSourceStack source) {
        Player player = source.getPlayer();
        if (player == null) return null;
        ServerLevel world = source.getLevel();
        GirlFriendEntity closest = null;
        double closestSq = FIND_RANGE * FIND_RANGE;
        for (GirlFriendEntity gf : world.getEntitiesOfClass(GirlFriendEntity.class, player.getBoundingBox().inflate(FIND_RANGE), g -> g.getOwner() == player)) {
            double dSq = gf.distanceToSqr(player);
            if (dSq < closestSq) {
                closestSq = dSq;
                closest = gf;
            }
        }
        return closest;
    }

    private static void registerGirlFriendCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("girlfriend")
                .then(Commands.literal("summon")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ServerLevel world = context.getSource().getLevel();
                        GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, world);
                        girlfriend.setPos(player.getX(), player.getY(), player.getZ());
                        girlfriend.setOwner(player);
                        girlfriend.setPlayerCustomName(FemaleNames.pickRandom(world.getRandom()));
                        girlfriend.setTextureVariant(GirlfriendSkins.pickRandomTextureVariant(world.getRandom()));
                        world.addFreshEntity(girlfriend);
                        context.getSource().sendSystemMessage(Component.literal("♥ GirlFriend summoned for " + player.getName().getString()));
                        return 1;
                    })
                )
                .then(Commands.literal("relationship")
                    .then(Commands.argument("level", IntegerArgumentType.integer(0, 100))
                        .executes(context -> {
                            GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                            if (gf == null) {
                                context.getSource().sendSystemMessage(Component.literal("No girlfriend found nearby"));
                                return 0;
                            }
                            int level = IntegerArgumentType.getInteger(context, "level");
                            gf.setRelationshipLevel(level);
                            context.getSource().sendSystemMessage(Component.literal("♥ Set relationship to " + level));
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("list")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ServerLevel world = context.getSource().getLevel();
                        var list = world.getEntitiesOfClass(GirlFriendEntity.class, player.getBoundingBox().inflate(FIND_RANGE), g -> g.getOwner() == player);
                        context.getSource().sendSystemMessage(Component.literal("Girlfriends nearby: " + list.size()));
                        for (GirlFriendEntity gf : list) {
                            context.getSource().sendSystemMessage(gf.getStatsDisplayText());
                        }
                        return 1;
                    })
                )
                .then(Commands.literal("give")
                    .executes(context -> {
                        return giveSummoner(context.getSource(), context.getSource().getPlayerOrException());
                    })
                )
                .then(Commands.literal("mood")
                    .then(Commands.argument("level", IntegerArgumentType.integer(0, 100))
                        .executes(context -> {
                            GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                            if (gf == null) {
                                context.getSource().sendSystemMessage(Component.literal("No girlfriend found nearby"));
                                return 0;
                            }
                            int level = IntegerArgumentType.getInteger(context, "level");
                            gf.setMoodLevel(level);
                            context.getSource().sendSystemMessage(Component.literal("♥ Set mood to " + level));
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("texture")
                    .then(Commands.argument("variant", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (int i = 1; i <= 20; i++) builder.suggest(String.valueOf(i));
                            builder.suggest("default");
                            builder.suggest("alt");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                            if (gf == null) {
                                context.getSource().sendSystemMessage(Component.literal("No girlfriend found nearby"));
                                return 0;
                            }
                            String v = StringArgumentType.getString(context, "variant");
                            gf.setTextureVariant(v);
                            context.getSource().sendSystemMessage(Component.literal("♥ Set texture to " + v));
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("skin")
                    .then(Commands.argument("username", StringArgumentType.string())
                        .executes(context -> {
                            GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                            if (gf == null) {
                                context.getSource().sendSystemMessage(Component.literal("No girlfriend found nearby"));
                                return 0;
                            }
                            String username = StringArgumentType.getString(context, "username");
                            gf.setSkinOwnerName(username);
                            context.getSource().sendSystemMessage(Component.literal("♥ Set girlfriend skin to player: " + username));
                            return 1;
                        })
                )
                )
                .then(Commands.literal("stats")
                    .executes(context -> {
                        GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                        if (gf == null) {
                            context.getSource().sendSystemMessage(Component.literal("No girlfriend found nearby"));
                            return 0;
                        }
                        context.getSource().sendSystemMessage(gf.getStatsDisplayText());
                        context.getSource().getPlayerOrException().sendSystemMessage(gf.getStatsDisplayText(), true);
                        return 1;
                    })
                )
        );
    }

    private static int giveSummoner(CommandSourceStack source, ServerPlayer target) {
        ItemStack stack = new ItemStack(ItemRegistry.GIRLFRIEND_SUMMONER);
        if (!target.getInventory().add(stack)) {
            target.drop(stack, false);
        }
        source.sendSystemMessage(Component.literal("Gave Girlfriend Summoner to " + target.getName().getString()));
        return 1;
    }
}
