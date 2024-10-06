package net.random_something.masquerader_mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.random_something.masquerader_mod.event.WorldEventHandler;

import java.util.function.Supplier;

public class IllusionerMaskAbilityPacket {

    private static Vec3 teleportPos = null;

    public IllusionerMaskAbilityPacket(Vec3 teleportPos) {
        IllusionerMaskAbilityPacket.teleportPos = teleportPos;
    }

    public static void encode(IllusionerMaskAbilityPacket packet, FriendlyByteBuf buffer) {
    }

    public static IllusionerMaskAbilityPacket decode(FriendlyByteBuf buffer) {
        return new IllusionerMaskAbilityPacket(teleportPos);
    }

    public static void handle(IllusionerMaskAbilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                WorldEventHandler.executeIllusionerMaskAbility(player, teleportPos);
            }
        });
        context.setPacketHandled(true);
    }
}
