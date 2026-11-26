package com.fantasyfeudalism.additions.sleep.mixin;
import com.fantasyfeudalism.additions.sleep.SleepingBags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Player.class)
abstract class PlayerSleepMixin {
    @Unique private int ff$bagTicks;
    @Unique private BlockPos ff$bagPos;
    @Inject(method="startSleepInBed",at=@At("HEAD"))
    private void ff$reset(BlockPos pos,CallbackInfoReturnable<?> ci){ff$bagTicks=0;ff$bagPos=null;}
    @Inject(method="tick",at=@At("HEAD"))
    private void ff$count(CallbackInfo ci){
        var player=(Player)(Object)this;
        if(!player.isSleeping()){ff$bagTicks=0;ff$bagPos=null;}
        else if(SleepingBags.usingBag(player)){
            var pos=player.getSleepingPos().orElseThrow();if(!pos.equals(ff$bagPos)){ff$bagTicks=0;ff$bagPos=pos;}
            if(ff$bagTicks<SleepingBags.SLEEP_TICKS)ff$bagTicks++;
        }else if(ff$bagPos!=null)ff$bagTicks=0;
    }
    @Inject(method="isSleepingLongEnough",at=@At("HEAD"),cancellable=true)
    private void ff$ready(CallbackInfoReturnable<Boolean> ci){
        var player=(Player)(Object)this;
        boolean bag=SleepingBags.usingBag(player);
        if(bag||(ff$bagPos!=null&&player.isSleeping()))ci.setReturnValue(player.isSleeping()&&bag&&ff$bagTicks>=SleepingBags.SLEEP_TICKS);
    }
}
