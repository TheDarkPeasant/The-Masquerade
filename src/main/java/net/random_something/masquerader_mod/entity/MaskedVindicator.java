package net.random_something.masquerader_mod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class MaskedVindicator extends Vindicator implements MaskedIllager {
    private static final EntityDataAccessor<Integer> MASK = SynchedEntityData.defineId(MaskedVindicator.class, EntityDataSerializers.INT);

    public int getMask() {
        return this.entityData.get(MASK);
    }

    public void setMask(int mask) {
        this.entityData.set(MASK, mask);
    }

    public MaskedVindicator(EntityType<? extends Vindicator> p_34074_, Level p_34075_) {
        super(p_34074_, p_34075_);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_34088_, DifficultyInstance p_34089_, MobSpawnType p_34090_, @Nullable SpawnGroupData p_34091_, @Nullable CompoundTag p_34092_) {
        setMask(this.random.nextInt(0, 3));
        return super.finalizeSpawn(p_34088_, p_34089_, p_34090_, p_34091_, p_34092_);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_21484_) {
        super.addAdditionalSaveData(p_21484_);
        p_21484_.putInt("mask", this.getMask());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_21450_) {
        super.readAdditionalSaveData(p_21450_);
        this.setMask(p_21450_.getInt("mask"));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MASK, 0);
    }

    @Override
    public boolean canAttack(LivingEntity entity) {
        return !(entity instanceof MaskedVillager) && super.canAttack(entity);
    }
}