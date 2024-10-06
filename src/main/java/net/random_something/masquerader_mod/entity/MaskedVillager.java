package net.random_something.masquerader_mod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.random_something.masquerader_mod.Config;
import net.random_something.masquerader_mod.item.ItemRegister;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MaskedVillager extends AbstractVillager {
    private static final EntityDataAccessor<Integer> TEXTURE = SynchedEntityData.defineId(MaskedVillager.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> NEARBY_ILLAGERS = SynchedEntityData.defineId(MaskedVillager.class, EntityDataSerializers.BOOLEAN);
    private int textureTimer = 0;

    public int getTexture() {
        return this.entityData.get(TEXTURE);
    }

    public void setTexture(int texture) {
        this.entityData.set(TEXTURE, texture);
    }

    public boolean areIllagersNearby() {
        return this.entityData.get(NEARBY_ILLAGERS);
    }

    public void setIllagersNearby(boolean illagersNearby) {
        this.entityData.set(NEARBY_ILLAGERS, illagersNearby);
    }

    public MaskedVillager(EntityType<? extends AbstractVillager> p_34074_, Level p_34075_) {
        super(p_34074_, p_34075_);
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.8D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 0.5D, 0.5D));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.FOLLOW_RANGE, 12.0D).add(Attributes.MAX_HEALTH, 20.0D);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_33034_) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TEXTURE, 0);
        this.entityData.define(NEARBY_ILLAGERS, false);
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player p_35856_, InteractionHand p_35857_) {
        if (areIllagersNearby()) return InteractionResult.FAIL;
        ItemStack itemstack = p_35856_.getItemInHand(p_35857_);
        if (!itemstack.is(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (p_35857_ == InteractionHand.MAIN_HAND) {
                p_35856_.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.getOffers().isEmpty()) {
                if (!this.level.isClientSide) {
                    this.setTradingPlayer(p_35856_);
                    this.openTradingScreen(p_35856_, this.getDisplayName(), 1);
                }

            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(p_35856_, p_35857_);
        }
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        offers.clear();

        offers.add(new MerchantOffer(
                new ItemStack(ItemRegister.EVOKER_MASK.get(), 1),
                new ItemStack(Items.EMERALD, 16),
                new ItemStack(ItemRegister.BLANK_MASK.get(), 1),
                1000000000,
                2,
                0.05f
        ));

        offers.add(new MerchantOffer(
                new ItemStack(ItemRegister.ILLUSIONER_MASK.get(), 1),
                new ItemStack(Items.EMERALD, 16),
                new ItemStack(ItemRegister.BLANK_MASK.get(), 1),
                1000000000,
                2,
                0.05f
        ));

        offers.add(new MerchantOffer(
                new ItemStack(ItemRegister.RAVAGER_MASK.get(), 1),
                new ItemStack(Items.EMERALD, 16),
                new ItemStack(ItemRegister.BLANK_MASK.get(), 1),
                1000000000,
                2,
                0.05f
        ));

        offers.add(new MerchantOffer(
                new ItemStack(ItemRegister.WITCH_MASK.get(), 1),
                new ItemStack(Items.EMERALD, 16),
                new ItemStack(ItemRegister.BLANK_MASK.get(), 1),
                1000000000,
                2,
                0.05f
        ));
    }

    public boolean removeWhenFarAway(double p_35886_) {
        return false;
    }

    protected void rewardTradeXp(MerchantOffer p_35859_) {
        if (p_35859_.shouldRewardExp()) {
            int i = 3 + this.random.nextInt(4);
            this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));
        }

    }

    @Override
    public void aiStep() {
        if (areIllagersNearby()) {
            if (Config.alternateTextures.get()) {
                if (!this.level.isClientSide) {
                    if (textureTimer == 0) {
                        setTexture(0);
                    }
                    if (textureTimer == 10) {
                        setTexture(1);
                    }
                    if (textureTimer == 20) {
                        setTexture(2);
                    }
                    if (textureTimer == 30) {
                        setTexture(3);
                    }
                    if (textureTimer == 40) {
                        setTexture(4);
                    }
                }

                if (textureTimer == 20 || textureTimer == 40) {
                    double speedThreshold = 0.1;
                    double currentSpeed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
                    if (level.isClientSide && currentSpeed > speedThreshold) this.sweatParticles();
                }

                textureTimer++;

                if (textureTimer > 50) {
                    textureTimer = 0;
                }

            } else {
                if (!level.isClientSide) {
                    if (textureTimer == 0) {
                        setTexture(0);
                    }
                    if (textureTimer == 5) {
                        setTexture(1);
                    }
                    if (textureTimer == 10) {
                        setTexture(2);
                    }
                    if (textureTimer == 15) {
                        setTexture(3);
                    }
                }

                textureTimer++;

                if (textureTimer > 20) {
                    textureTimer = 0;
                    double speedThreshold = 0.1;
                    double currentSpeed = Math.sqrt(getDeltaMovement().x * getDeltaMovement().x + getDeltaMovement().z * getDeltaMovement().z);
                    if (level.isClientSide && currentSpeed > speedThreshold) this.sweatParticles();
                }
            }
        } else {
            if (!this.level.isClientSide) setTexture(5);
            textureTimer = 0;
        }

        super.aiStep();
    }

    protected void sweatParticles() {
        for (int i = 0; i < 5; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(ParticleTypes.SPLASH, this.getRandomX(1.0D), this.getRandomY() + 1.0D, this.getRandomZ(1.0D), d0, d1, d2);
        }

    }

    @Override
    public void tick() {
        List<Raider> list = this.level.getEntitiesOfClass(Raider.class, this.getBoundingBox().inflate(100.0D), (predicate) -> predicate instanceof MaskedIllager || predicate instanceof Masquerader);
        if (!this.level.isClientSide) {
            this.setIllagersNearby(!list.isEmpty());
        }

        if (this.areIllagersNearby()) {
            List<Pillager> pillagers = this.level.getEntitiesOfClass(Pillager.class, this.getBoundingBox().inflate(100.0D));
            for (Pillager attacker : pillagers) {
                if (attacker.getLastHurtByMob() == this) {
                    attacker.setLastHurtByMob(null);
                }

                if (attacker.getTarget() == this) {
                    attacker.setTarget(null);
                }
            }
        }

        super.tick();
    }
}