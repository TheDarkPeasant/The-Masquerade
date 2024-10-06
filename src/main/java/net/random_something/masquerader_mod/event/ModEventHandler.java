package net.random_something.masquerader_mod.event;

import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.entity.EntityRegister;
import net.random_something.masquerader_mod.entity.MaskedVillager;
import net.random_something.masquerader_mod.entity.Masquerader;
import net.random_something.masquerader_mod.entity.MasqueraderClone;

@Mod.EventBusSubscriber(modid = MasqueraderMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    @SubscribeEvent
    public static void attributeRegister(EntityAttributeCreationEvent event) {
        event.put(EntityRegister.MASQUERADER.get(), Masquerader.createAttributes().build());
        event.put(EntityRegister.MASQUERADER_CLONE.get(), MasqueraderClone.createAttributes().build());
        event.put(EntityRegister.MASKED_VINDICATOR.get(), Vindicator.createAttributes().build());
        event.put(EntityRegister.MASKED_EVOKER.get(), Evoker.createAttributes().build());
        event.put(EntityRegister.MASKED_ILLUSIONER.get(), Illusioner.createAttributes().build());
        event.put(EntityRegister.MASKED_WITCH.get(), Witch.createAttributes().build());
        event.put(EntityRegister.MASKED_VILLAGER.get(), MaskedVillager.createAttributes().build());
    }
}