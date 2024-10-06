package net.random_something.masquerader_mod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = MasqueraderMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<List<? extends Integer>> masqueraderRaidcount;
    public static final ForgeConfigSpec.ConfigValue<Boolean> bossBar;
    public static final ForgeConfigSpec.ConfigValue<Boolean> masqueraderForcefield;
    public static final ForgeConfigSpec.ConfigValue<Boolean> masqueraderOnlyOneAllowed;
    public static final ForgeConfigSpec.ConfigValue<Boolean> alternateTextures;

    static {
        BUILDER.push("Masquerader Mod Config");
        masqueraderRaidcount = BUILDER.comment("How many Masqueraders each wave.", "Requires game restart.", "Must have no more and no less than 8 integers.", "If you have the Illage and Spillage mod, you can instead use the Boss Randomizer for better compatibility.", "Note that you CANNOT have a native I&S boss in the same wave as a Masquerader or else they will both have their forcefields active permanently.").worldRestart().defineList("Masquerader Raidcount", Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0), s -> s instanceof Integer);
        bossBar = BUILDER.comment("Whether or not the Masquerader has a boss health bar. Bar does not display in raids", "Default: true").define("Masquerader Boss Bar", true);
        masqueraderForcefield = BUILDER.comment("Should the Masquerader only be allowed to fight once all other raiders in the wave are killed?", "Default: true").define("Masquerader Forcefield", true);
        masqueraderOnlyOneAllowed = BUILDER.comment("Controls if Masquerader should despawn if it spawns in the 7th wave before the bonus wave.", "Overrides Masquerader Raidcount.", "Default = true").define("Only One Masquerader", true);
        alternateTextures = BUILDER.comment("Whether or not to use the alternate model and textures for the Masquerader, mask items, and masked Illagers made by maedus.", "Requires game restart.", "Default = false").worldRestart().define("Use Alternate Textures", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}