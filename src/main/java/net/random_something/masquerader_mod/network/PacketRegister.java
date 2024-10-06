package net.random_something.masquerader_mod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.random_something.masquerader_mod.MasqueraderMod;

public class PacketRegister {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MasqueraderMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        INSTANCE.registerMessage(0, IllusionerMaskAbilityPacket.class, IllusionerMaskAbilityPacket::encode, IllusionerMaskAbilityPacket::decode, IllusionerMaskAbilityPacket::handle);
        INSTANCE.registerMessage(1, RavagerMaskAbilityPacket.class, RavagerMaskAbilityPacket::encode, RavagerMaskAbilityPacket::decode, RavagerMaskAbilityPacket::handle);
        INSTANCE.registerMessage(2, WitchMaskAbilityPacket.class, WitchMaskAbilityPacket::encode, WitchMaskAbilityPacket::decode, WitchMaskAbilityPacket::handle);
    }
}