package com.fantasyfeudalism.additions.client.mixin;

import com.fantasyfeudalism.additions.client.ClairvoyanceClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=Minecraft.class,priority=1100)
abstract class MinecraftMixin {
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void feudalAdditions$outline(Entity entity, CallbackInfoReturnable<Boolean> result) {
        if (ClairvoyanceClient.highlights(entity)) result.setReturnValue(true);
    }
}
