package com.fantasyfeudalism.items.sleep.mixin;
import com.fantasyfeudalism.items.sleep.SleepingBagBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
abstract class SleepPositionMixin {
    @Inject(method="setPosToBed",at=@At("HEAD"),cancellable=true)
    private void ff$floorLevel(BlockPos pos,CallbackInfo ci){
        var entity=(LivingEntity)(Object)this;
        if(entity.level().getBlockState(pos).getBlock() instanceof SleepingBagBlock){entity.setPos(pos.getX()+.5,pos.getY()+.1875,pos.getZ()+.5);ci.cancel();}
    }
}
