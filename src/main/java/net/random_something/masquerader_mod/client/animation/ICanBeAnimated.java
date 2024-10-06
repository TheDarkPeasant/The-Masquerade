package net.random_something.masquerader_mod.client.animation;

import net.minecraft.world.entity.AnimationState;

public interface ICanBeAnimated {
    AnimationState getAnimationState(String var1);
    default float getAnimationSpeed() {
        return 1.0F;
    }
}
