package dev.freightera.mianbaoguard.mixin.mod;

import dev.freightera.mianbaoguard.MianbaoGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The flamethrower's block-hit pass replaces dirt with scorched variants and
 * places fire. The redirect blocks replacement of existing non-Mianbao blocks,
 * but lets the procedure finish (so the projectile is still discarded by its
 * own tick).
 */
@Mixin(targets = "net.mcreator.myfirstmod.procedures.Fireball_tanshe_explodeProcedure", remap = false)
public abstract class FireballScorchMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static boolean mianbaoGuard$guardTerrainChange(LevelAccessor level, BlockPos pos, BlockState state,
            int flags) {
        if (MianbaoGuard.cancelIncendiaryChange(level, pos, state)) {
            return false;
        }
        return level.setBlock(pos, state, flags);
    }
}
