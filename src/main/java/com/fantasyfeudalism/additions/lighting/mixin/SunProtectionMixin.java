package com.fantasyfeudalism.additions.lighting.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(net.minecraft.world.entity.Mob.class)
abstract class SunProtectionMixin {
    @Inject(method = "isSunBurnTick", at = @At("RETURN"), cancellable = true)
    private void feudalAdditions$soulProtection(CallbackInfoReturnable<Boolean> result) {
        if (result.getReturnValueZ() && com.fantasyfeudalism.additions.lighting.LightFuel.protectedFromSun((net.minecraft.world.entity.Mob)(Object)this)) result.setReturnValue(false);
    }
}
