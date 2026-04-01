package com.beckytidus.girlfriendmod;

import com.beckytidus.girlfriendmod.chat.GirlfriendChatHandler;
import com.beckytidus.girlfriendmod.command.GirlFriendCommand;
import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import com.beckytidus.girlfriendmod.event.EntityAttributeHandler;
import com.beckytidus.girlfriendmod.interaction.EntityInteractionHandler;
import com.beckytidus.girlfriendmod.interaction.ItemUseHandler;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;

public class GirlfriendMod implements ModInitializer {
	public static final String MOD_ID = "girlfriend-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Girlfriend Mod...");

		EntityRegistry.register();
		ItemRegistry.register();
		EntityAttributeHandler.register();
		GirlFriendCommand.register();
		EntityInteractionHandler.register();
		ItemUseHandler.register();
		GirlfriendChatHandler.register();
		ServerTickEvents.END_SERVER_TICK.register(DelayedChatReply::tick);

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.accept(ItemRegistry.GIRLFRIEND_SUMMONER));

		LOGGER.info("Girlfriend Mod loaded successfully!");
	}
}
