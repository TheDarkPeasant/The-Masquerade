package net.random_something.masquerader_mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.random_something.masquerader_mod.MasqueraderMod;

public class EntityRegister {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MasqueraderMod.MOD_ID);
    public static final RegistryObject<EntityType<Masquerader>> MASQUERADER = ENTITIES.register("masquerader", () -> EntityType.Builder.of(Masquerader::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(MasqueraderMod.MOD_ID + ":masquerader"));
    public static final RegistryObject<EntityType<MasqueraderClone>> MASQUERADER_CLONE = ENTITIES.register("masquerader_clone", () -> EntityType.Builder.<MasqueraderClone>of(MasqueraderClone::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(MasqueraderMod.MOD_ID + ":masquerader_clone"));
    public static final RegistryObject<EntityType<MaskedVindicator>> MASKED_VINDICATOR = ENTITIES.register("masked_vindicator", () -> EntityType.Builder.of(MaskedVindicator::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(MasqueraderMod.MOD_ID + ":masked_vindicator"));
    public static final RegistryObject<EntityType<MaskedEvoker>> MASKED_EVOKER = ENTITIES.register("masked_evoker", () -> EntityType.Builder.of(MaskedEvoker::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(MasqueraderMod.MOD_ID + ":masked_evoker"));
    public static final RegistryObject<EntityType<MaskedIllusioner>> MASKED_ILLUSIONER = ENTITIES.register("masked_illusioner", () -> EntityType.Builder.of(MaskedIllusioner::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(MasqueraderMod.MOD_ID + ":masked_illusioner"));
    public static final RegistryObject<EntityType<MaskedWitch>> MASKED_WITCH = ENTITIES.register("masked_witch", () -> EntityType.Builder.of(MaskedWitch::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(MasqueraderMod.MOD_ID + ":masked_witch"));
    public static final RegistryObject<EntityType<MaskedVillager>> MASKED_VILLAGER = ENTITIES.register("masked_villager", () -> EntityType.Builder.of(MaskedVillager::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).build(MasqueraderMod.MOD_ID + ":masked_villager"));
}