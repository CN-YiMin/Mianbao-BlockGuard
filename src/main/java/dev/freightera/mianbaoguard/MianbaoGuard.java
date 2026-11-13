package dev.freightera.mianbaoguard;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Caller attribution and the actual block-list filter.
 *
 * <p>The stack walk only happens for events that are already destructive
 * (an explosion finishing, a direct destroyBlock call, a fire block placed by
 * a weapon). It is never on a tick path.
 */
public final class MianbaoGuard {
    private static final String TARGET_PACKAGE = "net.mcreator.myfirstmod";
    private static final String SELF_PACKAGE = "dev.freightera.mianbaoguard";

    private static final Set<String> FRAMEWORK_PREFIXES = Set.of(
            "net.minecraft.",
            "net.neoforged.",
            "net.minecraftforge.",
            "com.mojang.",
            "org.spongepowered.",
            "io.github.llamalad7.",
            "com.llamalad7.",
            "cpw.mods.",
            "java.",
            "jdk.",
            "sun.",
            "com.sun.");

    private static final StackWalker WALKER = StackWalker.getInstance();

    private MianbaoGuard() {
    }

    /**
     * Looks for a Mianbao frame near the top of the stack. Checking a small
     * window instead of only the first non-framework frame keeps attribution
     * correct when another mod wraps the same vanilla method with its own
     * mixin: the wrapper would otherwise hide the Mianbao caller. The window
     * is bounded so the walk stays cheap, and it is only entered from a
     * destructive event.
     */
    public static boolean isMianbaoCaller() {
        return WALKER.walk(frames -> frames
                .limit(24)
                .filter(frame -> !isFramework(frame.getClassName()))
                .anyMatch(frame -> frame.getClassName().startsWith(TARGET_PACKAGE)));
    }

    private static boolean isFramework(String className) {
        if (className.startsWith(SELF_PACKAGE)) {
            return true;
        }
        for (String prefix : FRAMEWORK_PREFIXES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called once at the end of {@code Explosion.explode()}. At that point the
     * explosion has computed its block list and applied entity damage, but has
     * not yet applied the block list (that happens in finalizeExplosion).
     */
    public static void filterExplosionBlocks(Level level, List<BlockPos> blocks) {
        if (blocks.isEmpty()) {
            return;
        }
        if (GuardConfig.blockWhitelistEmpty()) {
            blocks.clear();
            return;
        }
        blocks.removeIf(pos -> !GuardConfig.allows(level.getBlockState(pos)));
    }

    /**
     * Used by the two incendiary post-processing passes that replace terrain
     * with scorched variants, water or air. Any change to an existing block
     * that is not one of the mod's own blocks is a terrain change and is
     * blocked. Removing/updating the mod's own burning charge is still
     * allowed, otherwise its block entity would tick forever.
     */
    public static boolean cancelIncendiaryChange(LevelAccessor level, BlockPos pos, BlockState newState) {
        if (!GuardConfig.protectScorchedTerrain()) {
            return false;
        }
        BlockState oldState = level.getBlockState(pos);
        if (oldState.isAir() || GuardConfig.allows(oldState)) {
            return false;
        }
        return !isTargetModBlock(oldState);
    }

    private static boolean isTargetModBlock(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return "mianbaos_modernwarfare".equals(id.getNamespace());
    }
}
