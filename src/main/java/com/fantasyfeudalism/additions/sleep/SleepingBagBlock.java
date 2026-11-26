package com.fantasyfeudalism.additions.sleep;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

/** Uses vanilla two-part placement, occupancy, safety, wake-up and dimension explosions. */
public final class SleepingBagBlock extends BedBlock {
    public static final MapCodec<BedBlock> CODEC=RecordCodecBuilder.mapCodec(i->i.group(DyeColor.CODEC.fieldOf("color").forGetter(BedBlock::getColor),propertiesCodec()).apply(i,SleepingBagBlock::new));
    private static final VoxelShape SHAPE=Block.box(0,0,0,16,3,16);
    public SleepingBagBlock(DyeColor color,BlockBehaviour.Properties properties){super(color,properties);}
    @Override public MapCodec<BedBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return SHAPE;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return null;}
}
