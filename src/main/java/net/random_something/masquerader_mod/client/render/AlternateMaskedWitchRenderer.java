package net.random_something.masquerader_mod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.client.model.MaskedWitchModel;
import net.random_something.masquerader_mod.client.render.layer.MaskedWitchItemLayer;

@OnlyIn(Dist.CLIENT)
public class AlternateMaskedWitchRenderer<T extends Witch> extends MobRenderer<T, MaskedWitchModel<T>> {
    private static final ResourceLocation MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_witch/masked_witch_alternate.png");

    public AlternateMaskedWitchRenderer(EntityRendererProvider.Context context) {
        super(context, new MaskedWitchModel<>(context.bakeLayer(MaskedWitchModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new MaskedWitchItemLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(T p_116412_, float p_116413_, float p_116414_, PoseStack p_116415_, MultiBufferSource p_116416_, int p_116417_) {
        this.model.setHoldingItem(!p_116412_.getMainHandItem().isEmpty());
        super.render(p_116412_, p_116413_, p_116414_, p_116415_, p_116416_, p_116417_);
    }

    @Override
    public ResourceLocation getTextureLocation(Witch p_114541_) {
        return MASK;
    }
}
