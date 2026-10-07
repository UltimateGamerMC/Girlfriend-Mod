package com.beckytidus.girlfriendmod.network;

import com.beckytidus.girlfriendmod.GirlfriendMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Sent by the girlfriend menu when the player clicks something. */
public record GirlfriendActionPayload(int entityId, String action, String argument) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<GirlfriendActionPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(GirlfriendMod.MOD_ID, "action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GirlfriendActionPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, GirlfriendActionPayload::entityId,
        ByteBufCodecs.stringUtf8(32), GirlfriendActionPayload::action,
        ByteBufCodecs.stringUtf8(32), GirlfriendActionPayload::argument,
        GirlfriendActionPayload::new
    );

    @Override
    public CustomPacketPayload.Type<GirlfriendActionPayload> type() {
        return TYPE;
    }
}
