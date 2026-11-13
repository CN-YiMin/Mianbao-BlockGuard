package dev.freightera.mianbaoguard.mixin.mod;

import dev.freightera.mianbaoguard.GuardConfig;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The smaller scorched-ring variant; same reasoning as Explosionblastwork2. */
@Mixin(targets = "net.mcreator.myfirstmod.procedures.ExplosionscorchedProcedure", remap = false)
public abstract class ExplosionscorchedProcedureMixin {
    @Inject(method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDD)V", at = @At("HEAD"), cancellable = true)
    private static void mianbaoGuard$blockScorchedRing(LevelAccessor level, double x, double y, double z,
            CallbackInfo ci) {
        if (GuardConfig.protectScorchedTerrain()) {
            ci.cancel();
        }
    }
}
