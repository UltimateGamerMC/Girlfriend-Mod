package com.beckytidus.girlfriendmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beckytidus.girlfriendmod.registry.EntityRegistry;
import com.beckytidus.girlfriendmod.registry.ItemRegistry;
import com.beckytidus.girlfriendmod.command.GirlFriendCommand;
import com.beckytidus.girlfriendmod.interaction.EntityInteractionHandler;
import com.beckytidus.girlfriendmod.interaction.ItemUseHandler;
import com.beckytidus.girlfriendmod.event.EntityAttributeHandler;
import com.beckytidus.girlfriendmod.dialogue.DelayedChatReply;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

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
		ServerTickEvents.END_SERVER_TICK.register(DelayedChatReply::tick);

		RegistryKey<ItemGroup> ingredientsKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.ofVanilla("ingredients"));
		ItemGroupEvents.modifyEntriesEvent(ingredientsKey).register(entries -> entries.add(ItemRegistry.GIRLFRIEND_SUMMONER));

		LOGGER.info("Girlfriend Mod loaded successfully!");
	}
}