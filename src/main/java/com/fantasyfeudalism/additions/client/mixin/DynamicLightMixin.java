package com.fantasyfeudalism.additions.client.mixin;

import com.fantasyfeudalism.additions.client.DynamicLighting;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
abstract class DynamicLightMixin {
    @Inject(method="getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",at=@At("RETURN"),cancellable=true)
    private static void feudalAdditions$dynamicLight(BlockAndTintGetter level,BlockState state,BlockPos pos,CallbackInfoReturnable<Integer> result){
        result.setReturnValue(DynamicLighting.lightAt(pos,result.getReturnValueI()));
    }
}
