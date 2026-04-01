package com.beckytidus.girlfriendmod.registry;

import com.beckytidus.girlfriendmod.GirlfriendMod;
import com.beckytidus.girlfriendmod.item.GirlFriendSummonerItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ItemRegistry {
    public static Item GIRLFRIEND_SUMMONER;

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(GirlfriendMod.MOD_ID, "girlfriend_summoner");
        ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);

        Item.Properties settings = new Item.Properties()
                .stacksTo(1)
                .setId(key);

        GIRLFRIEND_SUMMONER = Registry.register(
            BuiltInRegistries.ITEM,
            id,
            new GirlFriendSummonerItem(settings)
        );
    }
}
