package dev.freightera.mianbaoguard.mixin;

import dev.freightera.mianbaoguard.GuardConfig;
import dev.freightera.mianbaoguard.MianbaoGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Covers the procedures that remove a block directly instead of going through
 * an Explosion (missile crater loops, the flamethrower, poisoned leaves,
 * scorched wood, ...).
 *
 * <p>The 2-argument LevelWriter helper delegates to this 4-argument method, so
 * one injection covers both call shapes. Only the destructive call itself is
 * inspected; ordinary block updates never come through here.
 */
@Mixin(Level.class)
public abstract class LevelDestroyMixin {
    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z", at = @At("HEAD"), cancellable = true)
    private void mianbaoGuard$cancelDirectBreak(BlockPos pos, boolean dropBlock, Entity entity, int recursion,
            CallbackInfoReturnable<Boolean> cir) {
        if (!GuardConfig.directBreakGuard()) {
            return;
        }
        Level self = (Level) (Object) this;
        if (GuardConfig.allows(self.getBlockState(pos))) {
            return;
        }
        if (!MianbaoGuard.isMianbaoCaller()) {
            return;
        }
        cir.setReturnValue(false);
    }
}
