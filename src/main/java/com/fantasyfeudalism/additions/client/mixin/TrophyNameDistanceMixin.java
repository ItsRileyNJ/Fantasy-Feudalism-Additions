package com.fantasyfeudalism.additions.client.mixin;

import com.fantasyfeudalism.additions.trophy.TrophyNameRules;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(EntityRenderer.class)
abstract class TrophyNameDistanceMixin {
    @Redirect(method="renderNameTag",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/client/ClientHooks;isNameplateInRenderDistance(Lnet/minecraft/world/entity/Entity;D)Z"))
    private boolean feudalAdditions$distance(Entity entity,double distance){
        double multiplier=TrophyNameRules.distanceMultiplier(entity);
        return net.neoforged.neoforge.client.ClientHooks.isNameplateInRenderDistance(entity,distance/(multiplier*multiplier));
    }
    @Redirect(method="renderNameTag",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;isDiscrete()Z"))
    private boolean feudalAdditions$crouchedVisibility(Entity entity){return TrophyNameRules.discrete(entity,entity.isDiscrete());}
}
