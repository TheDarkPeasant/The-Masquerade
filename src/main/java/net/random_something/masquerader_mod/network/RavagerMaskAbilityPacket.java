package net.random_something.masquerader_mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.random_something.masquerader_mod.event.WorldEventHandler;

import java.util.function.Supplier;

public class RavagerMaskAbilityPacket {
    public RavagerMaskAbilityPacket() {

    }

    public static void encode(RavagerMaskAbilityPacket packet, FriendlyByteBuf buffer) {
    }

    public static RavagerMaskAbilityPacket decode(FriendlyByteBuf buffer) {
        return new RavagerMaskAbilityPacket();
    }

    public static void handle(RavagerMaskAbilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                WorldEventHandler.doServerRavagerMaskAbility(player);
            }
        });
        context.setPacketHandled(true);
    }
}
