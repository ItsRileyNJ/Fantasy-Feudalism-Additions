package com.fantasyfeudalism.items.lighting.mixin;

import com.fantasyfeudalism.items.lighting.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
abstract class LightItemPlacementMixin {
    @WrapMethod(method="useOn")
    private InteractionResult feudalItems$committedFuel(UseOnContext context,Operation<InteractionResult> original){
        var stack=(ItemStack)(Object)this;
        if(!(context.getLevel() instanceof ServerLevel level))return original.call(context);
        if(stack.is(Items.DEBUG_STICK)){
            var pos=context.getClickedPos();var before=level.getBlockState(pos);
            var result=original.call(context);
            if(result.consumesAction()&&!level.captureBlockSnapshots&&context.getPlayer()!=null&&context.getPlayer().canUseGameMasterBlocks())LightFuel.debugEdit(level,pos,before);
            return result;
        }
        if(LightItemFuel.state(stack)==null)return original.call(context);
        // Capture before the last item is consumed, and before the clicked block is replaced.
        var pos=new BlockPlaceContext(context).getClickedPos().immutable();
        var before=level.getBlockState(pos);
        var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY);
        var result=original.call(context);
        if(result.consumesAction()&&!level.captureBlockSnapshots&&level.getBlockState(pos)!=before)LightPlacement.restore(level,pos,data);
        return result;
    }
}
