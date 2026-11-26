package com.fantasyfeudalism.additions.client.mixin;

import com.fantasyfeudalism.additions.trophy.TrophyNameRules;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntityRenderer.class)
abstract class TrophyNameMixin {
    @ModifyConstant(method="shouldShowName",constant=@Constant(floatValue=64.0f))
    private float feudalAdditions$visibleName(float distance,LivingEntity entity){return distance*TrophyNameRules.distanceMultiplier(entity);}

    @Redirect(method="shouldShowName",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;isDiscrete()Z"))
    private boolean feudalAdditions$crouchedRange(LivingEntity entity){return TrophyNameRules.discrete(entity,entity.isDiscrete());}
}
