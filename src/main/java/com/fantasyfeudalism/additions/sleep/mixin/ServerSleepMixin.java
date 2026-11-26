package com.fantasyfeudalism.additions.sleep.mixin;
import com.fantasyfeudalism.additions.sleep.SleepingBagBlock;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerPlayer.class)
abstract class ServerSleepMixin {
    @Unique private BlockPos ff$bagAttempt;
    @WrapMethod(method="startSleepInBed")
    private Either<Player.BedSleepingProblem,Unit> ff$noSpawn(BlockPos pos,Operation<Either<Player.BedSleepingProblem,Unit>> original){
        var player=(ServerPlayer)(Object)this;var previous=ff$bagAttempt;
        ff$bagAttempt=player.level().getBlockState(pos).getBlock() instanceof SleepingBagBlock?pos:null;
        try{return original.call(pos);}finally{ff$bagAttempt=previous;}
    }
    @Inject(method="setRespawnPosition",at=@At("HEAD"),cancellable=true)
    private void ff$preserveSpawn(ResourceKey<Level> dimension,BlockPos pos,float angle,boolean forced,boolean message,CallbackInfo ci){
        if(ff$bagAttempt!=null&&ff$bagAttempt.equals(pos)&&dimension.equals(((ServerPlayer)(Object)this).level().dimension()))ci.cancel();
    }
}
