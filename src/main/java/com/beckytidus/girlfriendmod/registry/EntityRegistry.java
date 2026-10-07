package com.beckytidus.girlfriendmod.registry;

import com.beckytidus.girlfriendmod.GirlfriendMod;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityRegistry {
    public static EntityType<GirlFriendEntity> GIRLFRIEND;

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(GirlfriendMod.MOD_ID, "girlfriend");
        ResourceKey<EntityType<?>> key = ResourceKey.create(BuiltInRegistries.ENTITY_TYPE.key(), id);

        GIRLFRIEND = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                id,
                EntityType.Builder.of(GirlFriendEntity::new, MobCategory.CREATURE)
                        .sized(0.9f, 1.9f)
                        .build(key)
        );
    }
}
