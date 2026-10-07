package com.beckytidus.girlfriendmod;

import com.beckytidus.girlfriendmod.chat.GirlfriendChatHandler;
import com.beckytidus.girlfriendmod.command.GirlFriendCommand;
import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import com.beckytidus.girlfriendmod.network.GirlfriendActions;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GirlfriendMod implements ModInitializer {
	public static final String MOD_ID = "girlfriend-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final double COMPANION_RANGE = 24.0;

	@Override
	public void onInitialize() {
		EntityRegistry.register();
		ItemRegistry.register();
		FabricDefaultAttributeRegistry.register(EntityRegistry.GIRLFRIEND, GirlFriendEntity.createGirlfriendAttributes());
		GirlFriendCommand.register();
		GirlfriendChatHandler.register();
		GirlfriendActions.register();
		ServerTickEvents.END_SERVER_TICK.register(DelayedChatReply::tick);

		// Soulmates pull you back from death once per in-game day.
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayer player)) return true;
			for (GirlFriendEntity gf : player.level().getEntitiesOfClass(GirlFriendEntity.class, player.getBoundingBox().inflate(COMPANION_RANGE), g -> g.isOwnedBy(player))) {
				if (gf.tryRescueOwner(player)) return false;
			}
			return true;
		});

		EntitySleepEvents.START_SLEEPING.register((entity, pos) -> {
			if (!(entity instanceof ServerPlayer player)) return;
			for (GirlFriendEntity gf : player.level().getEntitiesOfClass(GirlFriendEntity.class, player.getBoundingBox().inflate(COMPANION_RANGE), g -> g.isOwnedBy(player))) {
				gf.onOwnerSleep();
			}
		});

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.accept(ItemRegistry.GIRLFRIEND_SUMMONER));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> output.accept(ItemRegistry.GIRLFRIEND_SUMMONER));
	}
}
