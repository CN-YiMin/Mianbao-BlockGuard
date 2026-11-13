package dev.freightera.mianbaoguard.mixin;

import dev.freightera.mianbaoguard.GuardConfig;
import dev.freightera.mianbaoguard.MianbaoGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The only block-state hook in the mod, and it is gated on the cheapest
 * possible test: the new state must be a fire block. Ordinary stone/dirt/air
 * updates fail that test with one instanceof and pay nothing else.
 *
 * <p>This closes the loop for incendiary weapons that set fire directly after
 * an explosion (fire then spreads and destroys flammable blocks).
 */
@Mixin(Level.class)
public abstract class LevelFireMixin {
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("HEAD"), cancellable = true)
    private void mianbaoGuard$cancelWeaponFire(BlockPos pos, BlockState state, int flags,
            CallbackInfoReturnable<Boolean> cir) {
        if (!GuardConfig.fireGuard()) {
            return;
        }
        if (!(state.getBlock() instanceof BaseFireBlock)) {
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
