package com.fantasyfeudalism.additions.compat.mixin;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="dev.villagerscontract.GuardMenu$2",remap=false)
abstract class GuardDaggerSlotMixin {
    @Inject(method="mayPlace",at=@At("HEAD"),cancellable=true,remap=false)
    private void feudalAdditions$noMobDaggers(ItemStack stack,CallbackInfoReturnable<Boolean> result){if(stack.is(com.fantasyfeudalism.additions.content.Content.BONE_DAGGER.get()))result.setReturnValue(false);}
}
