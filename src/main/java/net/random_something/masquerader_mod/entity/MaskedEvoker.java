package net.random_something.masquerader_mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.level.Level;

public class MaskedEvoker extends Evoker implements MaskedIllager {
    public MaskedEvoker(EntityType<? extends Evoker> p_34074_, Level p_34075_) {
        super(p_34074_, p_34075_);
    }

    @Override
    public boolean canAttack(LivingEntity entity) {
        return !(entity instanceof MaskedVillager) && super.canAttack(entity);
    }
}