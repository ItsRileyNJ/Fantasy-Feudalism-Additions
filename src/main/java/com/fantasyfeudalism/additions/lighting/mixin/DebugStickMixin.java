package com.fantasyfeudalism.additions.lighting.mixin;

import com.fantasyfeudalism.additions.lighting.LightFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Synchronize real fuel only after vanilla accepts an administrator's debug-stick edit. */
@Mixin(DebugStickItem.class)
abstract class DebugStickMixin {
    @Inject(method="handleInteraction",at=@At("RETURN"))
    private void feudalAdditions$debugFuel(Player player,BlockState before,LevelAccessor world,BlockPos pos,boolean update,ItemStack stick,CallbackInfoReturnable<Boolean> result){
        if(!update||!Boolean.TRUE.equals(result.getReturnValue())||!(world instanceof ServerLevel level)||level.captureBlockSnapshots)return;
        LightFuel.debugEdit(level,pos,before);
    }
}
