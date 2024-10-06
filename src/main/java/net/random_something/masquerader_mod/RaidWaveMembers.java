package net.random_something.masquerader_mod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.random_something.masquerader_mod.entity.EntityRegister;

import java.util.ArrayList;
import java.util.List;

public class RaidWaveMembers {
    public static final List<Raid.RaiderType> CUSTOM_RAID_MEMBERS = new ArrayList<>();
    public static Raid.RaiderType PHANTOM_TAMER;

    public static void registerWaveMembers() {
        PHANTOM_TAMER = translateToWaves("masquerader", EntityRegister.MASQUERADER.get(), Config.masqueraderRaidcount.get());
    }

    private static Raid.RaiderType translateToWaves(String name, EntityType<? extends Raider> type, List<? extends Integer> list) {
        Raid.RaiderType member = Raid.RaiderType.create(name, type, new int[]{list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), list.get(5), list.get(6), list.get(7)});
        CUSTOM_RAID_MEMBERS.add(member);
        return member;
    }
}