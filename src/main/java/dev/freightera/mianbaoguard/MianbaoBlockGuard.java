package dev.freightera.mianbaoguard;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An addon for Mianbao's NewModernWarfare that removes the mod's ability to
 * destroy blocks.
 *
 * <p>The guard deliberately never subscribes to a tick event and never scans
 * entities or blocks. It only runs where actual destruction happens:
 * <ul>
 *   <li>at the end of {@link net.minecraft.world.level.Explosion#explode()},
 *       where the list of blocks the explosion is about to remove is known;</li>
 *   <li>at the head of {@code Level.destroyBlock}, for the few procedures that
 *       remove a single block directly;</li>
 *   <li>when the mod enqueues one of its nuclear terrain waves;</li>
 *   <li>when the mod places fire or its bulk "scorched terrain" replacement.</li>
 * </ul>
 *
 * <p>Attribution is done by looking at the caller on the stack of those
 * discrete events, not by polling. Explosions and direct block removals are
 * rare compared with a tick, so the cost is proportional to the number of
 * destructive events, not to the number of ticks or loaded blocks.
 */
@Mod(MianbaoBlockGuard.MODID)
public final class MianbaoBlockGuard {
    public static final String MODID = "mianbao_blockguard";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public MianbaoBlockGuard(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, GuardConfig.SPEC, "mianbao_blockguard-common.toml");
        modBus.addListener(GuardConfig::onConfigLoad);
        modBus.addListener(GuardConfig::onConfigReload);
    }
}
