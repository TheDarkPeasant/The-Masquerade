package net.random_something.masquerader_mod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.random_something.masquerader_mod.Config;
import net.random_something.masquerader_mod.MasqueraderMod;

@OnlyIn(Dist.CLIENT)
public class MaskedIllusionerRenderer extends IllusionerRenderer {
    private static final ResourceLocation MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_illusioner/masked_illusioner.png");
    private static final ResourceLocation ALT_MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_illusioner/masked_illusioner_alternate.png");

    public MaskedIllusionerRenderer(EntityRendererProvider.Context p_174439_) {
        super(p_174439_);
    }

    @Override
    public ResourceLocation getTextureLocation(Illusioner p_114541_) {
        return Config.alternateTextures.get() ? ALT_MASK : MASK;
    }
}
