package dev.freightera.mianbaoguard.mixin.mod;

import dev.freightera.mianbaoguard.GuardConfig;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stops the managed nuclear sequence from scheduling terrain rings. */
@Mixin(targets = "net.mcreator.myfirstmod.procedures.ManagedNuclearSequenceScheduler", remap = false)
public abstract class ManagedNuclearSequenceSchedulerMixin {
    @Inject(method = "enqueueTerrain(Lnet/minecraft/server/level/ServerLevel;DDD)V", at = @At("HEAD"), cancellable = true)
    private static void mianbaoGuard$blockTerrain(ServerLevel level, double x, double y, double z, CallbackInfo ci) {
        if (GuardConfig.protectNuclearTerrain()) {
            ci.cancel();
        }
    }
}
