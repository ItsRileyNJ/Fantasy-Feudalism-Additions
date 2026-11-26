package com.fantasyfeudalism.additions.lighting.mixin;
import com.fantasyfeudalism.additions.lighting.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockItem.class)
abstract class LightPlacementMixin {
    @Inject(method="place",at=@At("HEAD"))
    private void feudalAdditions$capture(BlockPlaceContext context,CallbackInfoReturnable<InteractionResult> result,@com.llamalad7.mixinextras.sugar.Share("ff_light_data") com.llamalad7.mixinextras.sugar.ref.LocalRef<net.minecraft.world.item.component.CustomData> data){
        data.set(context.getItemInHand().getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY));
    }
    @Inject(method="place",at=@At("RETURN"))
    private void feudalAdditions$keepFuel(BlockPlaceContext context,CallbackInfoReturnable<InteractionResult> result,@com.llamalad7.mixinextras.sugar.Share("ff_light_data") com.llamalad7.mixinextras.sugar.ref.LocalRef<net.minecraft.world.item.component.CustomData> captured){
        if(!result.getReturnValue().consumesAction()||!(context.getLevel() instanceof net.minecraft.server.level.ServerLevel level)||level.captureBlockSnapshots)return;
        LightPlacement.restore(level,context.getClickedPos(),captured.get());
    }
}
