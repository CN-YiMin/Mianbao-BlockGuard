package dev.freightera.mianbaoguard.mixin.mod;

import dev.freightera.mianbaoguard.GuardConfig;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The mod's generic "large explosion" pass that replaces a 13x11x13 sphere of
 * terrain with scorched variants. It is post-processing, not entity logic, so
 * cancelling it is safe.
 */
@Mixin(targets = "net.mcreator.myfirstmod.procedures.Explosionblastwork2Procedure", remap = false)
public abstract class Explosionblastwork2ProcedureMixin {
    @Inject(method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDD)V", at = @At("HEAD"), cancellable = true)
    private static void mianbaoGuard$blockScorchedTerrain(LevelAccessor level, double x, double y, double z,
            CallbackInfo ci) {
        if (GuardConfig.protectScorchedTerrain()) {
            ci.cancel();
        }
    }
}
