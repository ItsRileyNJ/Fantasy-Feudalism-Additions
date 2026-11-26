package com.fantasyfeudalism.additions.clairvoyance;

import com.fantasyfeudalism.additions.FeudalAdditions;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClairvoyancePayload(ResourceLocation dimension, List<UUID> targets) implements CustomPacketPayload {
    public static final Type<ClairvoyancePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FeudalAdditions.ID, "clairvoyance_targets"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClairvoyancePayload> CODEC = StreamCodec.of(
        (buffer, value) -> {
            buffer.writeResourceLocation(value.dimension());
            buffer.writeCollection(value.targets(), (out, id) -> out.writeUUID(id));
        }, buffer -> new ClairvoyancePayload(buffer.readResourceLocation(), buffer.readList(in -> in.readUUID())));
    public ClairvoyancePayload { targets = List.copyOf(targets); }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
