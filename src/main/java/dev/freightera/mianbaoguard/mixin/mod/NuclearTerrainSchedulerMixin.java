package dev.freightera.mianbaoguard.mixin.mod;

import dev.freightera.mianbaoguard.GuardConfig;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The mod's own budgeted crater/scorch scheduler. Cancelling the enqueue means
 * its per-tick task list stays empty, so its tick handler has nothing to do.
 */
@Mixin(targets = "net.mcreator.myfirstmod.procedures.NuclearTerrainScheduler", remap = false)
public abstract class NuclearTerrainSchedulerMixin {
    @Inject(method = "enqueueCrater(Lnet/minecraft/server/level/ServerLevel;DDDD)V", at = @At("HEAD"), cancellable = true)
    private static void mianbaoGuard$blockCrater(ServerLevel level, double x, double y, double z, double scale,
            CallbackInfo ci) {
        if (GuardConfig.protectNuclearTerrain()) {
            ci.cancel();
        }
    }

    @Inject(method = "enqueueScorch(Lnet/minecraft/server/level/ServerLevel;DDDI)V", at = @At("HEAD"), cancellable = true)
    private static void mianbaoGuard$blockScorch(ServerLevel level, double x, double y, double z, int radius,
            CallbackInfo ci) {
        if (GuardConfig.protectNuclearTerrain()) {
            ci.cancel();
        }
    }
}
