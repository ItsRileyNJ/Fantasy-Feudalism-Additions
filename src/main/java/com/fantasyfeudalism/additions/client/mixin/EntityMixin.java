package com.fantasyfeudalism.additions.client.mixin;

import com.fantasyfeudalism.additions.client.ClairvoyanceClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=Entity.class,priority=1100)
abstract class EntityMixin {
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void feudalAdditions$whiteOutline(CallbackInfoReturnable<Integer> result) {
        if (ClairvoyanceClient.highlights((Entity) (Object) this)) result.setReturnValue(0xFFFFFF);
    }
}
