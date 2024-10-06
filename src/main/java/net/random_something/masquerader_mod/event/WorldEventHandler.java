package net.random_something.masquerader_mod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.random_something.masquerader_mod.MasqueraderMod;
import net.random_something.masquerader_mod.item.ItemRegister;
import net.random_something.masquerader_mod.network.IllusionerMaskAbilityPacket;
import net.random_something.masquerader_mod.network.PacketRegister;
import net.random_something.masquerader_mod.network.RavagerMaskAbilityPacket;
import net.random_something.masquerader_mod.network.WitchMaskAbilityPacket;

@Mod.EventBusSubscriber(modid = MasqueraderMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEventHandler {

    @SubscribeEvent
    public static void evokerMaskAbility(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ItemRegister.EVOKER_MASK.get()) && player.getMainHandItem().isEmpty() && !player.getCooldowns().isOnCooldown(ItemRegister.EVOKER_MASK.get())) {
            player.swing(InteractionHand.MAIN_HAND);
            player.playSound(SoundEvents.EVOKER_CAST_SPELL);
            int standingOnY = Mth.floor(player.getY()) - 1;
            double headY = player.getY() + 1.0D;
            float yawRadians = (float) (Math.toRadians(90 + player.getYRot()));
            for (int l = 0; l < 16; l++) {
                double d2 = 1.25D * (double) (l + 1);
                spawnFangs(player.getX() + (double) Mth.cos(yawRadians) * d2, headY, player.getZ() + (double) Mth.sin(yawRadians) * d2, standingOnY, yawRadians, l, player.level(), player);
            }

            player.getCooldowns().addCooldown(ItemRegister.EVOKER_MASK.get(), 40);
        }
    }

    private static void spawnFangs(double x, double y, double z, int lowestYCheck, float rotationYaw, int warmupDelayTicks, Level world, Player player) {
        BlockPos pos = BlockPos.containing(x, y, z);
        boolean shouldSpawn = false;
        double yCorrection = 0.0D;

        do {
            BlockPos belowPos = pos.below();
            BlockState stateDown = world.getBlockState(belowPos);
            if (stateDown.isFaceSturdy(world, belowPos, Direction.UP)) {
                if (!world.isEmptyBlock(pos)) {
                    BlockState state = world.getBlockState(pos);
                    VoxelShape voxelshape = state.getCollisionShape(world, pos);
                    if (!voxelshape.isEmpty()) {
                        yCorrection = voxelshape.max(Direction.Axis.Y);
                    }
                }

                shouldSpawn = true;
                break;
            }

            pos = belowPos;
        } while (pos.getY() >= lowestYCheck);

        if (shouldSpawn) {
            world.addFreshEntity(new EvokerFangs(world, x, (double) pos.getY() + yCorrection, z, rotationYaw, warmupDelayTicks, player));
        }
    }

    @SubscribeEvent
    public static void prepareIllusionerMaskAbility(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (helmet.is(ItemRegister.ILLUSIONER_MASK.get()) && player.getMainHandItem().isEmpty() && !player.getCooldowns().isOnCooldown(ItemRegister.ILLUSIONER_MASK.get())) {
            player.swing(InteractionHand.MAIN_HAND);

            Vec3 lookVec = player.getLookAngle();
            Vec3 startPos = player.position();

            Vec3 horizontalLookVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();
            double horizontalDistance = 20;
            if (horizontalLookVec.lengthSqr() > 0.001) {
                horizontalLookVec = horizontalLookVec.scale(horizontalDistance);
            } else {
                horizontalLookVec = new Vec3(0, 0, 0);
            }

            Vec3 endPos = startPos.add(horizontalLookVec);

            ClipContext context = new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            BlockHitResult result = player.level().clip(context);

            Vec3 teleportPos;
            if (result.getType() == HitResult.Type.MISS) {
                teleportPos = endPos;
            } else {
                teleportPos = result.getLocation().subtract(horizontalLookVec.scale(0.1));
                BlockPos blockPos = BlockPos.containing(teleportPos);

                while (!player.level().getBlockState(blockPos).getCollisionShape(player.level(), blockPos).isEmpty()) {
                    teleportPos = teleportPos.subtract(horizontalLookVec.scale(0.1));
                    blockPos = BlockPos.containing(teleportPos);
                }
            }

            teleportPos = findSafeTeleportPosition(player, teleportPos);

            player.level().playSound(player, teleportPos.x, teleportPos.y, teleportPos.z, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);
            PacketRegister.INSTANCE.sendToServer(new IllusionerMaskAbilityPacket(teleportPos));
        }
    }

    public static void executeIllusionerMaskAbility(Player player, Vec3 teleportPos) {
        if (player.level().isClientSide) return;

        player.level().playSound(player, teleportPos.x, teleportPos.y, teleportPos.z, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.level().broadcastEntityEvent(player, (byte) 46);
        player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
        player.getCooldowns().addCooldown(ItemRegister.ILLUSIONER_MASK.get(), 200);
    }


    private static Vec3 findSafeTeleportPosition(Player player, Vec3 teleportPos) {
        BlockPos blockPos = BlockPos.containing(teleportPos);

        while (!player.level().getBlockState(blockPos).getCollisionShape(player.level(), blockPos).isEmpty()) {
            teleportPos = teleportPos.add(0, 1, 0);
            blockPos = BlockPos.containing(teleportPos);
        }

        return teleportPos;
    }

    @SubscribeEvent
    public static void doClientRavagerMaskAbility(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (helmet.is(ItemRegister.RAVAGER_MASK.get()) && player.getMainHandItem().isEmpty() && !player.getCooldowns().isOnCooldown(ItemRegister.RAVAGER_MASK.get())) {
            player.swing(InteractionHand.MAIN_HAND);
            makeRoarParticles(player);
            player.playSound(SoundEvents.RAVAGER_ROAR);
            PacketRegister.INSTANCE.sendToServer(new RavagerMaskAbilityPacket());
        }
    }

    public static void doServerRavagerMaskAbility(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));

        for (Entity entity : player.level().getEntities(player, player.getBoundingBox().inflate(15.0))) {
            if (entity.isAlive()) {
                double x = player.getX() - entity.getX();
                double y = player.getY() - entity.getY();
                double z = player.getZ() - entity.getZ();
                double d = Math.sqrt(x * x + y * y + z * z);
                if (player.distanceToSqr(entity) < 25.0) {
                    entity.hurtMarked = true;
                    if (entity instanceof LivingEntity) entity.hurt(player.damageSources().mobAttack(player), 5);
                    entity.setSecondsOnFire(4);
                    entity.setDeltaMovement(-x / d * 4.0, -y / d * 0.5, -z / d * 4.0);
                    entity.lerpMotion(-x / d * 4.0, -y / d * 0.5, -z / d * 4.0);
                }
            }
        }

        player.getCooldowns().addCooldown(ItemRegister.RAVAGER_MASK.get(), 300);
    }

    @SubscribeEvent
    public static void doClientWitchMaskAbility(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (helmet.is(ItemRegister.WITCH_MASK.get()) && player.getMainHandItem().isEmpty() && !player.getCooldowns().isOnCooldown(ItemRegister.WITCH_MASK.get())) {
            player.swing(InteractionHand.MAIN_HAND);
            player.playSound(SoundEvents.BREWING_STAND_BREW, 1.0F, 0.8F + player.getRandom().nextFloat() * 0.4F);
            player.playSound(SoundEvents.WITCH_DRINK, 1.0F, 0.8F + player.getRandom().nextFloat() * 0.4F);
            PacketRegister.INSTANCE.sendToServer(new WitchMaskAbilityPacket());
        }
    }

    public static void doServerWitchMaskAbility(Player player) {
        MobEffect effect = MobEffects.MOVEMENT_SPEED;
        if (player.isOnFire()) {
            effect = MobEffects.FIRE_RESISTANCE;
        } else if (player.isEyeInFluid(FluidTags.WATER)) {
            effect = MobEffects.WATER_BREATHING;
        } else if (player.getHealth() <= player.getMaxHealth() / 2) {
            effect = MobEffects.REGENERATION;
        }

        int amplifier = effect == MobEffects.REGENERATION ? 2 : 0;
        int duration = effect == MobEffects.REGENERATION ? 200 : 400;
        player.addEffect(new MobEffectInstance(effect, duration, amplifier));

        player.getCooldowns().addCooldown(ItemRegister.WITCH_MASK.get(), 600);
    }

    public static void makeRoarParticles(Player player) {
        Vec3 vec3 = player.getBoundingBox().getCenter();
        for (int i = 0; i < 40; ++i) {
            double d0 = player.getRandom().nextGaussian() * 0.2D;
            double d1 = player.getRandom().nextGaussian() * 0.2D;
            double d2 = player.getRandom().nextGaussian() * 0.2D;
            player.level().addParticle(ParticleTypes.POOF, false, vec3.x, vec3.y, vec3.z, (float) d0, (float) d1, (float) d2);
        }

        for (int i = 0; i < 40; ++i) {
            double d0 = player.getRandom().nextGaussian() * 0.2D;
            double d1 = player.getRandom().nextGaussian() * 0.2D;
            double d2 = player.getRandom().nextGaussian() * 0.2D;
            player.level().addParticle(ParticleTypes.FLAME, false, vec3.x, vec3.y, vec3.z, (float) d0, (float) d1, (float) d2);
        }
    }
}