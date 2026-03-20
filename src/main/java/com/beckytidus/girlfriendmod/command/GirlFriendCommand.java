package com.beckytidus.girlfriendmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import com.beckytidus.girlfriendmod.registry.FemaleNames;
import com.beckytidus.girlfriendmod.registry.GirlfriendSkins;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GirlFriendCommand {
    private static final double FIND_RANGE = 128.0;

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerGirlFriendCommand(dispatcher);
        });
    }

    @Nullable
    private static GirlFriendEntity getClosestGirlfriend(ServerCommandSource source) {
        PlayerEntity player = source.getPlayer();
        if (player == null) return null;
        ServerWorld world = source.getWorld();
        GirlFriendEntity closest = null;
        double closestSq = FIND_RANGE * FIND_RANGE;
        for (GirlFriendEntity gf : world.getEntitiesByClass(GirlFriendEntity.class, player.getBoundingBox().expand(FIND_RANGE), g -> g.getOwner() == player)) {
            double dSq = gf.squaredDistanceTo(player);
            if (dSq < closestSq) {
                closestSq = dSq;
                closest = gf;
            }
        }
        return closest;
    }

    private static void registerGirlFriendCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("girlfriend")
                .then(CommandManager.literal("summon")
                    .executes(context -> {
                        PlayerEntity player = context.getSource().getPlayerOrThrow();
                        ServerWorld world = context.getSource().getWorld();
                        GirlFriendEntity girlfriend = new GirlFriendEntity(EntityRegistry.GIRLFRIEND, world);
                        girlfriend.setPosition(player.getX(), player.getY(), player.getZ());
                        girlfriend.setOwner(player);
                        girlfriend.setPlayerCustomName(FemaleNames.pickRandom(world.getRandom()));
                        girlfriend.setTextureVariant(GirlfriendSkins.pickRandomTextureVariant(world.getRandom()));
                        world.spawnEntity(girlfriend);
                        context.getSource().sendMessage(Text.literal("♥ GirlFriend summoned for " + player.getName().getString()));
                        return 1;
                    })
                )
                .then(CommandManager.literal("relationship")
                    .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 100))
                        .executes(context -> {
                            GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                            if (gf == null) {
                                context.getSource().sendMessage(Text.literal("No girlfriend found nearby"));
                                return 0;
                            }
                            int level = IntegerArgumentType.getInteger(context, "level");
                            gf.setRelationshipLevel(level);
                            context.getSource().sendMessage(Text.literal("♥ Set relationship to " + level));
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("list")
                    .executes(context -> {
                        PlayerEntity player = context.getSource().getPlayerOrThrow();
                        ServerWorld world = context.getSource().getWorld();
                        var list = world.getEntitiesByClass(GirlFriendEntity.class, player.getBoundingBox().expand(FIND_RANGE), g -> g.getOwner() == player);
                        context.getSource().sendMessage(Text.literal("Girlfriends nearby: " + list.size()));
                        for (GirlFriendEntity gf : list) {
                            context.getSource().sendMessage(gf.getStatsDisplayText());
                        }
                        return 1;
                    })
                )
                .then(CommandManager.literal("give")
                    .executes(context -> {
                        return giveSummoner(context.getSource(), context.getSource().getPlayerOrThrow());
                    })
                )
                .then(CommandManager.literal("mood")
                    .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 100))
                        .executes(context -> {
                            GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                            if (gf == null) {
                                context.getSource().sendMessage(Text.literal("No girlfriend found nearby"));
                                return 0;
                            }
                            int level = IntegerArgumentType.getInteger(context, "level");
                            gf.setMoodLevel(level);
                            context.getSource().sendMessage(Text.literal("♥ Set mood to " + level));
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("texture")
                    .then(CommandManager.argument("variant", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (int i = 1; i <= 20; i++) builder.suggest(String.valueOf(i));
                            builder.suggest("default");
                            builder.suggest("alt");
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                            if (gf == null) {
                                context.getSource().sendMessage(Text.literal("No girlfriend found nearby"));
                                return 0;
                            }
                            String v = StringArgumentType.getString(context, "variant");
                            gf.setTextureVariant(v);
                            context.getSource().sendMessage(Text.literal("♥ Set texture to " + v));
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("skin")
                    .then(CommandManager.argument("username", StringArgumentType.string())
                        .executes(context -> {
                            GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                            if (gf == null) {
                                context.getSource().sendMessage(Text.literal("No girlfriend found nearby"));
                                return 0;
                            }
                            String username = StringArgumentType.getString(context, "username");
                            gf.setSkinOwnerName(username);
                            context.getSource().sendMessage(Text.literal("♥ Set girlfriend skin to player: " + username));
                            return 1;
                        })
                )
                )
                .then(CommandManager.literal("stats")
                    .executes(context -> {
                        GirlFriendEntity gf = getClosestGirlfriend(context.getSource());
                        if (gf == null) {
                            context.getSource().sendMessage(Text.literal("No girlfriend found nearby"));
                            return 0;
                        }
                        context.getSource().sendMessage(gf.getStatsDisplayText());
                        context.getSource().getPlayerOrThrow().sendMessage(gf.getStatsDisplayText(), true);
                        return 1;
                    })
                )
        );
    }

    private static int giveSummoner(ServerCommandSource source, PlayerEntity target) {
        ItemStack stack = new ItemStack(ItemRegistry.GIRLFRIEND_SUMMONER);
        if (!target.getInventory().insertStack(stack)) {
            target.dropItem(stack, false);
        }
        source.sendMessage(Text.literal("Gave Girlfriend Summoner to " + target.getName().getString()));
        return 1;
    }
}
