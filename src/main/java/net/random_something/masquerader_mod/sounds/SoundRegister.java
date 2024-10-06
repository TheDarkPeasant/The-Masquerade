package net.random_something.masquerader_mod.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.random_something.masquerader_mod.MasqueraderMod;

public class SoundRegister {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MasqueraderMod.MOD_ID);

    public static final RegistryObject<SoundEvent> MASQUERADER_CHANGE_MASK = registerSoundEvent("masquerader_change_mask");
    public static final RegistryObject<SoundEvent> MASQUERADER_LAUGH = registerSoundEvent("masquerader_laugh");
    public static final RegistryObject<SoundEvent> MASQUERADER_PREPARE_CLONE = registerSoundEvent("masquerader_prepare_clone");
    public static final RegistryObject<SoundEvent> MASQUERADER_DEATH = registerSoundEvent("masquerader_death");
    public static final RegistryObject<SoundEvent> CLONE_DEATH = registerSoundEvent("clone_death");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(MasqueraderMod.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> new SoundEvent(id));
    }
}