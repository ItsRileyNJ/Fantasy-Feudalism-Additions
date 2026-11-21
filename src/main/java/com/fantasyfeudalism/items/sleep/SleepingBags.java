package com.fantasyfeudalism.items.sleep;

import com.fantasyfeudalism.items.content.Content;
import java.util.EnumMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class SleepingBags {
    public static final int SLEEP_TICKS=200;
    public static final EnumMap<DyeColor,DeferredBlock<SleepingBagBlock>> BLOCKS=new EnumMap<>(DyeColor.class);
    public static final EnumMap<DyeColor,DeferredItem<BedItem>> ITEMS=new EnumMap<>(DyeColor.class);
    static{for(var color:DyeColor.values()){
        String id=color.getName()+"_sleeping_bag";
        var block=Content.BLOCKS.register(id,()->new SleepingBagBlock(color,BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_BED).mapColor(color)));
        BLOCKS.put(color,block);
        ITEMS.put(color,Content.ITEMS.register(id,()->new BedItem(block.get(),new Item.Properties().stacksTo(1)){
            @Override public void appendHoverText(ItemStack stack,TooltipContext context,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){
                lines.add(net.minecraft.network.chat.Component.translatable("tooltip.fantasy_feudalism_items.sleeping_bag"));
            }
        }));
    }}
    public static void init(){}
    public static boolean isSleepingBag(ItemStack item){return item.getItem() instanceof BlockItem block&&block.getBlock() instanceof SleepingBagBlock;}
    public static DyeColor color(ItemStack item){return ((SleepingBagBlock)((BlockItem)item.getItem()).getBlock()).getColor();}
    public static boolean usingBag(Player player){return player.getSleepingPos().map(p->player.level().getBlockState(p).getBlock() instanceof SleepingBagBlock).orElse(false);}
    private SleepingBags(){}
}
