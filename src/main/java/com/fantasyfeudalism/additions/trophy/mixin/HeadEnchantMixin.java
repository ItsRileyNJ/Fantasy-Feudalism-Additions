package com.fantasyfeudalism.additions.trophy.mixin;
import com.fantasyfeudalism.additions.trophy.Trophies;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Item.class)
abstract class HeadEnchantMixin {
    @Inject(method="isEnchantable", at=@At("HEAD"), cancellable=true)
    private void feudalAdditions$heads(ItemStack stack, CallbackInfoReturnable<Boolean> result) {
        if (stack.is(Trophies.HEADS)) result.setReturnValue(!stack.isEnchanted());
    }
    @Inject(method="getEnchantmentValue", at=@At("HEAD"), cancellable=true)
    private void feudalAdditions$value(CallbackInfoReturnable<Integer> result) {
        if (((Item)(Object)this).builtInRegistryHolder().is(Trophies.HEADS)) result.setReturnValue(10);
    }
}
