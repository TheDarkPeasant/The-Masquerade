package net.random_something.masquerader_mod.entity;

import com.google.common.collect.Lists;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.random_something.masquerader_mod.Config;
import net.random_something.masquerader_mod.client.animation.ICanBeAnimated;
import net.random_something.masquerader_mod.sounds.SoundRegister;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Masquerader extends AbstractIllager implements RangedAttackMob, CrossbowAttackMob, ICanBeAnimated {
    private ServerBossEvent bossEvent;
    private static final EntityDataAccessor<Boolean> SHOULD_DELETE_ITSELF = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> NEARBY_ILLAGERS = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOW_ARMS = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> MASK = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> ATTACK_TYPE = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> USING_ATTACK = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> CROSSBOW_ATTACK = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> CHARGING_CROSSBOW = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Integer> SHOOTING_TIME = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> CROSSBOW_USING_TIME = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.INT);

    public AnimationState roarAnimationState = new AnimationState();
    public AnimationState potionAnimationState = new AnimationState();
    public AnimationState changeAnimationState = new AnimationState();
    public AnimationState crossbowAnimationState = new AnimationState();
    public AnimationState fangsAnimationState = new AnimationState();
    public AnimationState deathAnimationState = new AnimationState();
    public AnimationState vexAnimationState = new AnimationState();

    public static final int NO_MASK = 0;
    public static final int EVOKER_MASK = 1;
    public static final int ILLUSIONER_MASK = 2;
    public static final int RAVAGER_MASK = 3;
    public static final int WITCH_MASK = 4;

    List<Integer> availableMasks = new ArrayList<>();

    private static final int CHANGE_MASK_ATTACK = 1;
    private static final int VEX_ATTACK = 2;
    private static final int FANGS_ATTACK = 3;
    private static final int CLONE_ATTACK = 4;
    private static final int MIRROR_ATTACK = 5;
    private static final int BLIND_ATTACK = 6;
    private static final int ROAR_AND_CHARGE_ATTACK = 7;
    private static final int POTION_RAIN_ATTACK = 8;

    private int changeMaskCooldown;
    private int vexCooldown;
    private int fangsCooldown;
    private int cloneCooldown;
    private int mirrorCooldown;
    private int blindCooldown;
    private int roarAndChargeCooldown;
    private int potionRainCooldown;

    private boolean closeCircles = false;
    private boolean lines = false;
    private boolean farCircles = false;
    private boolean rapidBelow = false;

    protected final IllusionerMaskBowAttackGoal bowGoal = new IllusionerMaskBowAttackGoal(this, 0.5D, 20, 15.0F);
    protected float damageTaken = 0;
    private int clientSideIllusionTicks;
    private final Vec3[][] clientSideIllusionOffsets;
    public final List<MasqueraderClone> clones = new ArrayList<>();
    private int losTicks;

    private double chargeX;
    private double chargeZ;
    private boolean usedCrossbow = false;

    private static final UUID SPEED_MODIFIER_DRINKING_UUID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
    private static final AttributeModifier SPEED_MODIFIER_DRINKING = new AttributeModifier(SPEED_MODIFIER_DRINKING_UUID, "Drinking speed penalty", -0.25D, AttributeModifier.Operation.ADDITION);
    private static final EntityDataAccessor<Boolean> DRINKING = SynchedEntityData.defineId(Masquerader.class, EntityDataSerializers.BOOLEAN);
    private int usingTime;
    private boolean drankHeal = false;
    private boolean drankRegen = false;

    public int attackTicks;

    public boolean shouldDeleteItself() {
        return this.entityData.get(SHOULD_DELETE_ITSELF);
    }

    public void setShouldDeleteItself(boolean shouldDelete) {
        this.entityData.set(SHOULD_DELETE_ITSELF, shouldDelete);
    }

    public boolean areIllagersNearby() {
        return this.entityData.get(NEARBY_ILLAGERS);
    }

    public boolean shouldShowArms() {
        return this.entityData.get(SHOW_ARMS);
    }

    public void setShowArms(boolean showArms) {
        this.entityData.set(SHOW_ARMS, showArms);
    }

    public void setIllagersNearby(boolean illagersNearby) {
        this.entityData.set(NEARBY_ILLAGERS, illagersNearby);
    }

    public int getMask() {
        return this.entityData.get(MASK);
    }

    public void setMask(int newMask) {
        this.entityData.set(MASK, newMask);
    }

    public boolean isUsingAttack() {
        return this.entityData.get(USING_ATTACK);
    }

    public void setUsingAttack(boolean usingAttack) {
        this.entityData.set(USING_ATTACK, usingAttack);
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public void setRoaring(boolean roaring) {
        this.entityData.set(ROARING, roaring);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    public boolean isCrossbowAttack() {
        return this.entityData.get(CROSSBOW_ATTACK);
    }

    public void setCrossbowAttack(boolean crossbowAttack) {
        this.entityData.set(CROSSBOW_ATTACK, crossbowAttack);
    }

    public int getCrossbowUsingTime() {
        return this.entityData.get(CROSSBOW_USING_TIME);
    }

    public void setCrossbowUsingTime(int usingTime) {
        this.entityData.set(CROSSBOW_USING_TIME, usingTime);
    }

    public int getShootingTime() {
        return this.entityData.get(SHOOTING_TIME);
    }

    public void setShootingTime(int shootingTime) {
        this.entityData.set(SHOOTING_TIME, shootingTime);
    }

    public void setChargingCrossbow(boolean chargingCrossbow) {
        this.entityData.set(CHARGING_CROSSBOW, chargingCrossbow);
    }

    public void setDrinkingPotion(boolean p_34164_) {
        this.getEntityData().set(DRINKING, p_34164_);
    }

    public boolean isDrinkingPotion() {
        return this.getEntityData().get(DRINKING);
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity p_33275_, ItemStack p_33276_, Projectile p_33277_, float p_33278_) {
        this.shootCrossbowProjectile(this, p_33275_, p_33277_, p_33278_, 1.6F);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    public int getAttackType() {
        return this.getEntityData().get(ATTACK_TYPE);
    }

    public void setAttackType(int attackType) {
        this.entityData.set(ATTACK_TYPE, attackType);
    }

    protected Masquerader(EntityType<? extends AbstractIllager> p_32105_, Level p_32106_) {
        super(p_32105_, p_32106_);
        this.xpReward = 70;

        if (Config.bossBar.get()) {
            bossEvent = (ServerBossEvent) (new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
            bossEvent.setVisible(false);
        }

        this.clientSideIllusionOffsets = new Vec3[2][4];

        for (int i = 0; i < 4; ++i) {
            this.clientSideIllusionOffsets[0][i] = Vec3.ZERO;
            this.clientSideIllusionOffsets[1][i] = Vec3.ZERO;
        }

        this.goalSelector.addGoal(5, bowGoal);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.4D).add(Attributes.FOLLOW_RANGE, 64.0D).add(Attributes.MAX_HEALTH, 300.0D);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if (Config.bossBar.get()) bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public int getArmorValue() {
        return getMask() == RAVAGER_MASK ? 15 + super.getArmorValue() : super.getArmorValue();
    }

    @Override
    public boolean isLeftHanded() {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer p_20119_) {
        super.startSeenByPlayer(p_20119_);
        if (Config.bossBar.get()) this.bossEvent.addPlayer(p_20119_);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p_20119_) {
        super.stopSeenByPlayer(p_20119_);
        if (Config.bossBar.get()) this.bossEvent.removePlayer(p_20119_);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StareAtTargetGoal());

        //No Mask
        this.goalSelector.addGoal(1, new ChangeMaskGoal());
        this.goalSelector.addGoal(2, new NoMaskMeleeGoal(this, 1.0, true));

        //Evoker Mask
        this.goalSelector.addGoal(2, new EvokerMaskVexGoal());
        this.goalSelector.addGoal(3, new EvokerMaskFangsGoal());
        this.goalSelector.addGoal(4, new EvokerMaskAvoidEntityGoal(this, Player.class, 8.0F, 0.6D, 1.0D));

        //Illusioner Mask
        this.goalSelector.addGoal(2, new IllusionerMaskCloneGoal());
        this.goalSelector.addGoal(3, new IllusionerMaskMirrorGoal());
        this.goalSelector.addGoal(4, new IllusionerMaskBlindGoal());

        //Ravager Mask
        this.goalSelector.addGoal(2, new RavagerMaskRoarAndChargeGoal());
        this.goalSelector.addGoal(3, new RavagerMaskCrossbowGoal(this, 1.0D, 8.0F));
        this.goalSelector.addGoal(4, new RavagerMaskMeleeGoal(this, 1.0, true));

        //Witch Mask
        this.goalSelector.addGoal(2, new WitchMaskPotionRainGoal());
        this.goalSelector.addGoal(3, new WitchMaskThrowPotionGoal(this, 1.0D, 40, 10.0F));

        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.4D, 0.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        if (!(this instanceof MasqueraderClone))
            this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
        if (!(this instanceof MasqueraderClone))
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true).setUnseenMemoryTicks(300));
        if (!(this instanceof MasqueraderClone))
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true).setUnseenMemoryTicks(300));
        if (!(this instanceof MasqueraderClone))
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true).setUnseenMemoryTicks(300));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_37856_, DifficultyInstance p_37857_, MobSpawnType p_37858_, @Nullable SpawnGroupData p_37859_, @Nullable CompoundTag p_37860_) {
        if (p_37858_ == MobSpawnType.EVENT) {
            this.setShouldDeleteItself(true);
        }

        return super.finalizeSpawn(p_37856_, p_37857_, p_37858_, p_37859_, p_37860_);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("mask", this.getMask());
        tag.putInt("maskCooldown", changeMaskCooldown);
        tag.putInt("cloneCooldown", cloneCooldown);
        tag.putBoolean("drankHeal", drankHeal);
        tag.putBoolean("drankRegen", drankRegen);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (Config.bossBar.get()) this.bossEvent.setName(this.getDisplayName());
        this.setMask(tag.getInt("mask"));
        changeMaskCooldown = tag.getInt("maskCooldown");
        cloneCooldown = tag.getInt("cloneCooldown");
        drankHeal = tag.getBoolean("drankHeal");
        drankRegen = tag.getBoolean("drankRegen");
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOULD_DELETE_ITSELF, false);
        this.entityData.define(NEARBY_ILLAGERS, false);
        this.entityData.define(SHOW_ARMS, false);
        this.entityData.define(ANIMATION_STATE, 0);
        this.entityData.define(MASK, NO_MASK);
        this.entityData.define(ATTACK_TYPE, 0);
        this.entityData.define(USING_ATTACK, false);
        this.entityData.define(ROARING, false);
        this.entityData.define(CHARGING, false);
        this.entityData.define(CROSSBOW_ATTACK, false);
        this.entityData.define(CHARGING_CROSSBOW, false);
        this.entityData.define(CROSSBOW_USING_TIME, 0);
        this.entityData.define(SHOOTING_TIME, 0);
        this.entityData.define(DRINKING, false);
    }

    @Override
    public IllagerArmPose getArmPose() {
        if (getAttackType() == CLONE_ATTACK || getAttackType() == MIRROR_ATTACK || getAttackType() == BLIND_ATTACK) {
            setShowArms(true);
            return IllagerArmPose.SPELLCASTING;
        }
        if (this.isAggressive() && (this.getMainHandItem().is(Items.IRON_SWORD) || this.getMainHandItem().is(Items.IRON_AXE)) && !isUsingAttack()) {
            setShowArms(true);
            return IllagerArmPose.ATTACKING;
        }
        if (this.isAggressive() && this.getMainHandItem().is(Items.BOW) && !isUsingAttack()) {
            setShowArms(true);
            return IllagerArmPose.BOW_AND_ARROW;
        }
        if (!this.isAggressive() && !isUsingAttack() && this.isCelebrating()) {
            setShowArms(true);
            return IllagerArmPose.CELEBRATING;
        }

        if (!isUsingAttack() && !isCrossbowAttack() && this.isAlive()) setShowArms(false);
        return IllagerArmPose.CROSSED;
    }

    @Override
    public void applyRaidBuffs(int p_37844_, boolean p_37845_) {
    }

    @Override
    public boolean fireImmune() {
        return getMask() == RAVAGER_MASK;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (getMask() == WITCH_MASK) {
            return effect.getEffect().isBeneficial();
        }

        if (getMask() != ILLUSIONER_MASK) {
            return effect.getEffect() != MobEffects.INVISIBILITY;
        }

        return super.canBeAffected(effect);
    }

    @Override
    protected float getEquipmentDropChance(EquipmentSlot p_21520_) {
        if (getMask() == WITCH_MASK) return 2.0f;
        return super.getEquipmentDropChance(p_21520_);
    }

    @Override
    protected void checkFallDamage(double p_20990_, boolean p_20991_, BlockState p_20992_, BlockPos p_20993_) {
        if (!clones.isEmpty()) return;
        super.checkFallDamage(p_20990_, p_20991_, p_20992_, p_20993_);
    }

    @Override
    public boolean canBeLeader() {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos p_20135_, BlockState p_20136_) {
        if (!isCharging()) {
            super.playStepSound(p_20135_, p_20136_);
        } else {
            this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33034_) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegister.MASQUERADER_DEATH.get();
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundRegister.MASQUERADER_LAUGH.get();
    }

    @Override
    public boolean canAttack(LivingEntity entity) {
        return !(entity instanceof MaskedVillager) && super.canAttack(entity);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        this.doMirrorStuff();

        if (getMask() == ILLUSIONER_MASK && this.getTarget() != null && !this.hasLineOfSight(this.getTarget())) {
            losTicks++;
        }
        if (losTicks >= 40 && this.getTarget() != null) {
            for (int i = 0; i < 64; i++) {
                if (this.teleport(this.getTarget())) {
                    if (!this.isSilent()) {
                        this.level.playSound(null, this.xo, this.yo, this.zo, SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F);
                        this.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
                    }
                    losTicks = 0;
                    break;
                }
            }
        }

        if (!this.level.isClientSide && availableMasks.size() < 1) {
            availableMasks.add(EVOKER_MASK);
            availableMasks.add(ILLUSIONER_MASK);
            availableMasks.add(RAVAGER_MASK);
            availableMasks.add(WITCH_MASK);
        }

        if (!(this instanceof MasqueraderClone) && !this.level.isClientSide && availableMasks.contains(getMask())) {
            availableMasks.remove((Integer) getMask());
        }

        if (getAttackType() == CHANGE_MASK_ATTACK) {
            if (attackTicks <= 40) {
                makeChangeMaskParticles(this.level, this.random.nextDouble(), this.random.nextDouble(), this.random.nextDouble(), attackTicks);
            }

            if (attackTicks == 40 && this.isAlive()) {
                makeFinishChangeMaskParticles(this.level);
                this.playSound(SoundEvents.EVOKER_CAST_SPELL, 5.0f, 1.0f);
                this.playSound(SoundRegister.MASQUERADER_LAUGH.get(), 3.0f, 1.0f);
                this.playSound(SoundEvents.ENDER_DRAGON_FLAP, 5.0f, 0.8F + this.random.nextFloat() * 0.3F);

                for (Entity entity : this.level.getEntities(this, this.getBoundingBox().inflate(15.0))) {
                    if (entity.isAlive()) {
                        double x = this.getX() - entity.getX();
                        double y = this.getY() - entity.getY();
                        double z = this.getZ() - entity.getZ();
                        double d = Math.sqrt(x * x + y * y + z * z);
                        if (this.distanceToSqr(entity) < 16.0) {
                            entity.hurtMarked = true;
                            entity.hurt(DamageSource.indirectMagic(this, entity), 5);
                            entity.setDeltaMovement(-x / d * 4.0, -y / d * 0.5, -z / d * 4.0);
                            entity.lerpMotion(-x / d * 4.0, -y / d * 0.5, -z / d * 4.0);
                        }
                    }
                }

                if (!this.level.isClientSide) {
                    int newMask = availableMasks.get(random.nextInt(0, availableMasks.size()));
                    while (newMask == getMask()) {
                        availableMasks.remove((Integer) newMask);
                        newMask = availableMasks.get(random.nextInt(0, availableMasks.size()));
                    }
                    if (this.hasEffect(MobEffects.INVISIBILITY)) {
                        this.removeEffect(MobEffects.INVISIBILITY);
                    }
                    if (newMask == RAVAGER_MASK) roarAndChargeCooldown = 20;
                    if (newMask == WITCH_MASK) {
                        drankRegen = false;
                        drankHeal = false;
                    }
                    killAllClones();

                    setMask(newMask);

                    if (Config.bossBar.get()) {
                        switch (newMask) {
                            case EVOKER_MASK -> bossEvent.setColor(BossEvent.BossBarColor.YELLOW);
                            case ILLUSIONER_MASK -> bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
                            case RAVAGER_MASK -> bossEvent.setColor(BossEvent.BossBarColor.RED);
                            case WITCH_MASK -> bossEvent.setColor(BossEvent.BossBarColor.GREEN);
                        }
                        if (!bossEvent.isVisible() && !this.hasActiveRaid()) bossEvent.setVisible(true);
                    }
                }
            }

            if (attackTicks == 49) {
                setAnimationState(0);
                setShowArms(false);
            }
        }

        if (getAttackType() != CHANGE_MASK_ATTACK) setMask(getMask());

        this.ensureCorrectEquipment();

        if (getMask() == RAVAGER_MASK && doesAttackMeetNormalRequirements() && !usedCrossbow && roarAndChargeCooldown < 260 && roarAndChargeCooldown > 180 && this.isAlive() && this.getOffhandItem().is(Items.CROSSBOW)) {
            setCrossbowAttack(true);
            usedCrossbow = true;
        }
        if (changeMaskCooldown == 200) usedCrossbow = false;
        if (this.getMainHandItem().is(Items.CROSSBOW)) {
            setCrossbowUsingTime(getCrossbowUsingTime() + 1);
            if (getCrossbowUsingTime() >= 100) {
                this.setCrossbowAttack(false);
                setShootingTime(-5);
                setCrossbowUsingTime(0);
            }
        }

        if (getMask() == WITCH_MASK) this.doDrinking();
    }

    private void doDrinking() {
        if (!this.level.isClientSide && this.isAlive()) {
            if (this.isDrinkingPotion() && !isUsingAttack()) {
                if (this.usingTime-- <= 0) {
                    this.setDrinkingPotion(false);
                    ItemStack itemstack = this.getMainHandItem();
                    this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    if (itemstack.is(Items.POTION)) {
                        MobEffectInstance mobeffectinstance = PotionUtils.getMobEffects(itemstack).get(0);
                        if (mobeffectinstance.getEffect() == MobEffects.REGENERATION) {
                            this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, changeMaskCooldown, 2));
                            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, changeMaskCooldown, 1));
                        } else if (mobeffectinstance.getEffect() != MobEffects.HEAL) {
                            this.addEffect(new MobEffectInstance(mobeffectinstance.getEffect(), changeMaskCooldown, mobeffectinstance.getAmplifier()));
                        } else {
                            this.addEffect(new MobEffectInstance(mobeffectinstance.getEffect(), 2, mobeffectinstance.getAmplifier()));
                        }
                    }

                    this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_DRINKING);
                }
            }
            if (!areIllagersNearby() && !this.isDrinkingPotion() && !isUsingAttack()) {
                Potion potion = null;
                if (this.random.nextFloat() < 0.15F && this.isEyeInFluid(FluidTags.WATER) && !this.hasEffect(MobEffects.WATER_BREATHING)) {
                    potion = Potions.WATER_BREATHING;
                } else if (this.random.nextFloat() < 0.15F && (this.isOnFire() || this.getLastDamageSource() != null && this.getLastDamageSource().isFire()) && !this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    potion = Potions.FIRE_RESISTANCE;
                } else if (this.random.nextFloat() < 0.5F && this.getTarget() != null && !this.hasEffect(MobEffects.MOVEMENT_SPEED) && this.getTarget().distanceToSqr(this) > 625.0D) {
                    potion = Potions.SWIFTNESS;
                }

                if (potion != null) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
                    this.usingTime = this.getMainHandItem().getUseDuration();
                    this.setDrinkingPotion(true);
                    if (!this.isSilent()) {
                        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                    }

                    AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
                    assert attributeinstance != null;
                    attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
                    attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
                }
            }
            if (!areIllagersNearby() && !this.isDrinkingPotion() && !isUsingAttack() && !drankRegen && potionRainCooldown > 0) {
                Potion potion = Potions.STRONG_REGENERATION;
                this.setItemSlot(EquipmentSlot.MAINHAND, PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
                this.usingTime = this.getMainHandItem().getUseDuration();
                this.setDrinkingPotion(true);
                if (!this.isSilent()) {
                    this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                }

                AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
                assert attributeinstance != null;
                attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
                attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
                drankRegen = true;
            }
            if (!areIllagersNearby() && !this.isDrinkingPotion() && !isUsingAttack() && !drankHeal && changeMaskCooldown < 300) {
                Potion potion = null;
                if (this.getHealth() < this.getMaxHealth()) {
                    potion = Potions.STRONG_HEALING;
                }

                if (potion != null) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, PotionUtils.setPotion(new ItemStack(Items.POTION), potion));
                    this.usingTime = this.getMainHandItem().getUseDuration();
                    this.setDrinkingPotion(true);
                    if (!this.isSilent()) {
                        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_DRINK, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                    }

                    AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
                    assert attributeinstance != null;
                    attributeinstance.removeModifier(SPEED_MODIFIER_DRINKING);
                    attributeinstance.addTransientModifier(SPEED_MODIFIER_DRINKING);
                    drankHeal = true;
                }
            }
        }
    }

    private void doMirrorStuff() {
        if (this.level.isClientSide && this.isInvisible()) {
            --this.clientSideIllusionTicks;
            if (this.clientSideIllusionTicks < 0) {
                this.clientSideIllusionTicks = 0;
            }

            if (this.hurtTime != 1 && this.tickCount % 1200 != 0) {
                if (this.hurtTime == this.hurtDuration - 1) {
                    this.clientSideIllusionTicks = 3;

                    for (int k = 0; k < 4; ++k) {
                        this.clientSideIllusionOffsets[0][k] = this.clientSideIllusionOffsets[1][k];
                        this.clientSideIllusionOffsets[1][k] = new Vec3(0.0D, 0.0D, 0.0D);
                    }
                }
            } else {
                this.clientSideIllusionTicks = 3;

                for (int j = 0; j < 4; ++j) {
                    this.clientSideIllusionOffsets[0][j] = this.clientSideIllusionOffsets[1][j];
                    this.clientSideIllusionOffsets[1][j] = new Vec3((double) (-6.0F + (float) this.random.nextInt(13)) * 0.5D, Math.max(0, this.random.nextInt(6) - 4), (double) (-6.0F + (float) this.random.nextInt(13)) * 0.5D);
                }

                for (int l = 0; l < 16; ++l) {
                    this.level.addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5D), this.getRandomY(), this.getZ(0.5D), 0.0D, 0.0D, 0.0D);
                }

                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F, false);
            }
        }
    }

    private boolean hasWrongEquipment() {
        if (getMask() == NO_MASK && (!this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.IRON_SWORD) || !this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty())) {
            return true;
        }
        if (getMask() == EVOKER_MASK && (!this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || !this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty())) {
            return true;
        }
        if (getMask() == WITCH_MASK && ((!this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && !this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.POTION) && !this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.SPLASH_POTION)) || (!this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() && !this.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.LINGERING_POTION)))) {
            return true;
        }
        if (getMask() == ILLUSIONER_MASK && (!this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.BOW) || !this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty())) {
            return true;
        }
        if (getMask() == RAVAGER_MASK && !isCrossbowAttack() && (!this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.IRON_AXE) || !this.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.CROSSBOW))) {
            return true;
        } else
            return getMask() == RAVAGER_MASK && isCrossbowAttack() && (!this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.CROSSBOW) || !this.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.IRON_AXE));
    }

    public void ensureCorrectEquipment() {
        if (getMask() == NO_MASK && hasWrongEquipment()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        } else if ((getMask() == EVOKER_MASK || getMask() == WITCH_MASK) && hasWrongEquipment()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        } else if (getMask() == ILLUSIONER_MASK && hasWrongEquipment()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        } else if (getMask() == RAVAGER_MASK && hasWrongEquipment()) {
            if (!isCrossbowAttack()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.CROSSBOW));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.IRON_AXE));
            }
        }
    }

    public Vec3[] getIllusionOffsets(float p_32940_) {
        if (this.clientSideIllusionTicks <= 0) {
            return this.clientSideIllusionOffsets[1];
        } else {
            double d0 = ((float) this.clientSideIllusionTicks - p_32940_) / 3.0F;
            d0 = Math.pow(d0, 0.25D);
            Vec3[] avec3 = new Vec3[4];

            for (int i = 0; i < 4; ++i) {
                avec3[i] = this.clientSideIllusionOffsets[1][i].scale(1.0D - d0).add(this.clientSideIllusionOffsets[0][i].scale(d0));
            }

            return avec3;
        }
    }

    private void makeSpellParticles(Level level, float red, float green, float blue) {
        if (level.isClientSide) return;

        if (level instanceof ServerLevel serverLevel) {
            float f = this.yBodyRot * ((float) Math.PI / 180F) + Mth.cos((float) this.tickCount * 0.6662F) * 0.25F;
            float f1 = Mth.cos(f);
            float f2 = Mth.sin(f);

            double x1 = this.getX() + f1 * 0.6D;
            double y1 = this.getY() + 1.8D;
            double z1 = this.getZ() + f2 * 0.6D;
            double x2 = this.getX() - f1 * 0.6D;
            double y2 = this.getY() + 1.8D;
            double z2 = this.getZ() - f2 * 0.6D;

            ClientboundLevelParticlesPacket packet1 = new ClientboundLevelParticlesPacket(ParticleTypes.ENTITY_EFFECT, false, x1, y1, z1, red, green, blue, 1.0F, 0);
            ClientboundLevelParticlesPacket packet2 = new ClientboundLevelParticlesPacket(ParticleTypes.ENTITY_EFFECT, false, x2, y2, z2, red, green, blue, 1.0F, 0);

            List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
            for (ServerPlayer player : players) {
                player.connection.send(packet1);
                player.connection.send(packet2);
            }
        }
    }

    private void makeChangeMaskParticles(Level level, double red, double green, double blue, int quantity) {
        if (level.isClientSide) return;

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < quantity; i++) {
                double x = this.getRandomX(1.0) + (-0.5 + this.random.nextDouble()) * 2.5;
                double y = this.getRandomY() + (-0.5 + this.random.nextDouble()) * 1.5;
                double z = this.getRandomZ(1.0) + (-0.5 + this.random.nextDouble()) * 2.5;

                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.ENTITY_EFFECT, false, x, y, z, (float) red, (float) green, (float) blue, 1.0F, 0);

                List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
                for (ServerPlayer player : players) {
                    player.connection.send(packet);
                }
            }
        }
    }

    private void makeFinishChangeMaskParticles(Level level) {
        if (level.isClientSide) return;

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 30; ++i) {

                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.EXPLOSION_EMITTER, false, this.getX(), this.getY(), this.getZ(), 0, 0, 0, 1.0F, 0);

                List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
                for (ServerPlayer player : players) {
                    player.connection.send(packet);
                }
            }
        }
    }

    private void makeCircle(double centerX, double centerZ, double radius, int segments, int warmupDelay, float angleOffset) {
        LivingEntity target = this.getTarget();
        double d0 = Math.min(target.getY(), this.getY());
        double d1 = Math.max(target.getY(), this.getY()) + 1.0D;
        float f = (float) Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX());
        for (int k = 0; k < segments; ++k) {
            float f2 = f + (float) k * (float) Math.PI * 2.0F / segments + angleOffset;
            this.createSpellEntity(centerX + (double) Mth.cos(f2) * radius, centerZ + (double) Mth.sin(f2) * radius, d0, d1, f2, warmupDelay);
        }
    }

    private void makeLine(int segments, float angleOffset, int additionalDelay) {
        LivingEntity target = this.getTarget();
        double d0 = Math.min(target.getY(), this.getY());
        double d1 = Math.max(target.getY(), this.getY()) + 1.0D;
        float f = (float) Mth.atan2(target.getZ() - this.getZ(), target.getX() - this.getX()) + angleOffset;
        for (int l = 0; l < segments; ++l) {
            double d2 = 1.25D * (double) (l + 1);
            this.createSpellEntity(this.getX() + (double) Mth.cos(f) * d2, this.getZ() + (double) Mth.sin(f) * d2, d0, d1, f, l + additionalDelay);
        }
    }

    private void createSpellEntity(double p_32673_, double p_32674_, double p_32675_, double p_32676_, float p_32677_, int p_32678_) {
        BlockPos blockpos = new BlockPos(p_32673_, p_32676_, p_32674_);
        boolean flag = false;
        double d0 = 0.0D;

        do {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = this.level.getBlockState(blockpos1);
            if (blockstate.isFaceSturdy(this.level, blockpos1, Direction.UP)) {
                if (!this.level.isEmptyBlock(blockpos)) {
                    BlockState blockstate1 = this.level.getBlockState(blockpos);
                    VoxelShape voxelshape = blockstate1.getCollisionShape(this.level, blockpos);
                    if (!voxelshape.isEmpty()) {
                        d0 = voxelshape.max(Direction.Axis.Y);
                    }
                }

                flag = true;
                break;
            }

            blockpos = blockpos.below();
        } while (blockpos.getY() >= Mth.floor(p_32675_) - 1);

        if (flag) {
            this.level.addFreshEntity(new EvokerFangs(this.level, p_32673_, (double) blockpos.getY() + d0, p_32674_, p_32677_, p_32678_, this));
        }
    }

    public void makeRoarParticles(Level level) {
        if (level.isClientSide) return;

        if (level instanceof ServerLevel serverLevel) {
            Vec3 vec3 = this.getBoundingBox().getCenter();
            for (int i = 0; i < 40; ++i) {
                double d0 = this.random.nextGaussian() * 0.2D;
                double d1 = this.random.nextGaussian() * 0.2D;
                double d2 = this.random.nextGaussian() * 0.2D;

                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.POOF, false, vec3.x, vec3.y, vec3.z, (float) d0, (float) d1, (float) d2, 1.0F, 0);

                List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
                for (ServerPlayer player : players) {
                    player.connection.send(packet);
                }
            }

            for (int i = 0; i < 40; ++i) {
                double d0 = this.random.nextGaussian() * 0.2D;
                double d1 = this.random.nextGaussian() * 0.2D;
                double d2 = this.random.nextGaussian() * 0.2D;

                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.FLAME, false, vec3.x, vec3.y, vec3.z, (float) d0, (float) d1, (float) d2, 1.0F, 0);

                List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
                for (ServerPlayer player : players) {
                    player.connection.send(packet);
                }
            }
        }
    }

    public void makeChargeParticles(Level level) {
        if (level.isClientSide) return;

        if (level instanceof ServerLevel serverLevel) {
            Vec3 feet = this.position();
            double d0 = this.random.nextGaussian() * 0.2D;
            double d1 = this.random.nextGaussian() * 0.2D;
            double d2 = this.random.nextGaussian() * 0.2D;

            ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.POOF, false, feet.x, feet.y, feet.z, (float) d0, (float) d1, (float) d2, 1.0F, 0);

            List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
            for (ServerPlayer player : players) {
                player.connection.send(packet);
            }

            double x = this.getRandomX(1.0) + (-0.5 + this.random.nextDouble()) * 2.5;
            double y = this.getRandomY() + (-0.5 + this.random.nextDouble()) * 1.5;
            double z = this.getRandomZ(1.0) + (-0.5 + this.random.nextDouble()) * 2.5;

            ClientboundLevelParticlesPacket packet3 = new ClientboundLevelParticlesPacket(ParticleTypes.SMOKE, false, x, y, z, 0, 0, 0, 1.0F, 0);

            List<ServerPlayer> players3 = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
            for (ServerPlayer player : players3) {
                player.connection.send(packet3);
            }

            double x2 = this.getRandomX(1.0) + (-0.5 + this.random.nextDouble()) * 2.5;
            double y2 = this.getRandomY() + (-0.5 + this.random.nextDouble()) * 1.5;
            double z2 = this.getRandomZ(1.0) + (-0.5 + this.random.nextDouble()) * 2.5;

            ClientboundLevelParticlesPacket packet2 = new ClientboundLevelParticlesPacket(ParticleTypes.FLAME, false, x2, y2, z2, 0, 0, 0, 1.0F, 0);

            List<ServerPlayer> players2 = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
            for (ServerPlayer player : players2) {
                player.connection.send(packet2);
            }
        }
    }

    public void makeExplodeParticles(Level level) {
        if (level.isClientSide) return;

        if (level instanceof ServerLevel serverLevel) {
            int i;
            double d0;
            double d1;
            double d2;
            for (i = 0; i < 250; ++i) {
                d0 = (-0.5 + this.random.nextGaussian()) / 2.0;
                d1 = (-0.5 + this.random.nextGaussian()) / 2.0;
                d2 = (-0.5 + this.random.nextGaussian()) / 2.0;

                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.POOF, false, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), (float) d0, (float) d1, (float) d2, 1.0F, 0);

                List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
                for (ServerPlayer player : players) {
                    player.connection.send(packet);
                }
            }

            for (i = 0; i < 200; ++i) {
                d0 = (-0.5 + this.random.nextGaussian()) / 2.0;
                d1 = (-0.5 + this.random.nextGaussian()) / 2.0;
                d2 = (-0.5 + this.random.nextGaussian()) / 2.0;

                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.LARGE_SMOKE, false, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), (float) d0, (float) d1, (float) d2, 1.0F, 0);

                List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
                for (ServerPlayer player : players) {
                    player.connection.send(packet);
                }
            }

            for (i = 0; i < 150; ++i) {
                d0 = (-0.5 + this.random.nextGaussian()) / 2.0;
                d1 = (-0.5 + this.random.nextGaussian()) / 2.0;
                d2 = (-0.5 + this.random.nextGaussian()) / 2.0;

                ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(ParticleTypes.FLAME, false, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), (float) d0, (float) d1, (float) d2, 1.0F, 0);

                List<ServerPlayer> players = serverLevel.getPlayers(p -> p.distanceToSqr(this) < 64 * 64);
                for (ServerPlayer player : players) {
                    player.connection.send(packet);
                }
            }
        }
    }

    @Override
    public void tick() {
        maxUpStep = isCharging() ? 1 : 0.6f;

        this.checkIfShouldDelete();

        this.doForcefield();

        if (this.hasActiveRaid() && Config.bossBar.get() && bossEvent.isVisible()) {
            bossEvent.setVisible(false);
        }

        if (getAttackType() > 0) {
            ++this.attackTicks;
        } else {
            this.attackTicks = 0;
        }

        if (!areIllagersNearby()) this.updateCooldowns();

        this.doAttacks();

        super.tick();
    }

    private void checkIfShouldDelete() {
        if (this.hasActiveRaid()) {
            if (Config.masqueraderOnlyOneAllowed.get() && this.getCurrentRaid() != null && this.getCurrentRaid().getGroupsSpawned() == 7 && this.shouldDeleteItself()) {
                this.getCurrentRaid().removeFromRaid(this, true);
                if (!this.level.isClientSide) {
                    this.discard();
                }
            }

            if (this.getCurrentRaid() != null) {
                this.getCurrentRaid().updateBossbar();
            }
        }
    }

    private void doForcefield() {
        List<Raider> list = this.level.getEntitiesOfClass(Raider.class, this.getBoundingBox().inflate(100.0D), (predicate) -> predicate.hasActiveRaid() && !(predicate instanceof Masquerader));
        if (this.hasActiveRaid() && Config.masqueraderForcefield.get()) {
            if (!this.level.isClientSide) {
                this.setIllagersNearby(!list.isEmpty());
            }
            if (!list.isEmpty()) {
                this.setTarget(null);
            }
        }
        List<Raider> list2 = this.level.getEntitiesOfClass(Raider.class, this.getBoundingBox().inflate(100.0D), (predicate) -> predicate instanceof MaskedIllager);
        if (!this.hasActiveRaid()) {
            if (!this.level.isClientSide) {
                this.setIllagersNearby(!list2.isEmpty());
            }
        }

        if (this.areIllagersNearby()) {
            List<Mob> mobs = this.level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(100.0D));
            for (Mob attacker : mobs) {
                if (attacker.getLastHurtByMob() == this) {
                    attacker.setLastHurtByMob(null);
                }

                if (attacker.getTarget() == this) {
                    attacker.setTarget(null);
                }
            }
        }
    }

    private void updateCooldowns() {
        if (this.changeMaskCooldown > 0) {
            --this.changeMaskCooldown;
        }
        if (this.vexCooldown > 0) {
            --this.vexCooldown;
        }
        if (this.fangsCooldown > 0) {
            --this.fangsCooldown;
        }
        if (this.cloneCooldown > 0) {
            --this.cloneCooldown;
        }
        if (this.mirrorCooldown > 0) {
            --this.mirrorCooldown;
        }
        if (this.blindCooldown > 0) {
            --this.blindCooldown;
        }
        if (this.roarAndChargeCooldown > 0) {
            --this.roarAndChargeCooldown;
        }
        if (this.potionRainCooldown > 0) {
            --this.potionRainCooldown;
        }
    }

    private void doAttacks() {
        switch (getAttackType()) {
            case VEX_ATTACK -> doVexAttack();
            case FANGS_ATTACK -> doFangsAttack();
            case CLONE_ATTACK -> doCloneAttack();
            case MIRROR_ATTACK -> doMirrorAttack();
            case BLIND_ATTACK -> doBlindAttack();
            case ROAR_AND_CHARGE_ATTACK -> doRoarAndChargeAttack();
            case POTION_RAIN_ATTACK -> doPotionRainAttack();
        }
    }

    private void doVexAttack() {
        makeChangeMaskParticles(this.level, 0.7f, 0.7f, 0.8f, 5);
        if ((attackTicks == 20 || attackTicks == 25 || attackTicks == 30 || attackTicks == 35 || attackTicks == 40) && canTargetedSpellHappen()) {
            this.playSound(SoundEvents.EVOKER_CAST_SPELL);
            ServerLevel serverlevel = (ServerLevel) this.level;
            for (int i = 0; i < 3; ++i) {
                BlockPos blockpos = this.blockPosition().offset(-2 + this.random.nextInt(5), 1, -2 + this.random.nextInt(5));
                Vex vex = EntityType.VEX.create(this.level);
                assert vex != null;
                vex.moveTo(blockpos, 0.0F, 0.0F);
                vex.finalizeSpawn(serverlevel, this.level.getCurrentDifficultyAt(blockpos), MobSpawnType.MOB_SUMMONED, null, null);
                vex.setOwner(this);
                vex.setBoundOrigin(blockpos);
                vex.setHealth(2);
                Objects.requireNonNull(vex.getAttribute(Attributes.MAX_HEALTH)).addPermanentModifier(new AttributeModifier("Less masquerader vex health", -12, AttributeModifier.Operation.ADDITION));
                vex.setLimitedLife(changeMaskCooldown);
                if (getTeam() != null) {
                    this.level.getScoreboard().addPlayerToTeam(vex.getStringUUID(),
                            this.level.getScoreboard().getPlayerTeam(getTeam().getName()));
                }
                serverlevel.addFreshEntityWithPassengers(vex);
            }
        }
    }

    private void doFangsAttack() {
        if (attackTicks >= 20 && attackTicks <= 75) makeSpellParticles(this.level, 0.4f, 0.3f, 0.35f);
        if (canTargetedSpellHappen()) {
            LivingEntity target = this.getTarget();
            double masqueraderX = this.getX();
            double masqueraderZ = this.getZ();
            double targetX = target.getX();
            double targetZ = target.getZ();
            int randomSelection = closeCircles || lines || farCircles || rapidBelow ? -1 : this.random.nextInt(0, 3);
            if (closeCircles || (randomSelection != -1 && this.distanceToSqr(this.getTarget()) < 25.0D)) {
                closeCircles = true;
                if (attackTicks == 20 || attackTicks == 41 || attackTicks == 62) {
                    this.playSound(SoundEvents.EVOKER_CAST_SPELL);
                    makeCircle(masqueraderX, masqueraderZ, 1.5, 7, 0, 0);
                    makeCircle(masqueraderX, masqueraderZ, 3.25, 14, 7, 1.2566371F);
                    makeCircle(masqueraderX, masqueraderZ, 5.0, 18, 14, 0);
                }
            } else if (lines || (randomSelection == 0 && this.distanceToSqr(target) <= 256.0D)) {
                lines = true;
                if (attackTicks == 20 || attackTicks == 30 || attackTicks == 40 || attackTicks == 50 || attackTicks == 60 || attackTicks == 70 || attackTicks == 80) {
                    this.playSound(SoundEvents.EVOKER_CAST_SPELL);
                    makeLine(16, 0, 0);
                }
            } else if (farCircles || randomSelection == 1) {
                farCircles = true;
                if (attackTicks == 20 || attackTicks == 60) {
                    this.playSound(SoundEvents.EVOKER_CAST_SPELL);
                    makeCircle(targetX, targetZ, 5.0, 18, 0, 0);
                    makeCircle(targetX, targetZ, 3.25, 14, 5, 1.2566371F);
                    makeCircle(targetX, targetZ, 1.5, 7, 10, 0);
                    makeCircle(targetX, targetZ, 0.01, 1, 10, 0);
                    makeCircle(targetX, targetZ, 1.5, 7, 20, 0);
                    makeCircle(targetX, targetZ, 0.01, 1, 20, 0);
                    makeCircle(targetX, targetZ, 3.25, 14, 25, 1.2566371F);
                    makeCircle(targetX, targetZ, 5.0, 18, 30, 0);
                }
            } else {
                rapidBelow = true;
                if (attackTicks == 20) this.playSound(SoundEvents.EVOKER_CAST_SPELL);
                if (attackTicks >= 20) {
                    double d0 = Math.min(target.getY(), this.getY());
                    double d1 = Math.max(target.getY(), this.getY()) + 1.0D;
                    this.createSpellEntity(targetX, targetZ, d0, d1, 0, 0);
                }
            }
        }
    }

    private void doCloneAttack() {
        makeSpellParticles(this.level, 0.47f, 0.34f, 0.46f);
        if (attackTicks == 20 && canTargetedSpellHappen()) {
            this.playSound(SoundEvents.EVOKER_CAST_SPELL);

            for (int i = 0; i < 9; i++) {
                MasqueraderClone clone = new MasqueraderClone(EntityRegister.MASQUERADER_CLONE.get(), this.level, this, Math.max(changeMaskCooldown / 2, 300));
                clone.setPos(this.position());
                clones.add(clone);
                if (getTeam() != null) {
                    this.level.getScoreboard().addPlayerToTeam(clone.getStringUUID(),
                            this.level.getScoreboard().getPlayerTeam(getTeam().getName()));
                }
                this.level.addFreshEntity(clone);
                for (int j = 0; j < 64; ++j) {
                    if (clone.teleport(this.getTarget())) {
                        if (!clone.isSilent()) {
                            clone.level.playSound(null, clone.xo, clone.yo, clone.zo, SoundEvents.ILLUSIONER_MIRROR_MOVE, clone.getSoundSource(), 1.0F, 1.0F);
                            clone.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
                        }
                        break;
                    }
                }
            }

            for (int i = 0; i < 64; ++i) {
                if (this.teleport(this.getTarget())) {
                    this.clearFire();
                    if (!this.isSilent()) {
                        this.level.playSound(null, this.xo, this.yo, this.zo, SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F);
                        this.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
                    }
                    break;
                }
            }
            this.distractAttackers(clones.get(this.random.nextInt(0, clones.size())));
        }
    }

    private void doMirrorAttack() {
        makeSpellParticles(this.level, 0.3f, 0.3f, 0.8f);
        if (attackTicks == 20 && this.isAlive()) {
            this.playSound(SoundEvents.EVOKER_CAST_SPELL);
            this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 800));
        }
    }

    private void doBlindAttack() {
        makeSpellParticles(this.level, 0.1f, 0.1f, 0.2f);
        if (attackTicks == 20 && canTargetedSpellHappen()) {
            this.playSound(SoundEvents.EVOKER_CAST_SPELL);
            this.getTarget().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, Math.min(changeMaskCooldown, 300)));
        }
    }

    private void doRoarAndChargeAttack() {
        if (attackTicks == 8) {
            Masquerader.this.makeRoarParticles(Masquerader.this.level);
            Masquerader.this.playSound(SoundEvents.RAVAGER_ROAR);
        }

        if (attackTicks >= 8 && attackTicks < 18) {
            if (!isRoaring()) setRoaring(true);
            for (Entity entity : this.level.getEntities(this, this.getBoundingBox().inflate(15.0), this::hasLineOfSight)) {
                if (entity.isAlive()) {
                    double x = this.getX() - entity.getX();
                    double y = this.getY() - entity.getY();
                    double z = this.getZ() - entity.getZ();
                    double d = Math.sqrt(x * x + y * y + z * z);
                    if (this.distanceToSqr(entity) < 25.0) {
                        entity.hurtMarked = true;
                        if (entity instanceof LivingEntity)
                            entity.hurt(DamageSource.mobAttack(this), 5);
                        entity.setSecondsOnFire(8);
                        entity.setDeltaMovement(-x / d * 4.0, -y / d * 0.5, -z / d * 4.0);
                        entity.lerpMotion(-x / d * 4.0, -y / d * 0.5, -z / d * 4.0);
                    }
                }
            }
        }
        if (attackTicks == 20 && canTargetedSpellHappen()) {
            LivingEntity target = this.getTarget();
            double deltaX = target.getX() - this.getX();
            double deltaY = target.getY() + target.getEyeHeight() - this.getY() - this.getEyeHeight();
            double deltaZ = target.getZ() - this.getZ();
            double distanceForFacing = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            float yaw = (float) (Math.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;
            float pitch = (float) (-(Math.atan2(deltaY, distanceForFacing) * (180 / Math.PI)));

            this.setYRot(yaw);
            this.setXRot(pitch);
            this.yHeadRot = yaw;
            this.yBodyRot = yaw;

            double targetX = target.getX() - this.getX();
            double targetY = target.getY() - this.getY();
            double targetZ = target.getZ() - this.getZ();
            double distance = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
            float power = 4.5F;
            double motionX = (targetX / distance) * power * 0.2;
            double motionZ = (targetZ / distance) * power * 0.2;
            this.chargeX = motionX;
            this.chargeZ = motionZ;
            setRoaring(false);
            setCharging(true);
        }
        if (isCharging() && this.isAlive()) {
            makeChargeParticles(this.level);
            this.setDeltaMovement(this.chargeX, this.getDeltaMovement().y, this.chargeZ);

            for (Entity entity : this.level.getEntities(this, this.getBoundingBox().inflate(15.0))) {
                if (entity.isAlive()) {
                    double x = this.getX() - entity.getX();
                    double y = this.getY() - entity.getY();
                    double z = this.getZ() - entity.getZ();
                    double d = Math.sqrt(x * x + y * y + z * z);
                    if (this.distanceToSqr(entity) < 9.0) {
                        if (entity instanceof LivingEntity)
                            this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F);
                        entity.hurtMarked = true;
                        if (entity instanceof LivingEntity)
                            entity.hurt(DamageSource.mobAttack(this), 10);
                        entity.setSecondsOnFire(8);
                        entity.setDeltaMovement(-x / d * 4.0, -y / d * 0.5 + 1.2, -z / d * 4.0);
                        entity.lerpMotion(-x / d * 4.0, -y / d * 0.5 + 1.2, -z / d * 4.0);
                    }
                }
            }
        }
    }

    private void doPotionRainAttack() {
        if (attackTicks > 35) {
            this.getMainHandItem().shrink(this.getMainHandItem().getCount());
            this.getOffhandItem().shrink(this.getOffhandItem().getCount());
        }

        if (attackTicks >= 20 && attackTicks <= 35 && this.isAlive()) {
            for (int i = 0; i < 5; i++) {
                int randomSelection = this.random.nextInt(0, 4);
                if (randomSelection == 0) {
                    makeChangeMaskParticles(this.level, 0.28f, 0.30f, 0.28f, 5);
                } else if (randomSelection == 1) {
                    makeChangeMaskParticles(this.level, 0.35f, 0.42f, 0.50f, 5);
                } else if (randomSelection == 2) {
                    makeChangeMaskParticles(this.level, 0.26f, 0.03f, 0.03f, 5);
                } else {
                    makeChangeMaskParticles(this.level, 0.30f, 0.57f, 0.19f, 5);
                }
            }

            for (int i = 0; i < 3; ++i) {
                if (!this.level.isClientSide) {
                    ThrownPotion potionEntity = new ThrownPotion(this.level, this);
                    int lingerChance = this.random.nextInt(0, 10);
                    int randomSelection = this.random.nextInt(0, 4);
                    Potion potion = randomSelection == 0 ? Potions.STRONG_SLOWNESS : randomSelection == 1 ? Potions.STRONG_HARMING : randomSelection == 2 ? Potions.LONG_WEAKNESS : randomSelection == 3 ? Potions.LONG_POISON : null;
                    if (potion != null)
                        potionEntity.setItem(PotionUtils.setPotion(new ItemStack(lingerChance == 0 ? Items.LINGERING_POTION : Items.SPLASH_POTION), potion));
                    potionEntity.setXRot(-20.0F);
                    potionEntity.setPos(this.getX(), this.getY() + 1, this.getZ());
                    potionEntity.shoot(-2.0 + this.random.nextDouble() + this.random.nextDouble() + this.random.nextDouble() + this.random.nextDouble(), 5.0 + this.random.nextDouble() + this.random.nextDouble(), -2.0 + this.random.nextDouble() + this.random.nextDouble() + this.random.nextDouble() + this.random.nextDouble(), 0.75F, 20.0F);
                    this.level.addFreshEntity(potionEntity);
                    if (!this.isSilent()) {
                        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
                    }
                }
            }
        }
    }

    public void distractAttackers(LivingEntity entity) {
        List<Mob> list = this.level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(100.0));

        for (Mob attacker : list) {
            if (attacker.getLastHurtByMob() == this) {
                attacker.setLastHurtByMob(entity);
            }

            if (attacker.getTarget() == this) {
                attacker.setTarget(entity);
            }
        }

    }

    public boolean canTargetedSpellHappen() {
        return this.isAlive() && this.getTarget() != null;
    }

    public boolean doesAttackMeetNormalRequirements() {
        return (!(this instanceof MasqueraderClone) && !areIllagersNearby() && !isUsingAttack() && !isDrinkingPotion() && this.getTarget() != null);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.areIllagersNearby() && source != DamageSource.OUT_OF_WORLD)
            return false;
        if (source.getEntity() == this) return false;

        if (getMask() == ILLUSIONER_MASK && source instanceof IndirectEntityDamageSource) {
            if (!(source.getEntity() instanceof Masquerader)) {
                for (int i = 0; i < 64; ++i) {
                    if (this.teleport(this.getTarget() != null ? this.getTarget() : this)) {
                        if (!this.isSilent()) {
                            this.level.playSound(null, this.xo, this.yo, this.zo, SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F);
                            this.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0F, 1.0F);
                        }
                        return true;
                    }
                }
            }
            return false;
        }
        if (!clones.isEmpty()) {
            damageTaken += amount;
            if (damageTaken > 5) killAllClones();
        }
        return super.hurt(source, amount);
    }

    protected boolean teleport(LivingEntity center) {
        Vec3 vec3 = new Vec3(this.getX() - center.getX(), this.getY(0.5D) - center.getEyeY(), this.getZ() - center.getZ());
        vec3 = vec3.normalize();

        double minDistance = 5.0D;

        double offsetX = (this.random.nextDouble() - 0.5D) * 16.0D;
        double offsetY = this.random.nextInt(16) - 8;
        double offsetZ = (this.random.nextDouble() - 0.5D) * 16.0D;

        if (Math.abs(offsetX) < minDistance) {
            offsetX += Math.signum(offsetX) * minDistance;
        }
        if (Math.abs(offsetY) < minDistance) {
            offsetY += Math.signum(offsetY) * minDistance;
        }
        if (Math.abs(offsetZ) < minDistance) {
            offsetZ += Math.signum(offsetZ) * minDistance;
        }

        double d1 = this.getX() + offsetX - vec3.x * 16.0D;
        double d2 = this.getY() + offsetY - vec3.y * 16.0D;
        double d3 = this.getZ() + offsetZ - vec3.z * 16.0D;

        return this.teleport(d1, d2, d3);
    }

    private boolean teleport(double p_32544_, double p_32545_, double p_32546_) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(p_32544_, p_32545_, p_32546_);

        while (blockpos$mutableblockpos.getY() > this.level.getMinBuildHeight() && !this.level.getBlockState(blockpos$mutableblockpos).getMaterial().blocksMotion()) {
            blockpos$mutableblockpos.move(Direction.DOWN);
        }

        BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos);
        boolean flag = blockstate.getMaterial().blocksMotion();
        boolean flag1 = blockstate.getFluidState().is(FluidTags.WATER);
        if (flag && !flag1) {
            net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(this, p_32544_, p_32545_, p_32546_);
            if (event.isCanceled()) return false;
            Vec3 vec3 = this.position();
            boolean flag2 = this.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
            if (flag2) {
                this.level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(this));
            }

            return flag2;
        } else {
            return false;
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float p_32919_) {
        if (getMask() == ILLUSIONER_MASK) {
            if (!(this instanceof MasqueraderClone) && clones.isEmpty()) {
                setShootInterval(40);
                for (int a = -10; a <= 10; a += 10) {
                    Vec3 vec31 = this.getUpVector(1.0F);
                    Quaternion quaternion = new Quaternion(new Vector3f(vec31), a, true);
                    Vec3 vec3 = this.getViewVector(1.0F);
                    Vector3f vector3f = new Vector3f(vec3);
                    vector3f.transform(quaternion);
                    Vec3 lookVec = this.getLookAngle();
                    double fireworkX = this.getX() + lookVec.x;
                    double fireworkY = this.getEyeY() - 0.2;
                    double fireworkZ = this.getZ() + lookVec.z;
                    int explosionsByDifficulty = this.level.getCurrentDifficultyAt(this.blockPosition()).getDifficulty().getId();
                    ItemStack rocketItem = createRocket(explosionsByDifficulty * 2, DyeColor.PINK, DyeColor.PURPLE);
                    FireworkRocketEntity rocketEntity = new FireworkRocketEntity(this.level, rocketItem, this, fireworkX, fireworkY, fireworkZ, true);
                    rocketEntity.shoot(vector3f.x(), vector3f.y(), vector3f.z(), 1.0F, (float) (14 - this.level.getDifficulty().getId() * 4));
                    this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                    this.level.addFreshEntity(rocketEntity);
                }
            } else {
                ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof BowItem)));
                AbstractArrow abstractarrow = ProjectileUtil.getMobArrow(this, itemstack, p_32919_);
                if (this.getMainHandItem().getItem() instanceof BowItem)
                    abstractarrow = ((BowItem) this.getMainHandItem().getItem()).customArrow(abstractarrow);
                double d0 = target.getX() - this.getX();
                double d1 = target.getY(0.3333333333333333D) - abstractarrow.getY();
                double d2 = target.getZ() - this.getZ();
                double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                abstractarrow.setBaseDamage(3);
                abstractarrow.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, (float) (14 - this.level.getDifficulty().getId() * 4));
                this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                this.level.addFreshEntity(abstractarrow);
                setShootInterval(this.random.nextInt(20, 41));
            }
        } else if (getMask() == RAVAGER_MASK) {
            if (this.getMainHandItem().is(Items.CROSSBOW)) {
                for (int i = 0; i < 5; i++) {
                    ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof CrossbowItem)));
                    AbstractArrow abstractarrow = ProjectileUtil.getMobArrow(this, itemstack, p_32919_);
                    double d0 = target.getX() - this.getX();
                    double d1 = target.getY(0.3333333333333333D) - abstractarrow.getY();
                    double d2 = target.getZ() - this.getZ();
                    double d3 = Math.sqrt(d0 * d0 + d2 * d2);
                    abstractarrow.setBaseDamage(6);
                    abstractarrow.setSecondsOnFire(10000);
                    abstractarrow.setPierceLevel((byte) 1);
                    abstractarrow.shoot(d0, d1 + d3 * (double) 0.2F, d2, 1.6F, 10);
                    this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                    this.level.addFreshEntity(abstractarrow);
                    CrossbowItem.setCharged(this.getMainHandItem(), false);
                }
            }
        } else if (getMask() == WITCH_MASK && !isDrinkingPotion()) {
            Vec3 vec3 = target.getDeltaMovement();
            double d0 = target.getX() + vec3.x - this.getX();
            double d1 = target.getEyeY() - (double) 1.1F - this.getY();
            double d2 = target.getZ() + vec3.z - this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            Potion potion = Potions.STRONG_HARMING;
            int randomSelection = this.random.nextInt(0, 4);
            if (randomSelection == 0 && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                potion = Potions.SLOWNESS;
            } else if (randomSelection == 1 && target.getHealth() >= 8.0F && !target.hasEffect(MobEffects.POISON)) {
                potion = Potions.POISON;
            } else if (randomSelection == 2 && !target.hasEffect(MobEffects.WEAKNESS)) {
                potion = Potions.WEAKNESS;
            }

            ThrownPotion thrownpotion = new ThrownPotion(this.level, this);
            ItemStack potionItem = this.random.nextDouble() >= 0.75 ? new ItemStack(Items.LINGERING_POTION) : new ItemStack(Items.SPLASH_POTION);
            thrownpotion.setItem(PotionUtils.setPotion(potionItem, potion));
            thrownpotion.setXRot(thrownpotion.getXRot() + 20.0F);
            thrownpotion.setOwner(this);
            thrownpotion.shoot(d0, d1 + d3 * 0.2D, d2, 0.75F, 8.0F);
            if (!this.isSilent()) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            }

            this.level.addFreshEntity(thrownpotion);
        }
    }

    private void setShootInterval(int interval) {
        bowGoal.setMinAttackInterval(interval);
    }

    public static ItemStack createRocket(int explosions, DyeColor... dyeColor) {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        ItemStack star = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag starExplosionNBT = star.getOrCreateTagElement("Explosion");
        starExplosionNBT.putInt("Type", FireworkRocketItem.Shape.BURST.getId());
        CompoundTag rocketFireworksNBT = rocket.getOrCreateTagElement("Fireworks");
        ListTag rocketExplosionsNBT = new ListTag();
        CompoundTag actualStarExplosionNBT = star.getTagElement("Explosion");
        if (actualStarExplosionNBT != null) {
            List<Integer> colorList = Lists.newArrayList();
            for (DyeColor color : dyeColor) {
                int pinkFireworkColor = color.getFireworkColor();
                colorList.add(pinkFireworkColor);
            }
            actualStarExplosionNBT.putIntArray("Colors", colorList);
            actualStarExplosionNBT.putIntArray("FadeColors", colorList);
            for (int i = 0; i < explosions; i++) {
                rocketExplosionsNBT.add(actualStarExplosionNBT);
            }
        }
        if (!rocketExplosionsNBT.isEmpty()) {
            rocketFireworksNBT.put("Explosions", rocketExplosionsNBT);
        }
        return rocket;
    }

    @Override
    public void die(DamageSource cause) {
        killAllClones();
        this.setAnimationState(0);
        this.setAnimationState(6);
        this.setPose(Pose.STANDING);

        if (!this.level.isClientSide) {
            this.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
        }

        if (this.lastHurtByPlayerTime > 0) {
            this.lastHurtByPlayerTime = 10000;
        }
    }

    @Override
    protected void tickDeath() {
        deathTime++;
        this.clearFire();
        this.removeEffect(MobEffects.INVISIBILITY);

        if (!this.level.isClientSide && !this.shouldShowArms()) {
            this.setShowArms(true);
        }

        if (deathTime == 26) {
            this.playSound(SoundRegister.MASQUERADER_CHANGE_MASK.get(), 5.0F, 1.0F);
        }

        if (deathTime >= 63) {
            this.playSound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 2.0F, 1.0F);
            this.playSound(SoundEvents.EVOKER_CAST_SPELL, 2.0F, 1.0F);

            super.die(DamageSource.GENERIC);
            if (!this.level.isClientSide()) {
                this.level.broadcastEntityEvent(this, (byte) 60);
                this.remove(RemovalReason.KILLED);
            }
        }
    }

    public void killAllClones() {
        for (int i = clones.size() - 1; i >= 0; i--) {
            if (clones.get(i) != null) {
                clones.get(i).kill();
            }
        }
        clones.clear();
    }

    public void setAnimationState(int input) {
        this.entityData.set(ANIMATION_STATE, input);
    }

    @Override
    public AnimationState getAnimationState(String input) {
        if (Objects.equals(input, "roar")) {
            return this.roarAnimationState;
        } else if (Objects.equals(input, "potion")) {
            return this.potionAnimationState;
        } else if (Objects.equals(input, "change")) {
            return this.changeAnimationState;
        } else if (Objects.equals(input, "crossbow")) {
            return this.crossbowAnimationState;
        } else if (Objects.equals(input, "fangs")) {
            return this.fangsAnimationState;
        } else if (Objects.equals(input, "vex")) {
            return this.vexAnimationState;
        } else {
            return Objects.equals(input, "death") ? this.deathAnimationState : new AnimationState();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_21104_) {
        if (ANIMATION_STATE.equals(p_21104_) && this.level.isClientSide) {
            switch (this.entityData.get(ANIMATION_STATE)) {
                case 0 -> this.stopAllAnimationStates();
                case 1 -> {
                    this.stopAllAnimationStates();
                    this.roarAnimationState.start(this.tickCount);
                }
                case 2 -> {
                    this.stopAllAnimationStates();
                    this.potionAnimationState.start(this.tickCount);
                }
                case 3 -> {
                    this.stopAllAnimationStates();
                    this.changeAnimationState.start(this.tickCount);
                }
                case 4 -> {
                    this.stopAllAnimationStates();
                    this.crossbowAnimationState.start(this.tickCount);
                }
                case 5 -> {
                    this.stopAllAnimationStates();
                    this.fangsAnimationState.start(this.tickCount);
                }
                case 6 -> {
                    this.stopAllAnimationStates();
                    this.deathAnimationState.start(this.tickCount);
                }
                case 7 -> {
                    this.stopAllAnimationStates();
                    this.vexAnimationState.start(this.tickCount);
                }
            }
        }

        super.onSyncedDataUpdated(p_21104_);
    }

    public void stopAllAnimationStates() {
        this.roarAnimationState.stop();
        this.potionAnimationState.stop();
        this.changeAnimationState.stop();
        this.crossbowAnimationState.stop();
        this.fangsAnimationState.stop();
        this.deathAnimationState.stop();
        this.vexAnimationState.stop();
    }

    class StareAtTargetGoal extends Goal {
        public StareAtTargetGoal() {
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
        }

        public boolean canUse() {
            return Masquerader.this.areIllagersNearby() && !Masquerader.this.hasActiveRaid();
        }

        public boolean canContinueToUse() {
            return Masquerader.this.areIllagersNearby() && !Masquerader.this.hasActiveRaid();
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();
        }
    }

    class ChangeMaskGoal extends Goal {
        public ChangeMaskGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK, Flag.MOVE));
        }

        public boolean canUse() {
            return Masquerader.this.doesAttackMeetNormalRequirements() && !Masquerader.this.isCrossbowAttack() && (Masquerader.this.getHealth() < Masquerader.this.getMaxHealth() || Masquerader.this.getMask() != NO_MASK) && Masquerader.this.changeMaskCooldown < 1;
        }

        public void start() {
            Masquerader.this.setAttackType(CHANGE_MASK_ATTACK);
            Masquerader.this.setUsingAttack(true);
            Masquerader.this.setShowArms(true);
            Masquerader.this.setAnimationState(3);
            Masquerader.this.setCrossbowAttack(false);
            Masquerader.this.playSound(SoundRegister.MASQUERADER_CHANGE_MASK.get(), 5.0f, 1.0f);
        }

        public boolean canContinueToUse() {
            return Masquerader.this.attackTicks <= 60;
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();
        }

        public void stop() {
            Masquerader.this.attackTicks = 0;
            Masquerader.this.setAttackType(0);
            if (Masquerader.this.isAlive()) Masquerader.this.setShowArms(false);
            if (Masquerader.this.isAlive()) Masquerader.this.setAnimationState(0);
            Masquerader.this.setUsingAttack(false);

            Masquerader.this.changeMaskCooldown = 600;
        }
    }

    class NoMaskMeleeGoal extends MeleeAttackGoal {
        public NoMaskMeleeGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !areIllagersNearby() && Masquerader.this.getMask() == NO_MASK && !Masquerader.this.isUsingAttack();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !areIllagersNearby() && Masquerader.this.getMask() == NO_MASK && !Masquerader.this.isUsingAttack();
        }
    }

    class EvokerMaskAvoidEntityGoal extends AvoidEntityGoal {
        public EvokerMaskAvoidEntityGoal(PathfinderMob p_25027_, Class<Player> p_25028_, float p_25029_, double p_25030_, double p_25031_) {
            super(p_25027_, p_25028_, p_25029_, p_25030_, p_25031_);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !areIllagersNearby() && Masquerader.this.getMask() == EVOKER_MASK && !Masquerader.this.isUsingAttack();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && Masquerader.this.getMask() == EVOKER_MASK && !Masquerader.this.isUsingAttack();
        }
    }

    class EvokerMaskVexGoal extends Goal {
        public EvokerMaskVexGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK, Flag.MOVE));
        }

        public boolean canUse() {
            return Masquerader.this.doesAttackMeetNormalRequirements() && Masquerader.this.getMask() == EVOKER_MASK && Masquerader.this.vexCooldown < 1;
        }

        public void start() {
            Masquerader.this.setUsingAttack(true);
            Masquerader.this.setAttackType(VEX_ATTACK);
            Masquerader.this.setShowArms(true);
            Masquerader.this.setAnimationState(7);
            Masquerader.this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON);
        }

        public boolean canContinueToUse() {
            return Masquerader.this.attackTicks <= 60;
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();
        }

        public void stop() {
            Masquerader.this.attackTicks = 0;
            Masquerader.this.setAttackType(0);
            Masquerader.this.setShowArms(false);
            if (Masquerader.this.isAlive()) Masquerader.this.setAnimationState(0);
            if (Masquerader.this.isAlive()) Masquerader.this.setUsingAttack(false);

            Masquerader.this.vexCooldown = 800;
        }
    }

    class EvokerMaskFangsGoal extends Goal {
        public EvokerMaskFangsGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK, Flag.MOVE));
        }

        public boolean canUse() {
            return Masquerader.this.doesAttackMeetNormalRequirements() && Masquerader.this.getMask() == EVOKER_MASK && Masquerader.this.fangsCooldown < 1;
        }

        public void start() {
            Masquerader.this.setUsingAttack(true);
            Masquerader.this.setShowArms(true);
            Masquerader.this.setAnimationState(5);
            Masquerader.this.setAttackType(FANGS_ATTACK);
            Masquerader.this.playSound(SoundEvents.EVOKER_PREPARE_ATTACK);
        }

        public boolean canContinueToUse() {
            return Masquerader.this.attackTicks <= 80;
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();
        }

        public void stop() {
            Masquerader.this.attackTicks = 0;
            Masquerader.this.setAttackType(0);
            if (Masquerader.this.isAlive()) Masquerader.this.setShowArms(false);
            if (Masquerader.this.isAlive()) Masquerader.this.setAnimationState(0);
            Masquerader.this.closeCircles = false;
            Masquerader.this.lines = false;
            Masquerader.this.farCircles = false;
            Masquerader.this.rapidBelow = false;
            Masquerader.this.setUsingAttack(false);

            Masquerader.this.fangsCooldown = 60;
        }
    }

    class IllusionerMaskCloneGoal extends Goal {
        public IllusionerMaskCloneGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK, Flag.MOVE));
        }

        public boolean canUse() {
            return Masquerader.this.doesAttackMeetNormalRequirements() && Masquerader.this.getMask() == ILLUSIONER_MASK && clones.isEmpty() && Masquerader.this.cloneCooldown < 1;
        }

        public void start() {
            Masquerader.this.setUsingAttack(true);
            Masquerader.this.setAttackType(CLONE_ATTACK);
            Masquerader.this.playSound(SoundRegister.MASQUERADER_PREPARE_CLONE.get(), 0.8F, 1.0F);
        }

        public boolean canContinueToUse() {
            return Masquerader.this.attackTicks <= 20;
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();
        }

        public void stop() {
            Masquerader.this.attackTicks = 0;
            Masquerader.this.setAttackType(0);
            Masquerader.this.setUsingAttack(false);

            Masquerader.this.cloneCooldown = 800;
        }
    }

    class IllusionerMaskMirrorGoal extends Goal {
        public IllusionerMaskMirrorGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK, Flag.MOVE));
        }

        public boolean canUse() {
            return Masquerader.this.doesAttackMeetNormalRequirements() && Masquerader.this.getMask() == ILLUSIONER_MASK && clones.isEmpty() && Masquerader.this.mirrorCooldown < 1;
        }

        public void start() {
            Masquerader.this.setUsingAttack(true);
            Masquerader.this.setAttackType(MIRROR_ATTACK);
            Masquerader.this.playSound(SoundEvents.ILLUSIONER_PREPARE_MIRROR);
        }

        public boolean canContinueToUse() {
            return Masquerader.this.attackTicks <= 20;
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();
        }

        public void stop() {
            Masquerader.this.attackTicks = 0;
            Masquerader.this.setAttackType(0);
            Masquerader.this.setUsingAttack(false);

            Masquerader.this.mirrorCooldown = 800;
        }
    }

    class IllusionerMaskBlindGoal extends Goal {
        public IllusionerMaskBlindGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK, Flag.MOVE));
        }

        public boolean canUse() {
            return Masquerader.this.doesAttackMeetNormalRequirements() && Masquerader.this.getMask() == ILLUSIONER_MASK && clones.isEmpty() && Masquerader.this.blindCooldown < 1;
        }

        public void start() {
            Masquerader.this.setUsingAttack(true);
            Masquerader.this.setAttackType(BLIND_ATTACK);
            Masquerader.this.playSound(SoundEvents.ILLUSIONER_PREPARE_BLINDNESS);
        }

        public boolean canContinueToUse() {
            return Masquerader.this.attackTicks <= 20;
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();
        }

        public void stop() {
            Masquerader.this.attackTicks = 0;
            Masquerader.this.setAttackType(0);
            Masquerader.this.setUsingAttack(false);

            Masquerader.this.blindCooldown = 800;
        }
    }

    class IllusionerMaskBowAttackGoal extends RangedBowAttackGoal {
        public IllusionerMaskBowAttackGoal(Monster p_25792_, double p_25793_, int p_25794_, float p_25795_) {
            super(p_25792_, p_25793_, p_25794_, p_25795_);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !areIllagersNearby() && Masquerader.this.getMask() == ILLUSIONER_MASK && !Masquerader.this.isUsingAttack();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && Masquerader.this.getMask() == ILLUSIONER_MASK && !Masquerader.this.isUsingAttack();
        }
    }

    class RavagerMaskRoarAndChargeGoal extends Goal {
        public RavagerMaskRoarAndChargeGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK, Flag.MOVE));
        }

        public boolean canUse() {
            return Masquerader.this.doesAttackMeetNormalRequirements() && Masquerader.this.getMask() == RAVAGER_MASK && Masquerader.this.roarAndChargeCooldown < 1;
        }

        public void start() {
            Masquerader.this.setUsingAttack(true);
            Masquerader.this.setCrossbowAttack(false);
            Masquerader.this.setShowArms(true);
            Masquerader.this.setAnimationState(1);
            Masquerader.this.setAttackType(ROAR_AND_CHARGE_ATTACK);
        }

        public boolean canContinueToUse() {
            return Masquerader.this.attackTicks <= 60;
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null && !isCharging()) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();
        }

        public void stop() {
            Masquerader.this.attackTicks = 0;
            Masquerader.this.setAttackType(0);
            Masquerader.this.setUsingAttack(false);
            if (Masquerader.this.isAlive()) Masquerader.this.setShowArms(false);
            if (Masquerader.this.isAlive()) Masquerader.this.setAnimationState(0);
            if (Masquerader.this.isAlive()) {
                Masquerader.this.level.explode(Masquerader.this, Masquerader.this.getX(), Masquerader.this.getY(), Masquerader.this.getZ(), 4, Explosion.BlockInteraction.NONE);
                Masquerader.this.makeExplodeParticles(Masquerader.this.level);
                Masquerader.this.makeFinishChangeMaskParticles(Masquerader.this.level);
                for (Entity entity : Masquerader.this.level.getEntities(Masquerader.this, Masquerader.this.getBoundingBox().inflate(15.0))) {
                    if (entity.isAlive()) {
                        if (Masquerader.this.distanceToSqr(entity) < 64.0) {
                            entity.hurtMarked = true;
                            entity.setSecondsOnFire(8);
                        }
                    }
                }
            }

            Masquerader.this.setRoaring(false);
            Masquerader.this.setCharging(false);
            Masquerader.this.roarAndChargeCooldown = 300;
        }
    }

    class RavagerMaskMeleeGoal extends MeleeAttackGoal {
        public RavagerMaskMeleeGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !areIllagersNearby() && Masquerader.this.getMask() == RAVAGER_MASK && !Masquerader.this.isUsingAttack() && !Masquerader.this.getMainHandItem().is(Items.CROSSBOW);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !areIllagersNearby() && Masquerader.this.getMask() == RAVAGER_MASK && !Masquerader.this.isUsingAttack() && !Masquerader.this.getMainHandItem().is(Items.CROSSBOW);
        }
    }

    class RavagerMaskCrossbowGoal extends RangedCrossbowAttackGoal {
        private final Masquerader mob;
        private CrossbowState crossbowState = CrossbowState.UNCHARGED;
        private int seeTime;
        private int attackDelay;
        private int timeUsing = 0;
        private int shotsFired;

        public RavagerMaskCrossbowGoal(Masquerader mob, double speedModifier, float rangeToShoot) {
            super(mob, speedModifier, rangeToShoot);
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !areIllagersNearby() && Masquerader.this.getMask() == RAVAGER_MASK && Masquerader.this.isCrossbowAttack() && !Masquerader.this.isUsingAttack() && Masquerader.this.getMainHandItem().is(Items.CROSSBOW);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !areIllagersNearby() && Masquerader.this.getMask() == RAVAGER_MASK && Masquerader.this.isCrossbowAttack() && !Masquerader.this.isUsingAttack() && Masquerader.this.getMainHandItem().is(Items.CROSSBOW);
        }

        @Override
        public void start() {
            super.start();
            mob.setShootingTime(-5);
            mob.setCrossbowUsingTime(0);
            shotsFired = 0;
            timeUsing = -5;
            attackDelay = 20;
        }

        @Override
        public void stop() {
            this.seeTime = 0;
            if (this.mob.isUsingItem()) {
                this.mob.stopUsingItem();
                this.mob.setChargingCrossbow(false);
                CrossbowItem.setCharged(this.mob.getUseItem(), false);
            }
            this.crossbowState = CrossbowState.UNCHARGED;
            mob.setCrossbowAttack(false);
            if (mob.isAlive()) mob.setAnimationState(0);
            if (mob.isAlive()) mob.setShowArms(false);
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            timeUsing++;
            if (target != null) {
                boolean flag = this.mob.getSensing().hasLineOfSight(target);
                boolean flag1 = this.seeTime > 0;
                if (flag != flag1) {
                    this.seeTime = 0;
                }

                if (flag) {
                    ++this.seeTime;
                } else {
                    --this.seeTime;
                }
                this.mob.getNavigation().stop();

                this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (this.crossbowState == CrossbowState.UNCHARGED) {
                    mob.setAnimationState(4);
                    mob.setShowArms(true);
                    this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                    this.crossbowState = CrossbowState.CHARGING;
                    this.mob.setChargingCrossbow(true);
                }
                if (this.crossbowState == CrossbowState.CHARGING) {
                    if (!this.mob.isUsingItem()) {
                        this.crossbowState = CrossbowState.UNCHARGED;
                    }

                    if (timeUsing == 8) this.mob.playSound(SoundEvents.CROSSBOW_QUICK_CHARGE_3);
                    if (timeUsing >= 14 && shotsFired < 5) {
                        this.mob.releaseUsingItem();
                        this.crossbowState = CrossbowState.CHARGED;
                        this.attackDelay = 20;
                        ItemStack itemstack1 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                        CrossbowItem.setCharged(itemstack1, true);
                        this.mob.playSound(SoundEvents.CROSSBOW_LOADING_END);
                        this.mob.setChargingCrossbow(false);
                    }
                } else if (this.crossbowState == CrossbowState.CHARGED) {
                    --this.attackDelay;
                    if (this.attackDelay == 0) {
                        this.crossbowState = CrossbowState.READY_TO_ATTACK;
                    }
                } else if (this.crossbowState == CrossbowState.READY_TO_ATTACK) {
                    if ((mob.getShootingTime() == -5 || mob.getShootingTime() == 5 || mob.getShootingTime() == 15 || mob.getShootingTime() == 25 || mob.getShootingTime() == 35) && shotsFired < 5) {
                        mob.playSound(SoundEvents.BLAZE_SHOOT);
                        this.mob.performRangedAttack(target, 1.0F);
                        ItemStack itemstack1 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                        CrossbowItem.setCharged(itemstack1, false);
                        shotsFired++;
                    }
                    if ((mob.getShootingTime() == 0 || mob.getShootingTime() == 10 || mob.getShootingTime() == 20 || mob.getShootingTime() == 30) && shotsFired < 5) {
                        ItemStack itemstack1 = this.mob.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this.mob, item -> item instanceof CrossbowItem));
                        mob.playSound(SoundEvents.CROSSBOW_LOADING_END);
                        CrossbowItem.setCharged(itemstack1, true);
                    }

                    if (mob.getShootingTime() == 49 && shotsFired > 4) {
                        this.crossbowState = CrossbowState.UNCHARGED;
                        mob.setShootingTime(-5);
                        mob.setCrossbowUsingTime(0);
                        mob.setCrossbowAttack(false);
                        shotsFired = 0;
                    }

                    mob.setShootingTime(mob.getShootingTime() + 1);
                }

            }
        }

        enum CrossbowState {
            UNCHARGED,
            CHARGING,
            CHARGED,
            READY_TO_ATTACK;
        }
    }

    class WitchMaskThrowPotionGoal extends RangedAttackGoal {
        public WitchMaskThrowPotionGoal(RangedAttackMob p_25768_, double p_25769_, int p_25770_, float p_25771_) {
            super(p_25768_, p_25769_, p_25770_, p_25771_);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !areIllagersNearby() && Masquerader.this.getMask() == WITCH_MASK && !Masquerader.this.isUsingAttack();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !areIllagersNearby() && Masquerader.this.getMask() == WITCH_MASK && !Masquerader.this.isUsingAttack();
        }
    }

    class WitchMaskPotionRainGoal extends Goal {
        public WitchMaskPotionRainGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK, Flag.MOVE));
        }

        public boolean canUse() {
            return Masquerader.this.doesAttackMeetNormalRequirements() && Masquerader.this.getMask() == WITCH_MASK && Masquerader.this.potionRainCooldown < 1;
        }

        public void start() {
            Masquerader.this.setUsingAttack(true);
            Masquerader.this.setAttackType(POTION_RAIN_ATTACK);
            Masquerader.this.setShowArms(true);
            Masquerader.this.setAnimationState(2);
            Masquerader.this.playSound(SoundEvents.BREWING_STAND_BREW);
            Masquerader.this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SPLASH_POTION));
            Masquerader.this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.LINGERING_POTION));
        }

        public boolean canContinueToUse() {
            return Masquerader.this.attackTicks <= 60;
        }

        public void tick() {
            Masquerader.this.getNavigation().stop();
            if (Masquerader.this.getTarget() != null) {
                Masquerader.this.getLookControl().setLookAt(Masquerader.this.getTarget(), 100.0F, 100.0F);
            }

            Masquerader.this.navigation.stop();

            ItemStack mainHandItem = Masquerader.this.getMainHandItem();
            ItemStack offhandItem = Masquerader.this.getOffhandItem();

            if (mainHandItem.is(Items.SPLASH_POTION)) {
                int randomSelection = Masquerader.this.random.nextInt(0, 4);
                Potion potion = randomSelection == 0 ? Potions.LONG_POISON : randomSelection == 1 ? Potions.STRONG_HARMING : randomSelection == 2 ? Potions.STRONG_SLOWNESS : randomSelection == 3 ? Potions.WEAKNESS : null;
                if (potion != null) PotionUtils.setPotion(mainHandItem, potion);
            }

            if (offhandItem.is(Items.LINGERING_POTION)) {
                int randomSelection = Masquerader.this.random.nextInt(0, 4);
                Potion potion = randomSelection == 0 ? Potions.LONG_POISON : randomSelection == 1 ? Potions.STRONG_HARMING : randomSelection == 2 ? Potions.STRONG_SLOWNESS : randomSelection == 3 ? Potions.WEAKNESS : null;
                if (potion != null) PotionUtils.setPotion(offhandItem, potion);
            }
        }

        public void stop() {
            Masquerader.this.attackTicks = 0;
            Masquerader.this.setAttackType(0);
            if (Masquerader.this.isAlive()) Masquerader.this.setShowArms(false);
            if (Masquerader.this.isAlive()) Masquerader.this.setAnimationState(0);
            Masquerader.this.setUsingAttack(false);

            Masquerader.this.potionRainCooldown = 300;
        }
    }

}