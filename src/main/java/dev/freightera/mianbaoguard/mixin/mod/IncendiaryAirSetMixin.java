package dev.freightera.mianbaoguard.mixin.mod;

import dev.freightera.mianbaoguard.MianbaoGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Firebomb/gas-bomb post-processing replaces terrain with scorched variants,
 * water or air through direct setBlock calls. The redirect blocks any change
 * to an existing non-Mianbao block, while still letting the procedure remove
 * or update its own burning charge.
 */
@Mixin(targets = {
        "net.mcreator.myfirstmod.procedures.Firebomb_explode_block_workProcedure",
        "net.mcreator.myfirstmod.procedures.GasbombexplodeblockworkProcedure"
}, remap = false)
public abstract class IncendiaryAirSetMixin {
    @Redirect(
            method = "execute(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/level/block/state/BlockState;)V",
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
