package com.fantasyfeudalism.additions.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Apply item fuel after placement is committed, including NeoForge's deferred onPlace calls. */
public final class LightPlacement {
    public static void restore(ServerLevel level,BlockPos pos,CustomData captured){
        var state=level.getBlockState(pos);if(!WeatherLighting.supported(state.getBlock()))return;
        var data=captured.getUnsafe();
        if(data.contains("ff_fuel_stage"))LightFuel.setRemaining(level,pos,LightItemFuel.boundary(LightFuel.capacity(state),data.getInt("ff_fuel_stage")));
        else if(data.contains("ff_fuel"))LightFuel.setRemaining(level,pos,LightItemFuel.boundary(LightFuel.capacity(state),LightItemFuel.stageFor(data.getLong("ff_fuel"),LightFuel.capacity(state))));
        var restored=state;
        if(state.is(Blocks.LANTERN))restored=restored.setValue(GlowOil.FUEL,data.getBoolean("ff_glow_oil"));
        if(data.getBoolean("ff_off")||LightFuel.remaining(level,pos)==0)restored=restored.setValue(BlockStateProperties.LIT,false);
        if(restored!=state)level.setBlockAndUpdate(pos,restored);
    }
    private LightPlacement(){}
}
