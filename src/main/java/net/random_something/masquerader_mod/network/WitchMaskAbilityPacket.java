package net.random_something.masquerader_mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.random_something.masquerader_mod.event.WorldEventHandler;

import java.util.function.Supplier;

public class WitchMaskAbilityPacket {
    public WitchMaskAbilityPacket() {

    }

    public static void encode(WitchMaskAbilityPacket packet, FriendlyByteBuf buffer) {
    }

    public static WitchMaskAbilityPacket decode(FriendlyByteBuf buffer) {
        return new WitchMaskAbilityPacket();
    }

    public static void handle(WitchMaskAbilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                WorldEventHandler.doServerWitchMaskAbility(player);
            }
        });
        context.setPacketHandled(true);
    }
}
