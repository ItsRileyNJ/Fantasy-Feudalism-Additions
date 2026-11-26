package com.fantasyfeudalism.additions.client;
import com.fantasyfeudalism.additions.FeudalAdditions;
import com.fantasyfeudalism.additions.lighting.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
@EventBusSubscriber(modid=FeudalAdditions.ID,value=Dist.CLIENT)
public final class LightFuelTooltip {
    @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event){
        var stack=event.getItemStack();
        if(stack.is(com.fantasyfeudalism.additions.content.Content.GLOW_OIL.get()))event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_additions.glow_oil").withStyle(ChatFormatting.YELLOW));
        if(GlowOil.fueled(stack))event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_additions.glow_fueled").withStyle(ChatFormatting.YELLOW));
        var state=LightItemFuel.state(stack);if(state==null)return;
        if(WeatherLighting.soul(state))event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_additions.fuel.unlimited").withStyle(ChatFormatting.AQUA));
        else if(LightItemFuel.stage(stack)==0)event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_additions.fuel.empty").withStyle(ChatFormatting.RED));
        else event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_additions.fuel.stage",LightItemFuel.stage(stack)).withStyle(ChatFormatting.GRAY));
        if(!LightItemFuel.lit(stack))event.getToolTip().add(Component.translatable("tooltip.fantasy_feudalism_additions.fuel.unlit").withStyle(ChatFormatting.DARK_GRAY));
    }
}
