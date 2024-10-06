package net.random_something.masquerader_mod.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.entity.EntityRegister;

public class ItemRegister {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MasqueraderMod.MOD_ID);
    public static final RegistryObject<Item> MASQUERADER_SPAWN_EGG = ITEMS.register("masquerader_spawn_egg", () -> new ForgeSpawnEggItem(EntityRegister.MASQUERADER, 0x930000, 0xffd700, new Item.Properties()));
    public static final RegistryObject<Item> BLANK_MASK = ITEMS.register("blank_mask", () -> new BlankMask(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> EVOKER_MASK = ITEMS.register("evoker_mask", () -> new EvokerMask(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ILLUSIONER_MASK = ITEMS.register("illusioner_mask", () -> new IllusionerMask(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RAVAGER_MASK = ITEMS.register("ravager_mask", () -> new RavagerMask(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> WITCH_MASK = ITEMS.register("witch_mask", () -> new WitchMask(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INVITATION = ITEMS.register("invitation", () -> new Invitation(new Item.Properties().rarity(Rarity.UNCOMMON)));
}