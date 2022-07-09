package com.joeskott.ridingutils.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RidingUtilsCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> reinsNegateFallDamage;
    public static final ForgeConfigSpec.ConfigValue<Integer> reinsDurability;
    public static final ForgeConfigSpec.DoubleValue reinsJumpHeight;
    public static final ForgeConfigSpec.ConfigValue<Integer> ridingWhipDurability;
    public static final ForgeConfigSpec.ConfigValue<Integer> ridingWhipCooldownTicks;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ridingWhipAnimDamage;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ridingWhipBuck;
    public static final ForgeConfigSpec.ConfigValue<Integer> ridingWhipDangerStart;



    static {
        BUILDER.push("Configs for Riding Utils");

        reinsNegateFallDamage = BUILDER.comment("Do reins negate fall damage? Disable at your own health risk! (Defaults to true)")
                .define("Reins No Fall Damage", true);

        reinsDurability = BUILDER.comment("How much durability do reins have? (Defaults to 128)")
                .defineInRange("Reins Durability", 128, 1, 2048);

        reinsJumpHeight = BUILDER.comment("How high do mobs jump when using reins? (Defaults to 0.5)")
                .defineInRange("Reins Jump Height", 0.5d, 0.1d, 2.0d);

        ridingWhipDurability = BUILDER.comment("How much durability does the riding whip have? (Defaults to 256)")
                .defineInRange("Riding Whip Durability", 256, 1, 2048);

        ridingWhipCooldownTicks = BUILDER.comment("How many ticks before the riding whip can be used again? (Defaults to 40 or 2 seconds)")
                .defineInRange("Riding Whip Cooldown", 40, 1, 99999999);

        ridingWhipAnimDamage = BUILDER.comment("Does the riding whip occasionally cause faux damage even when repaired? (Defaults to false)")
                .define("Riding Whip Fake Damage", false);

        ridingWhipBuck = BUILDER.comment("Does the riding whip have a chance to buck off the rider when at low durability? (Defaults to true)")
                .define("Riding Whip Buck Chance", true);

        ridingWhipDangerStart = BUILDER.comment("When does the risk of side effects begin (at what damage value, higher = lower durability)? (Defaults to 128)")
                .defineInRange("Riding Whip Buck Danger", 128, 1, 2048);



        BUILDER.pop();
        SPEC = BUILDER.build();

    }



}
