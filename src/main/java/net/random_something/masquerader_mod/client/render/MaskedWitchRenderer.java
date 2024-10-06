package net.random_something.masquerader_mod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.random_something.masquerader_mod.MasqueraderMod;

@OnlyIn(Dist.CLIENT)
public class MaskedWitchRenderer extends WitchRenderer {
    private static final ResourceLocation MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_witch/masked_witch.png");

    public MaskedWitchRenderer(EntityRendererProvider.Context p_174439_) {
        super(p_174439_);
    }

    @Override
    public ResourceLocation getTextureLocation(Witch p_114541_) {
        return MASK;
    }
}
