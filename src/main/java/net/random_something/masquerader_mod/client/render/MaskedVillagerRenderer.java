package net.random_something.masquerader_mod.client.render;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.random_something.masquerader_mod.Config;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.entity.MaskedVillager;

@OnlyIn(Dist.CLIENT)
public class MaskedVillagerRenderer extends MobRenderer<MaskedVillager, VillagerModel<MaskedVillager>> {
    private static final ResourceLocation NORMAL = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/masked_villager_normal.png");
    private static final ResourceLocation SWEAT_1 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/masked_villager1.png");
    private static final ResourceLocation SWEAT_2 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/masked_villager2.png");
    private static final ResourceLocation SWEAT_3 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/masked_villager3.png");
    private static final ResourceLocation SWEAT_4 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/masked_villager4.png");

    private static final ResourceLocation ALT_NORMAL = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/alternate/masked_villager_normal.png");
    private static final ResourceLocation ALT_SWEAT_1 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/alternate/masked_villager1.png");
    private static final ResourceLocation ALT_SWEAT_2 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/alternate/masked_villager2.png");
    private static final ResourceLocation ALT_SWEAT_3 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/alternate/masked_villager3.png");
    private static final ResourceLocation ALT_SWEAT_4 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/alternate/masked_villager4.png");
    private static final ResourceLocation ALT_SWEAT_5 = new ResourceLocation(MasqueraderMod.MOD_ID, "textures/entity/masked_villager/alternate/masked_villager5.png");

    public MaskedVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.WANDERING_TRADER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(MaskedVillager entity) {
        if (Config.alternateTextures.get()) {
            return switch (entity.getTexture()) {
                case 0 -> ALT_SWEAT_1;
                case 1 -> ALT_SWEAT_2;
                case 2 -> ALT_SWEAT_3;
                case 3 -> ALT_SWEAT_4;
                case 4 -> ALT_SWEAT_5;
                default -> ALT_NORMAL;
            };
        } else {
            return switch (entity.getTexture()) {
                case 0 -> SWEAT_1;
                case 1 -> SWEAT_2;
                case 2 -> SWEAT_3;
                case 3 -> SWEAT_4;
                default -> NORMAL;
            };
        }

    }

    @Override
    public Vec3 getRenderOffset(MaskedVillager entity, float p_114484_) {
        if (entity.areIllagersNearby()) {
            double fear = 0.5;
            return new Vec3(entity.getRandom().nextGaussian() * 0.02 * fear, 0.0, entity.getRandom().nextGaussian() * 0.02 * fear);
        }

        return super.getRenderOffset(entity, p_114484_);
    }
}
