package dev.freightera.mianbaoguard;

import java.util.List;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config. Every option is read once when the config loads or reloads;
 * the hot paths only touch the cached fields below.
 */
public final class GuardConfig {
    private static final ModConfigSpec.BooleanValue ENABLED;
    private static final ModConfigSpec.BooleanValue KEEP_ENTITY_DAMAGE;
    private static final ModConfigSpec.BooleanValue PROTECT_EXPLOSIONS;
    private static final ModConfigSpec.BooleanValue PROTECT_DIRECT_BREAKS;
    private static final ModConfigSpec.BooleanValue PROTECT_NUCLEAR_TERRAIN;
    private static final ModConfigSpec.BooleanValue PROTECT_SCORCHED_TERRAIN;
    private static final ModConfigSpec.BooleanValue PROTECT_FIRE;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKS;

    private static volatile boolean enabled = true;
    private static volatile boolean keepEntityDamage = true;
    private static volatile boolean protectExplosions = true;
    private static volatile boolean protectDirectBreaks = true;
    private static volatile boolean protectNuclearTerrain = true;
    private static volatile boolean protectScorchedTerrain = true;
    private static volatile boolean protectFire = true;

    // Combined flags for the hot paths: one volatile read instead of two.
    private static volatile boolean explosionGuard = true;
    private static volatile boolean directBreakGuard = true;
    private static volatile boolean fireGuard = true;
    private static volatile boolean entityDamageGuard = false;

    private static volatile BlockFilter filter = BlockFilter.EMPTY;

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                        "Mianbao BlockGuard / 面包军火库方块保护",
                        "Stops mianbaos_modernwarfare from removing or replacing terrain.",
                        "阻止面包军火库的武器/实体破坏或替换地形。")
                .push("general");

        ENABLED = builder.comment(
                        "Master switch. false restores the mod's original behaviour immediately.",
                        "总开关。设为 false 即完全恢复面包军火库原本的破坏行为。")
                .define("enabled", true);

        KEEP_ENTITY_DAMAGE = builder.comment(
                        "true: explosions still hurt entities, only block destruction is removed.",
                        "false: the explosion is cancelled entirely, so it no longer hurts entities either.",
                        "true：爆炸仍会伤害实体，只去掉方块破坏。",
                        "false：整个爆炸被取消，连实体伤害也没有（武器基本失效）。")
                .define("keepEntityDamage", true);

        PROTECT_EXPLOSIONS = builder.comment(
                        "Filter the block list of every explosion started by mianbaos_modernwarfare.",
                        "过滤面包军火库发起的每一次爆炸的方块列表。")
                .define("protectExplosions", true);

        PROTECT_DIRECT_BREAKS = builder.comment(
                        "Cancel the mod's direct Level.destroyBlock calls (crater loops and special weapons).",
                        "取消模组直接调用 Level.destroyBlock 的破坏（弹坑循环与特殊武器）。")
                .define("protectDirectBreaks", true);

        PROTECT_NUCLEAR_TERRAIN = builder.comment(
                        "Do not enqueue the mod's nuclear wave / crater / scorch tasks.",
                        "不再把核弹的冲击波、弹坑、焦土任务加入队列。")
                .define("protectNuclearTerrain", true);

        PROTECT_SCORCHED_TERRAIN = builder.comment(
                        "Cancel the mod's bulk scorched-terrain replacement routines.",
                        "Also stops incendiary post-processing from clearing plants/snow with setBlock(AIR).",
                        "取消模组的大范围焦土替换流程。",
                        "同时阻止燃烧弹后处理用 setBlock(AIR) 清理植物/雪。")
                .define("protectScorchedTerrain", true);

        PROTECT_FIRE = builder.comment(
                        "Cancel fire blocks placed by the mod's weapons (fire spread can still destroy blocks).",
                        "取消武器放置的火焰方块（火焰蔓延仍可能烧毁方块）。")
                .define("protectFire", true);

        builder.pop();

        builder.comment(
                        "Blocks that are ALLOWED to be destroyed. Everything not listed is protected.",
                        "The list is a whitelist: an empty list protects every block.",
                        "Entry forms: 'minecraft:dirt' or a tag '#minecraft:dirt'.",
                        "允许被破坏的方块白名单：不在列表里的方块全部受保护。",
                        "空列表 = 保护所有方块。支持 'minecraft:dirt' 与标签 '#minecraft:dirt'。")
                .push("whitelist");

        BLOCKS = builder.defineListAllowEmpty("blocks", List.of(), () -> "", o -> o instanceof String);

        builder.pop();

        SPEC = builder.build();
    }

    private GuardConfig() {
    }

    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            rebuild();
        }
    }

    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            rebuild();
        }
    }

    private static void rebuild() {
        boolean on = ENABLED.get();
        enabled = on;
        keepEntityDamage = KEEP_ENTITY_DAMAGE.get();
        protectExplosions = on && PROTECT_EXPLOSIONS.get();
        protectDirectBreaks = on && PROTECT_DIRECT_BREAKS.get();
        protectNuclearTerrain = on && PROTECT_NUCLEAR_TERRAIN.get();
        protectScorchedTerrain = on && PROTECT_SCORCHED_TERRAIN.get();
        protectFire = on && PROTECT_FIRE.get();
        explosionGuard = protectExplosions;
        directBreakGuard = protectDirectBreaks;
        fireGuard = protectFire;
        entityDamageGuard = on && !keepEntityDamage;
        filter = BlockFilter.parse(BLOCKS.get());
        MianbaoBlockGuard.LOGGER.info(
                "Mianbao BlockGuard: enabled={}, whitelist entries={}, keepEntityDamage={}",
                enabled,
                filter.size(),
                keepEntityDamage);
    }

    public static boolean enabled() {
        return enabled;
    }

    public static boolean keepEntityDamage() {
        return keepEntityDamage;
    }

    public static boolean protectExplosions() {
        return protectExplosions;
    }

    public static boolean protectDirectBreaks() {
        return protectDirectBreaks;
    }

    public static boolean protectNuclearTerrain() {
        return protectNuclearTerrain;
    }

    public static boolean protectScorchedTerrain() {
        return protectScorchedTerrain;
    }

    public static boolean protectFire() {
        return protectFire;
    }

    public static boolean explosionGuard() {
        return explosionGuard;
    }

    public static boolean directBreakGuard() {
        return directBreakGuard;
    }

    public static boolean fireGuard() {
        return fireGuard;
    }

    public static boolean entityDamageGuard() {
        return entityDamageGuard;
    }

    public static boolean blockWhitelistEmpty() {
        return filter.isEmpty();
    }

    public static boolean allows(net.minecraft.world.level.block.state.BlockState state) {
        return filter.allows(state);
    }
}
