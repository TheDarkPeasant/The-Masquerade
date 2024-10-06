package net.random_something.masquerader_mod.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.random_something.masquerader_mod.Config;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.entity.MaskedVindicator;

@OnlyIn(Dist.CLIENT)
public class MaskedVindicatorRenderer extends VindicatorRenderer {
    private static final ResourceLocation MASK1 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_vindicator/masked_vindicator1.png");
    private static final ResourceLocation MASK2 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_vindicator/masked_vindicator2.png");
    private static final ResourceLocation MASK3 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_vindicator/masked_vindicator3.png");
    private static final ResourceLocation ALT_MASK1 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_vindicator/alternate/masked_vindicator1.png");
    private static final ResourceLocation ALT_MASK2 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_vindicator/alternate/masked_vindicator2.png");
    private static final ResourceLocation ALT_MASK3 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_vindicator/alternate/masked_vindicator3.png");

    public MaskedVindicatorRenderer(EntityRendererProvider.Context p_174439_) {
        super(p_174439_);
    }

    @Override
    public ResourceLocation getTextureLocation(Vindicator vindicator) {
        if (Config.alternateTextures.get()) {
            if (vindicator instanceof MaskedVindicator && ((MaskedVindicator) vindicator).getMask() == 0) {
                return ALT_MASK1;
            } else if (vindicator instanceof MaskedVindicator && ((MaskedVindicator) vindicator).getMask() == 1) {
                return ALT_MASK2;
            } else if (vindicator instanceof MaskedVindicator && ((MaskedVindicator) vindicator).getMask() == 2) {
                return ALT_MASK3;
            }
        }

        if (vindicator instanceof MaskedVindicator && ((MaskedVindicator) vindicator).getMask() == 0) {
            return MASK1;
        } else if (vindicator instanceof MaskedVindicator && ((MaskedVindicator) vindicator).getMask() == 1) {
            return MASK2;
        } else if (vindicator instanceof MaskedVindicator && ((MaskedVindicator) vindicator).getMask() == 2) {
            return MASK3;
        }

        return Config.alternateTextures.get() ? ALT_MASK1 : MASK1;
    }
}
