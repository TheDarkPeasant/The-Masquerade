package net.random_something.masquerader_mod.structures.placements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Iterator;
import java.util.Optional;

public class BallroomRandomSpread extends RandomSpreadStructurePlacement {
    public static final Codec<BallroomRandomSpread> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter((rec$) -> (rec$).locateOffset()), FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", FrequencyReductionMethod.DEFAULT).forGetter((rec$) -> (rec$).frequencyReductionMethod()), Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter((rec$) -> (rec$).frequency()), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter((rec$) -> (rec$).salt()), ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter((rec$) -> (rec$).exclusionZone()), SuperExclusionZone.CODEC.optionalFieldOf("super_exclusion_zone").forGetter(BallroomRandomSpread::superExclusionZone), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("spacing").forGetter(BallroomRandomSpread::spacing), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("separation").forGetter(BallroomRandomSpread::separation), RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(BallroomRandomSpread::spreadType), Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("min_distance_from_world_origin").forGetter(BallroomRandomSpread::minDistanceFromWorldOrigin)).apply(instance, instance.stable(BallroomRandomSpread::new)));
    private final int spacing;
    private final int separation;
    private final RandomSpreadType spreadType;
    private final Optional<Integer> minDistanceFromWorldOrigin;
    private final Optional<SuperExclusionZone> superExclusionZone;

    public BallroomRandomSpread(Vec3i locationOffset, FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt, Optional<ExclusionZone> exclusionZone, Optional<SuperExclusionZone> superExclusionZone, int spacing, int separation, RandomSpreadType spreadType, Optional<Integer> minDistanceFromWorldOrigin) {
        super(locationOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.spacing = spacing;
        this.separation = separation;
        this.spreadType = spreadType;
        this.minDistanceFromWorldOrigin = minDistanceFromWorldOrigin;
        this.superExclusionZone = superExclusionZone;
        if (spacing <= separation) {
            throw new RuntimeException("    The Masquerade: Spacing cannot be less or equal to separation.\n    Please correct this error as there's no way to spawn this structure properly\n        Spacing: %s\n        Separation: %s.\n".formatted(spacing, separation));
        }
    }

    public int spacing() {
        return this.spacing;
    }

    public int separation() {
        return this.separation;
    }

    public RandomSpreadType spreadType() {
        return this.spreadType;
    }

    public Optional<Integer> minDistanceFromWorldOrigin() {
        return this.minDistanceFromWorldOrigin;
    }

    public Optional<SuperExclusionZone> superExclusionZone() {
        return this.superExclusionZone;
    }

    @Override
    public boolean isStructureChunk(ChunkGenerator generator, RandomState randomState, long l, int i, int j) {
        if (!super.isStructureChunk(generator, randomState, l, i, j)) {
            return false;
        } else {
            return this.superExclusionZone.isEmpty() || !(this.superExclusionZone.get()).isPlacementForbidden(generator, randomState, l, i, j);
        }
    }

    public ChunkPos getPotentialStructureChunk(long seed, int x, int z) {
        int regionX = Math.floorDiv(x, this.spacing);
        int regionZ = Math.floorDiv(z, this.spacing);
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenrandom.setLargeFeatureWithSalt(seed, regionX, regionZ, this.salt());
        int diff = this.spacing - this.separation;
        int offsetX = this.spreadType.evaluate(worldgenrandom, diff);
        int offsetZ = this.spreadType.evaluate(worldgenrandom, diff);
        return new ChunkPos(regionX * this.spacing + offsetX, regionZ * this.spacing + offsetZ);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGenerator generator, RandomState p_227014_, long l, int x, int z) {
        if (this.minDistanceFromWorldOrigin.isPresent()) {
            int xBlockPos = x * 16;
            int zBlockPos = z * 16;
            if (xBlockPos * xBlockPos + zBlockPos * zBlockPos < this.minDistanceFromWorldOrigin.get() * this.minDistanceFromWorldOrigin.get()) {
                return false;
            }
        }

        ChunkPos chunkpos = this.getPotentialStructureChunk(l, x, z);
        return chunkpos.x == x && chunkpos.z == z;
    }

    public StructurePlacementType<?> type() {
        return StructurePlacementTypeRegister.BALLROOM_RANDOM_SPREAD.get();
    }

    public record SuperExclusionZone(HolderSet<StructureSet> otherSet, int chunkCount) {
        public static final Codec<SuperExclusionZone> CODEC = RecordCodecBuilder.create((builder) -> builder.group(RegistryCodecs.homogeneousList(Registry.STRUCTURE_SET_REGISTRY, StructureSet.DIRECT_CODEC).fieldOf("other_set").forGetter(SuperExclusionZone::otherSet), Codec.intRange(1, 16).fieldOf("chunk_count").forGetter(SuperExclusionZone::chunkCount)).apply(builder, SuperExclusionZone::new));

        boolean isPlacementForbidden(ChunkGenerator generator, RandomState randomState, long l, int i, int j) {
            Iterator<Holder<StructureSet>> var4 = this.otherSet.iterator();

            Holder<StructureSet> holder;
            do {
                if (!var4.hasNext()) {
                    return false;
                }

                holder = var4.next();
            } while (!generator.hasStructureChunkInRange(holder, randomState, l, i, j, this.chunkCount));

            return true;
        }

        public HolderSet<StructureSet> otherSet() {
            return this.otherSet;
        }

        public int chunkCount() {
            return this.chunkCount;
        }
    }
}
