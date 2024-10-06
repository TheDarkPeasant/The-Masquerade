package net.random_something.masquerader_mod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.client.model.MasqueraderModel;
import net.random_something.masquerader_mod.client.render.layer.MasqueraderArmorLayer;
import net.random_something.masquerader_mod.entity.Masquerader;
import net.random_something.masquerader_mod.entity.MasqueraderClone;

@OnlyIn(Dist.CLIENT)
public class MasqueraderRenderer<T extends Masquerader> extends MobRenderer<T, MasqueraderModel<T>> {
    private static final ResourceLocation NO_MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masquerader/masquerader.png");
    private static final ResourceLocation EVOKER_MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masquerader/masquerader_evoker.png");
    private static final ResourceLocation ILLUSIONER_MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masquerader/masquerader_illusioner.png");
    private static final ResourceLocation RAVAGER_MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masquerader/masquerader_ravager.png");
    private static final ResourceLocation WITCH_MASK = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masquerader/masquerader_witch.png");

    public MasqueraderRenderer(EntityRendererProvider.Context context) {
        super(context, new MasqueraderModel<>(context.bakeLayer(MasqueraderModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new MasqueraderArmorLayer<>(this));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()) {
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int p_116354_, T masquerader, float p_116356_, float p_116357_, float p_116358_, float p_116359_, float p_116360_, float p_116361_) {
                if (masquerader.shouldShowArms()) {
                    super.render(poseStack, multiBufferSource, p_116354_, masquerader, p_116356_, p_116357_, p_116358_, p_116359_, p_116360_, p_116361_);
                }
            }

        });
        this.addLayer(new CrossedArmsItemLayer<>(this, context.getItemInHandRenderer()) {
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int p_116354_, T masquerader, float p_116356_, float p_116357_, float p_116358_, float p_116359_, float p_116360_, float p_116361_) {
                if (!masquerader.shouldShowArms() && masquerader.getMainHandItem().is(Items.POTION)) {
                    super.render(poseStack, multiBufferSource, p_116354_, masquerader, p_116356_, p_116357_, p_116358_, p_116359_, p_116360_, p_116361_);
                }
            }

        });
    }

    @Override
    public ResourceLocation getTextureLocation(T pEntity) {
        if (pEntity instanceof MasqueraderClone) {
            return ILLUSIONER_MASK;
        }
        return switch (pEntity.getMask()) {
            case Masquerader.EVOKER_MASK -> EVOKER_MASK;
            case Masquerader.ILLUSIONER_MASK -> ILLUSIONER_MASK;
            case Masquerader.RAVAGER_MASK -> RAVAGER_MASK;
            case Masquerader.WITCH_MASK -> WITCH_MASK;
            default -> NO_MASK;
        };
    }

    @Override
    public void render(T p_114952_, float p_114953_, float p_114954_, PoseStack p_114955_, MultiBufferSource p_114956_, int p_114957_) {
        if (p_114952_.isInvisible()) {
            Vec3[] avec3 = p_114952_.getIllusionOffsets(p_114954_);
            float f = this.getBob(p_114952_, p_114954_);

            for (int i = 0; i < avec3.length; ++i) {
                p_114955_.pushPose();
                p_114955_.translate(avec3[i].x + (double) Mth.cos((float) i + f * 0.5F) * 0.025D, avec3[i].y + (double) Mth.cos((float) i + f * 0.75F) * 0.0125D, avec3[i].z + (double) Mth.cos((float) i + f * 0.7F) * 0.025D);
                super.render(p_114952_, p_114953_, p_114954_, p_114955_, p_114956_, p_114957_);
                p_114955_.popPose();
            }
        } else {
            super.render(p_114952_, p_114953_, p_114954_, p_114955_, p_114956_, p_114957_);
        }

    }

    @Override
    protected float getFlipDegrees(T p_115337_) {
        return 0;
    }

    @Override
    protected boolean isBodyVisible(T p_114959_) {
        return true;
    }

    @Override
    public Vec3 getRenderOffset(T entity, float p_114484_) {
        if (entity.getAttackType() == 7 && entity.isRoaring()) {
            int craziness = 5;
            return new Vec3(entity.getRandom().nextGaussian() * 0.02 * (double) craziness, 0.0, entity.getRandom().nextGaussian() * 0.02 * (double) craziness);
        }
        return super.getRenderOffset(entity, p_114484_);
    }
}