package net.random_something.masquerader_mod.structures.placements;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class StructurePlacementTypeRegister {
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPE;
    public static final RegistryObject<StructurePlacementType<BallroomRandomSpread>> BALLROOM_RANDOM_SPREAD;

    public StructurePlacementTypeRegister() {
    }

    static {
        STRUCTURE_PLACEMENT_TYPE = DeferredRegister.create(Registry.STRUCTURE_PLACEMENT_TYPE_REGISTRY, "masquerader_mod");
        BALLROOM_RANDOM_SPREAD = STRUCTURE_PLACEMENT_TYPE.register("ballroom_random_spread", () -> () -> BallroomRandomSpread.CODEC);
    }
}
