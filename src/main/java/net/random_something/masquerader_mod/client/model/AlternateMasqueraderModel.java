package net.random_something.masquerader_mod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.random_something.masquerader_mod.client.animation.MasqueraderAnimation;
import net.random_something.masquerader_mod.entity.Masquerader;

@OnlyIn(Dist.CLIENT)
public class AlternateMasqueraderModel<T extends Entity> extends HierarchicalModel<T> implements HeadedModel, ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("masquerader_mod", "alternate_masquerader"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart bone;
    private final ModelPart head;
    //	private final ModelPart hat;
    private final ModelPart arms;
    private final ModelPart cape;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public AlternateMasqueraderModel(ModelPart root) {
        this.root = root;
        this.body = this.root.getChild("body");
        this.bone = this.body.getChild("bone");
        this.head = this.root.getChild("head");
//  this.hat = this.head.getChild("hat");
//  this.hat.visible = false;
        this.cape = this.bone.getChild("cape");
        this.arms = this.body.getChild("arms");
        this.leftLeg = this.body.getChild("leg1");
        this.rightLeg = this.body.getChild("leg0");
        this.leftArm = this.body.getChild("LeftArm");
        this.rightArm = this.body.getChild("RightArm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(47, 38).addBox(-12.0F, -6.0F, -12.0F, 17.0F, 0.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(38, 0).addBox(-4.25F, -17.0F, -4.0F, 0.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Head_r1 = head.addOrReplaceChild("Head_r1", CubeListBuilder.create().texOffs(76, 62).addBox(0.0F, 0.0F, -8.0F, 7.0F, 0.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -6.0F, -3.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition mask = head.addOrReplaceChild("mask", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -30.0F, -4.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.075F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(-1.0F, 3.0F, 2.0F));

        PartDefinition bone = body.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(16, 20).addBox(-4.0F, -9.0F, -3.0F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-4.0F, -9.0F, -3.0F, 8.0F, 18.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(1.0F, 6.0F, -2.0F));

        PartDefinition cape = bone.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(6, 68).addBox(-8.0F, 0.0F, -0.5F, 13.0F, 18.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -9.0F, 3.0F));

        PartDefinition arms = body.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(44, 22).mirror().addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, -3.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition leg0 = body.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 9.0F, -2.0F));

        PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, 9.0F, -2.0F));

        PartDefinition RightArm = body.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(40, 46).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -1.0F, -2.0F));

        PartDefinition LeftArm = body.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(40, 46).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(6.0F, -1.0F, -2.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        ModelPart var10000;
        if (entity instanceof Masquerader masquerader) {
            this.animate(masquerader.getAnimationState("roar"), MasqueraderAnimation.ROAR, ageInTicks, masquerader.getAnimationSpeed());
            this.animate(masquerader.getAnimationState("potion"), MasqueraderAnimation.POTION, ageInTicks, masquerader.getAnimationSpeed());
            this.animate(masquerader.getAnimationState("change"), MasqueraderAnimation.CHANGE, ageInTicks, masquerader.getAnimationSpeed());
            this.animate(masquerader.getAnimationState("crossbow"), MasqueraderAnimation.CROSSBOW, ageInTicks, masquerader.getAnimationSpeed());
            this.animate(masquerader.getAnimationState("fangs"), MasqueraderAnimation.FANGS, ageInTicks, masquerader.getAnimationSpeed());
            this.animate(masquerader.getAnimationState("death"), MasqueraderAnimation.DEATH, ageInTicks, masquerader.getAnimationSpeed());
            this.animate(masquerader.getAnimationState("vex"), MasqueraderAnimation.VEX, ageInTicks, masquerader.getAnimationSpeed());

            this.cape.xRot = 0.1F + limbSwingAmount * 0.6F;

            AbstractIllager.IllagerArmPose armPose = masquerader.getArmPose();
            if (armPose == AbstractIllager.IllagerArmPose.ATTACKING) {
                if (masquerader.getMainHandItem().isEmpty()) {
                    AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, ageInTicks);
                } else {
                    AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, masquerader, this.attackTime, ageInTicks);
                }
            } else if (armPose == AbstractIllager.IllagerArmPose.SPELLCASTING) {
                this.rightArm.z = 0.0F;
                this.rightArm.x = -5.0F;
                this.leftArm.z = 0.0F;
                this.leftArm.x = 5.0F;
                this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
                this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
                this.rightArm.zRot = 2.3561945F;
                this.leftArm.zRot = -2.3561945F;
                this.rightArm.yRot = 0.0F;
                this.leftArm.yRot = 0.0F;
            } else if (armPose == AbstractIllager.IllagerArmPose.BOW_AND_ARROW) {
                this.rightArm.yRot = -0.1F + this.head.yRot;
                this.rightArm.xRot = (-(float) Math.PI / 2F) + this.head.xRot;
                this.leftArm.xRot = -0.9424779F + this.head.xRot;
                this.leftArm.yRot = this.head.yRot - 0.4F;
                this.leftArm.zRot = ((float) Math.PI / 2F);
            } else if (armPose == AbstractIllager.IllagerArmPose.CROSSBOW_HOLD) {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
            } else if (armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE) {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, ((AbstractIllager) entity), true);
            } else if (armPose == AbstractIllager.IllagerArmPose.CELEBRATING) {
                this.rightArm.z = 0.0F;
                this.rightArm.x = -5.0F;
                this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
                this.rightArm.zRot = 2.670354F;
                this.rightArm.yRot = 0.0F;
                this.leftArm.z = 0.0F;
                this.leftArm.x = 5.0F;
                this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
                this.leftArm.zRot = -2.3561945F;
                this.leftArm.yRot = 0.0F;
            }

            boolean flag = masquerader.shouldShowArms();
            this.arms.visible = !flag;
            this.leftArm.visible = flag;
            this.rightArm.visible = flag;

            var10000 = this.head;
            var10000.yRot += netHeadYaw * 0.017453292F;
            var10000.xRot += headPitch * 0.017453292F;

            if (this.riding) {
                this.leftLeg.xRot = -1.4137167F;
                this.leftLeg.yRot = -0.31415927F;
                this.leftLeg.zRot = -0.07853982F;
                this.rightLeg.xRot = -1.4137167F;
                this.rightLeg.yRot = 0.31415927F;
                this.rightLeg.zRot = 0.07853982F;
            } else if (!masquerader.isCharging()) {
                var10000 = this.rightLeg;
                var10000.xRot += Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
                var10000 = this.leftLeg;
                var10000.xRot += Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount * 0.5F;
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    private ModelPart getArm(HumanoidArm p_191216_1_) {
        return p_191216_1_ == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    @Override
    public void translateToHand(HumanoidArm p_102108_, PoseStack p_102109_) {
        this.root().translateAndRotate(p_102109_);
        this.body.translateAndRotate(p_102109_);
        this.getArm(p_102108_).translateAndRotate(p_102109_);
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }
}