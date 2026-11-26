package com.fantasyfeudalism.additions.client.mixin;

import com.fantasyfeudalism.additions.client.PlacedGlowLights;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
abstract class GlowOilBlockMixin {
    @Inject(method="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",at=@At("RETURN"))
    private void feudalAdditions$track(BlockPos pos,BlockState state,int flags,int recursion,CallbackInfoReturnable<Boolean> result){
        if(result.getReturnValueZ()&&(Object)this instanceof ClientLevel level)PlacedGlowLights.track(level,pos,state);
    }
}
