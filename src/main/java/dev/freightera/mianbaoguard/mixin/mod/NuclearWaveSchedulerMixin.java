package dev.freightera.mianbaoguard.mixin.mod;

import dev.freightera.mianbaoguard.GuardConfig;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops the nuclear shock-wave work at the queue boundary. No wave is ever
 * created, so there is no per-tick work and no explosion to filter later.
 * Visual-only waves are intentionally left alone.
 */
@Mixin(targets = "net.mcreator.myfirstmod.procedures.NuclearWaveScheduler", remap = false)
public abstract class NuclearWaveSchedulerMixin {
    @Inject(method = "enqueue(Lnet/minecraft/server/level/ServerLevel;DDDD)V", at = @At("HEAD"), cancellable = true)
    private static void mianbaoGuard$blockEnqueue(ServerLevel level, double x, double y, double z, double radius,
            CallbackInfo ci) {
        if (GuardConfig.protectNuclearTerrain()) {
            ci.cancel();
        }
    }

    @Inject(method = "enqueueScaled(Lnet/minecraft/server/level/ServerLevel;DDDDD)V", at = @At("HEAD"), cancellable = true)
    private static void mianbaoGuard$blockEnqueueScaled(ServerLevel level, double x, double y, double z, double radius,
            double scale, CallbackInfo ci) {
        if (GuardConfig.protectNuclearTerrain()) {
            ci.cancel();
        }
    }

    @Inject(method = "enqueueTerrainOnly(Lnet/minecraft/server/level/ServerLevel;DDDD)V", at = @At("HEAD"), cancellable = true)
    private static void mianbaoGuard$blockEnqueueTerrainOnly(ServerLevel level, double x, double y, double z,
            double radius, CallbackInfo ci) {
        if (GuardConfig.protectNuclearTerrain()) {
            ci.cancel();
        }
    }
}
