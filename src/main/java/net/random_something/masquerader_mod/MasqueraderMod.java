package net.random_something.masquerader_mod;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.random_something.masquerader_mod.entity.EntityRegister;
import net.random_something.masquerader_mod.item.ItemRegister;
import net.random_something.masquerader_mod.network.PacketRegister;
import net.random_something.masquerader_mod.sounds.SoundRegister;
import net.random_something.masquerader_mod.structures.StructureRegister;
import net.random_something.masquerader_mod.structures.placements.StructurePlacementTypeRegister;

@Mod(MasqueraderMod.MOD_ID)
public class MasqueraderMod {
    public static final String MOD_ID = "masquerader_mod";

    public MasqueraderMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EntityRegister.ENTITIES.register(modEventBus);
        ItemRegister.ITEMS.register(modEventBus);
        SoundRegister.SOUND_EVENTS.register(modEventBus);
        StructureRegister.STRUCTURE_TYPE_DEF_REG.register(modEventBus);
        StructureRegister.STRUCTURE_PIECE_DEF_REG.register(modEventBus);
        StructurePlacementTypeRegister.STRUCTURE_PLACEMENT_TYPE.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::clientSetup);
            MinecraftForge.EVENT_BUS.register(this);
        });

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, MasqueraderMod.MOD_ID + "-common.toml");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ItemRegister.BLANK_MASK);
            event.accept(ItemRegister.EVOKER_MASK);
            event.accept(ItemRegister.ILLUSIONER_MASK);
            event.accept(ItemRegister.RAVAGER_MASK);
            event.accept(ItemRegister.WITCH_MASK);
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ItemRegister.MASQUERADER_SPAWN_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ItemRegister.INVITATION);
        }
    }

    public void clientSetup(FMLClientSetupEvent event) {
        ItemProperties.register(ItemRegister.BLANK_MASK.get(), new ResourceLocation("alternate_texture"),
                (stack, world, entity, seed) -> Config.alternateTextures.get() ? 1.0F : 0.0F);
        ItemProperties.register(ItemRegister.EVOKER_MASK.get(), new ResourceLocation("alternate_texture"),
                (stack, world, entity, seed) -> Config.alternateTextures.get() ? 1.0F : 0.0F);
        ItemProperties.register(ItemRegister.ILLUSIONER_MASK.get(), new ResourceLocation("alternate_texture"),
                (stack, world, entity, seed) -> Config.alternateTextures.get() ? 1.0F : 0.0F);
        ItemProperties.register(ItemRegister.RAVAGER_MASK.get(), new ResourceLocation("alternate_texture"),
                (stack, world, entity, seed) -> Config.alternateTextures.get() ? 1.0F : 0.0F);
        ItemProperties.register(ItemRegister.WITCH_MASK.get(), new ResourceLocation("alternate_texture"),
                (stack, world, entity, seed) -> Config.alternateTextures.get() ? 1.0F : 0.0F);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        RaidWaveMembers.registerWaveMembers();
        PacketRegister.registerPackets();
    }
}