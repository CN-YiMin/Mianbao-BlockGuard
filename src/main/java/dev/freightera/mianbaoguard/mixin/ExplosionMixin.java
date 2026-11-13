package dev.freightera.mianbaoguard.mixin;

import dev.freightera.mianbaoguard.GuardConfig;
import dev.freightera.mianbaoguard.MianbaoGuard;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * One hook covers every explosion in the game that was started by Mianbao's
 * code, including explosions created directly with {@code new Explosion(...)}
 * by the nuclear wave scheduler.
 *
 * <p>At RETURN the block list is already computed. Clearing or trimming it
 * here means finalizeExplosion has nothing to destroy: no block removal, no
 * drops, no fire placement. Entity damage has already been applied, so it is
 * preserved unless keepEntityDamage is false.
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow
    @Final
    private Level level;

    @Inject(method = "explode()V", at = @At("HEAD"), cancellable = true)
    private void mianbaoGuard$maybeCancelEntityDamage(CallbackInfo ci) {
        if (!GuardConfig.entityDamageGuard()) {
            return;
        }
        if (!MianbaoGuard.isMianbaoCaller()) {
            return;
        }
        ci.cancel();
    }

    @Inject(method = "explode()V", at = @At("RETURN"))
    private void mianbaoGuard$filterBlocks(CallbackInfo ci) {
        if (!GuardConfig.explosionGuard()) {
            return;
        }
        if (!MianbaoGuard.isMianbaoCaller()) {
            return;
        }
        MianbaoGuard.filterExplosionBlocks(this.level, ((Explosion) (Object) this).getToBlow());
    }
}
