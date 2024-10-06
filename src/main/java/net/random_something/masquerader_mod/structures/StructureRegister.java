package net.random_something.masquerader_mod.structures;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class StructureRegister {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_DEF_REG =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, "masquerader_mod");

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPE_DEF_REG =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, "masquerader_mod");

    public static final RegistryObject<StructureType<MasqueradeBallroomStructure>> MASQUERADE_BALLROOM =
            STRUCTURE_TYPE_DEF_REG.register("masquerade_ballroom",
                    () -> () -> MasqueradeBallroomStructure.CODEC);

    public static final RegistryObject<StructurePieceType> MBSP =
            STRUCTURE_PIECE_DEF_REG.register("masquerade_ballroom",
                    () -> MasqueradeBallroomStructure.Piece::new);
}
