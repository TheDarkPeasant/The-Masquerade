package net.random_something.masquerader_mod.structures;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Map;
import java.util.Optional;

public class MasqueradeBallroomStructure extends Structure {
    public static final Codec<MasqueradeBallroomStructure> CODEC = simpleCodec(MasqueradeBallroomStructure::new);
    private static final ResourceLocation BALLROOM = new ResourceLocation("masquerader_mod", "masquerade_ballroom");
    private static final Map<ResourceLocation, BlockPos> OFFSET;

    public static void start(StructureTemplateManager templateManager, BlockPos pos, Rotation rotation, StructurePieceAccessor pieceList, RandomSource random) {
        int x = pos.getX();
        int z = pos.getZ();
        BlockPos rotationOffSet = (new BlockPos(0, 0, 0)).rotate(rotation);
        BlockPos blockpos = rotationOffSet.offset(x, Math.max(pos.getY(), 70), z);
        pieceList.addPiece(new Piece(templateManager, BALLROOM, blockpos, rotation));
    }

    public MasqueradeBallroomStructure(Structure.StructureSettings p_227593_) {
        super(p_227593_);
    }

    public Optional<GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        Rotation rotation = Rotation.getRandom(context.random());
        BlockPos blockpos = this.getLowestYIn5by5BoxOffset7Blocks(context, rotation);

        int terrainHeight = getTerrainHeightAtPosition(context, context.chunkGenerator(), blockpos);
        blockpos = new BlockPos(blockpos.getX(), terrainHeight, blockpos.getZ());

        if (!isSurfaceExposedToSky(context, context.chunkGenerator(), blockpos)) {
            return Optional.empty();
        }

        if (isSlopeTooSteep(context, context.chunkGenerator(), blockpos)) {
            return Optional.empty();
        }

        if (isNearWater(context, context.chunkGenerator(), blockpos)) {
            blockpos = findDryLandNearby(context, context.chunkGenerator(), blockpos);
        }

        return Optional.of(new Structure.GenerationStub(blockpos, (p_228526_) -> generatePieces(p_228526_, context)));
    }

    private boolean isSurfaceExposedToSky(Structure.GenerationContext context, ChunkGenerator chunkGenerator, BlockPos pos) {
        int surfaceY = chunkGenerator.getBaseHeight(pos.getX(), pos.getZ(), Heightmap.Types.WORLD_SURFACE, context.heightAccessor(), context.randomState());
        return pos.getY() >= surfaceY;
    }

    private int getTerrainHeightAtPosition(Structure.GenerationContext context, ChunkGenerator chunkGenerator, BlockPos pos) {
        return chunkGenerator.getBaseHeight(pos.getX(), pos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
    }

    private boolean isSlopeTooSteep(Structure.GenerationContext context, ChunkGenerator chunkGenerator, BlockPos pos) {
        int maxHeightDifference = 5;
        int baseHeight = getTerrainHeightAtPosition(context, chunkGenerator, pos);

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                int currentHeight = getTerrainHeightAtPosition(context, chunkGenerator, pos.offset(x, 0, z));
                if (Math.abs(currentHeight - baseHeight) > maxHeightDifference) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNearWater(Structure.GenerationContext context, ChunkGenerator chunkGenerator, BlockPos pos) {
        int radius = 5;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (chunkGenerator.getBaseColumn(checkPos.getX(), checkPos.getZ(), context.heightAccessor(), context.randomState()).getBlock(checkPos.getY()).getBlock() == Blocks.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos findDryLandNearby(Structure.GenerationContext context, ChunkGenerator chunkGenerator, BlockPos pos) {
        int searchRadius = 10;
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);
                if (!isNearWater(context, chunkGenerator, checkPos)) {
                    return checkPos;
                }
            }
        }
        return pos;
    }

    private static void generatePieces(StructurePiecesBuilder p_197233_, Structure.GenerationContext p_197234_) {
        BlockPos blockpos = new BlockPos(p_197234_.chunkPos().getMinBlockX(), 27, p_197234_.chunkPos().getMinBlockZ());
        Rotation rotation = Rotation.getRandom(p_197234_.random());
        start(p_197234_.structureTemplateManager(), blockpos, rotation, p_197233_, p_197234_.random());
    }

    public void afterPlace(WorldGenLevel level, StructureManager p_230228_, ChunkGenerator p_230229_, RandomSource p_230230_, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer p_230233_) {

        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int i = level.getMinBuildHeight();
        BoundingBox boundingbox = p_230233_.calculateBoundingBox();
        int j = boundingbox.minY();

        for (int k = boundingBox.minX(); k <= boundingBox.maxX(); ++k) {
            for (int l = boundingBox.minZ(); l <= boundingBox.maxZ(); ++l) {
                blockpos$mutableblockpos.set(k, j, l);
                if (!level.isEmptyBlock(blockpos$mutableblockpos) && boundingbox.isInside(blockpos$mutableblockpos) && p_230233_.isInsidePiece(blockpos$mutableblockpos)) {
                    for (int i1 = j - 1; i1 > i; --i1) {
                        blockpos$mutableblockpos.setY(i1);
                        if (!level.isEmptyBlock(blockpos$mutableblockpos) && !level.getBlockState(blockpos$mutableblockpos).liquid()) {
                            break;
                        }

                        level.setBlock(blockpos$mutableblockpos, Blocks.DIRT.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public StructureType<?> type() {
        return StructureRegister.MASQUERADE_BALLROOM.get();
    }

    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    static {
        OFFSET = ImmutableMap.of(BALLROOM, new BlockPos(0, 1, 0));
    }

    public static class Piece extends TemplateStructurePiece {
        public Piece(StructureTemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos, Rotation rotation) {
            super(StructureRegister.MBSP.get(), 0, templateManagerIn, resourceLocationIn, resourceLocationIn.toString(), makeSettings(rotation), makePosition(resourceLocationIn, pos));
        }

        public Piece(StructureTemplateManager templateManagerIn, CompoundTag tagCompound) {
            super(StructureRegister.MBSP.get(), tagCompound, templateManagerIn, (p_162451_) -> makeSettings(Rotation.valueOf(tagCompound.getString("Rot"))));
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            this(context.structureTemplateManager(), tag);
        }

        private static StructurePlaceSettings makeSettings(Rotation p_163156_) {
            BlockIgnoreProcessor blockignoreprocessor = BlockIgnoreProcessor.STRUCTURE_BLOCK;
            return (new StructurePlaceSettings()).setRotation(p_163156_).setMirror(Mirror.NONE).addProcessor(blockignoreprocessor).addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE));
        }

        private static BlockPos makePosition(ResourceLocation p_162453_, BlockPos p_162454_) {
            return p_162454_.offset(MasqueradeBallroomStructure.OFFSET.get(p_162453_));
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_162444_, CompoundTag tagCompound) {
            super.addAdditionalSaveData(p_162444_, tagCompound);
            tagCompound.putString("Rot", this.placeSettings.getRotation().name());
        }

        protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor worldIn, RandomSource rand, BoundingBox sbb) {
        }
    }
}