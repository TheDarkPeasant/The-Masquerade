package net.random_something.masquerader_mod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.random_something.masquerader_mod.Config;
import net.random_something.masquerader_mod.MasqueraderMod;

@OnlyIn(Dist.CLIENT)
public class MaskedEvokerRenderer extends EvokerRenderer {
    private static final ResourceLocation MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_evoker/masked_evoker.png");
    private static final ResourceLocation ALT_MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_evoker/masked_evoker_alternate.png");

    public MaskedEvokerRenderer(EntityRendererProvider.Context p_174439_) {
        super(p_174439_);
    }

    @Override
    public ResourceLocation getTextureLocation(SpellcasterIllager p_114541_) {
        return Config.alternateTextures.get() ? ALT_MASK : MASK;
    }
}
