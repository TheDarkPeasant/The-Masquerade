package net.random_something.masquerader_mod.event;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.random_something.masquerader_mod.Config;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.client.model.AlternateMasqueraderModel;
import net.random_something.masquerader_mod.client.model.MaskedWitchModel;
import net.random_something.masquerader_mod.client.model.MasqueraderModel;
import net.random_something.masquerader_mod.client.render.*;
import net.random_something.masquerader_mod.entity.EntityRegister;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MasqueraderMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (Config.alternateTextures.get()) {
                EntityRenderers.register(EntityRegister.MASQUERADER.get(), AlternateMasqueraderRenderer::new);
                EntityRenderers.register(EntityRegister.MASQUERADER_CLONE.get(), AlternateMasqueraderRenderer::new);
                EntityRenderers.register(EntityRegister.MASKED_WITCH.get(), AlternateMaskedWitchRenderer::new);
            } else {
                EntityRenderers.register(EntityRegister.MASQUERADER.get(), MasqueraderRenderer::new);
                EntityRenderers.register(EntityRegister.MASQUERADER_CLONE.get(), MasqueraderRenderer::new);
                EntityRenderers.register(EntityRegister.MASKED_WITCH.get(), MaskedWitchRenderer::new);
            }

            EntityRenderers.register(EntityRegister.MASKED_VINDICATOR.get(), MaskedVindicatorRenderer::new);
            EntityRenderers.register(EntityRegister.MASKED_EVOKER.get(), MaskedEvokerRenderer::new);
            EntityRenderers.register(EntityRegister.MASKED_ILLUSIONER.get(), MaskedIllusionerRenderer::new);
            EntityRenderers.register(EntityRegister.MASKED_VILLAGER.get(), MaskedVillagerRenderer::new);
        });
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MasqueraderModel.LAYER_LOCATION, MasqueraderModel::createBodyLayer);
        event.registerLayerDefinition(AlternateMasqueraderModel.LAYER_LOCATION, AlternateMasqueraderModel::createBodyLayer);
        event.registerLayerDefinition(MaskedWitchModel.LAYER_LOCATION, MaskedWitchModel::createBodyLayer);
    }
}