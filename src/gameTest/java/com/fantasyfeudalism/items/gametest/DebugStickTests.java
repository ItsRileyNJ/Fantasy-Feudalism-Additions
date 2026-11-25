package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.content.Content;
import com.fantasyfeudalism.items.lighting.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class DebugStickTests {
    private static ServerPlayer player(GameTestHelper h,int permission,GameType mode){
        var profile=new com.mojang.authlib.GameProfile(UUID.randomUUID(),"debug-test");
        var player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,ClientInformation.createDefault()){
            @Override protected int getPermissionLevel(){return permission;}
        };
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,new net.minecraft.server.network.CommonListenerCookie(profile,0,ClientInformation.createDefault(),false,net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE));
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        player.setGameMode(mode);player.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(1,2,1))));return player;
    }
    private static BlockPos place(GameTestHelper h,BlockState state,int x){
        var pos=h.absolutePos(new BlockPos(x,2,2));
        h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos.above(),Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos,state);return pos;
    }
    private static void select(ServerPlayer player,BlockState state,Property<?> property){
        var stick=new ItemStack(Items.DEBUG_STICK);
        stick.set(DataComponents.DEBUG_STICK_STATE,DebugStickState.EMPTY.withProperty(state.getBlockHolder(),property));
        player.setItemInHand(InteractionHand.MAIN_HAND,stick);
    }
    private static BlockHitResult hit(BlockPos pos){return new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);}
    private static InteractionResult use(ServerPlayer player,BlockPos pos){return player.getMainHandItem().useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit(pos)));}
    private static PlayerInteractEvent.RightClickBlock click(ServerPlayer player,BlockPos pos){
        var event=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,hit(pos));LightInteractions.interact(event);return event;
    }
    @GameTest(template="empty")
    public static void debug_stick_creates_real_burnout_on_standing_and_hanging_lanterns(GameTestHelper h){
        var admin=player(h,2,GameType.CREATIVE);
        for(boolean hanging:List.of(false,true)){
            var state=Blocks.LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING,hanging).setValue(BlockStateProperties.WATERLOGGED,hanging).setValue(GlowOil.FUEL,hanging);
            var pos=place(h,state,hanging?4:2);LightFuel.setRemaining(h.getLevel(),pos,12345);
            admin.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DEBUG_STICK));
            for(int i=0;i<=state.getProperties().size();i++){
                Items.DEBUG_STICK.canAttackBlock(state,h.getLevel(),pos,admin);
                if(admin.getMainHandItem().get(DataComponents.DEBUG_STICK_STATE).properties().get(state.getBlockHolder())==BlockStateProperties.LIT)break;
            }
            h.assertTrue(admin.getMainHandItem().get(DataComponents.DEBUG_STICK_STATE).properties().get(state.getBlockHolder())==BlockStateProperties.LIT,"Native left-click can select lit");
            h.assertTrue(LightFuel.remaining(h.getLevel(),pos)==12345,"Selecting a property leaves fuel intact");
            h.assertTrue(use(admin,pos).consumesAction(),"Authorized native debug-stick edit succeeds");
            var burnt=h.getLevel().getBlockState(pos);
            h.assertFalse(burnt.getValue(BlockStateProperties.LIT),"Debug-stick lit=false gives burnt-out state");
            h.assertTrue(LightFuel.remaining(h.getLevel(),pos)==0&&burnt.getLightEmission(h.getLevel(),pos)==0,"Burnout empties fuel and produces no light");
            h.assertTrue(burnt.getValue(BlockStateProperties.HANGING)==hanging&&burnt.getValue(BlockStateProperties.WATERLOGGED)==hanging&&burnt.getValue(GlowOil.FUEL)==hanging,"Other lantern properties are preserved");
            var saved=LightFuel.get(h.getLevel()).save(new CompoundTag(),h.getLevel().registryAccess());
            h.assertTrue(saved.getList("Lights",Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast).anyMatch(tag->tag.getLong("Pos")==pos.asLong()&&tag.getLong("Fuel")==0),"Burnout is recorded in persistent fuel data");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void debug_stick_keeps_native_permissions_and_supports_crouching(GameTestHelper h){
        var state=Blocks.LANTERN.defaultBlockState();var pos=place(h,state,2);LightFuel.setRemaining(h.getLevel(),pos,54321);
        for(var mode:List.of(GameType.CREATIVE,GameType.SURVIVAL)){
            var denied=player(h,mode==GameType.CREATIVE?0:2,mode);select(denied,state,BlockStateProperties.LIT);
            h.assertTrue(use(denied,pos)==InteractionResult.FAIL,"Non-operators and survival operators retain native debug-stick restrictions");
            h.assertTrue(h.getLevel().getBlockState(pos).getValue(BlockStateProperties.LIT)&&LightFuel.remaining(h.getLevel(),pos)==54321,"Denied edit cannot change state or fuel");
        }
        var admin=player(h,2,GameType.CREATIVE);select(admin,state,BlockStateProperties.LIT);admin.setShiftKeyDown(true);
        h.assertFalse(click(admin,pos).isCanceled(),"Lantern's normal crouch toggle does not intercept the debug stick");
        use(admin,pos);h.assertTrue(LightFuel.remaining(h.getLevel(),pos)==0,"Crouching debug-stick edit also empties fuel");
        use(admin,pos);h.assertTrue(h.getLevel().getBlockState(pos).getValue(BlockStateProperties.LIT)&&LightFuel.remaining(h.getLevel(),pos)==LightFuel.capacity(state),"Debug-stick lit=true restores a working fueled lantern");
        admin.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);LightFuel.setRemaining(h.getLevel(),pos,54321);click(admin,pos);
        h.assertFalse(h.getLevel().getBlockState(pos).getValue(BlockStateProperties.LIT),"Ordinary crouch interaction still switches off");
        h.assertTrue(LightFuel.remaining(h.getLevel(),pos)==54321,"Ordinary switching off does not empty fuel");h.succeed();
    }
    @GameTest(template="empty")
    public static void debug_burnout_survives_breaking_placement_and_oil_refill(GameTestHelper h){
        var admin=player(h,2,GameType.CREATIVE);var state=Blocks.LANTERN.defaultBlockState();var pos=place(h,state,2);
        select(admin,state,BlockStateProperties.LIT);use(admin,pos);
        admin.setGameMode(GameType.SURVIVAL);admin.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));
        h.assertTrue(admin.gameMode.destroyBlock(pos),"Burnt-out lantern can be broken normally");
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1),drop->drop.getItem().is(Items.LANTERN));
        h.assertTrue(drops.size()==1,"Burnt lantern drops one item");var drop=drops.getFirst().getItem().copy();
        h.assertTrue(LightItemFuel.stage(drop)==0&&!LightItemFuel.lit(drop),"Dropped lantern retains empty fuel and off state");
        var destination=pos.east(2);h.getLevel().setBlockAndUpdate(destination.below(),Blocks.STONE.defaultBlockState());admin.setItemInHand(InteractionHand.MAIN_HAND,drop);
        var result=drop.useOn(new UseOnContext(admin,InteractionHand.MAIN_HAND,hit(destination.below())));
        h.assertTrue(result.consumesAction()&&h.getLevel().getBlockState(destination).is(Blocks.LANTERN),"Burnt-out dropped item can be placed");
        h.assertTrue(LightFuel.remaining(h.getLevel(),destination)==0&&!h.getLevel().getBlockState(destination).getValue(BlockStateProperties.LIT),"Placing the item does not refill its fuel");
        admin.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Content.KELP_OIL.get()));click(admin,destination);
        h.assertTrue(LightFuel.remaining(h.getLevel(),destination)==LightFuel.capacity(state)&&h.getLevel().getBlockState(destination).getValue(BlockStateProperties.LIT),"Kelp Oil refills and lights an admin-burnt-out lantern");
        h.assertTrue(admin.getMainHandItem().is(Items.GLASS_BOTTLE),"Normal refueling still returns its bottle");h.succeed();
    }
    @GameTest(template="empty")
    public static void debug_edits_preserve_soul_lights_other_properties_and_modded_lanterns(GameTestHelper h){
        var admin=player(h,2,GameType.CREATIVE);var state=Blocks.LANTERN.defaultBlockState();var pos=place(h,state,2);LightFuel.setRemaining(h.getLevel(),pos,34567);
        select(admin,state,GlowOil.FUEL);use(admin,pos);
        h.assertTrue(h.getLevel().getBlockState(pos).getValue(GlowOil.FUEL)&&LightFuel.remaining(h.getLevel(),pos)==34567,"Editing glow oil does not empty fuel");
        var soul=Blocks.SOUL_LANTERN.defaultBlockState();var soulPos=place(h,soul,4);long fuel=LightFuel.remaining(h.getLevel(),soulPos);
        select(admin,soul,BlockStateProperties.LIT);use(admin,soulPos);
        h.assertFalse(h.getLevel().getBlockState(soulPos).getValue(BlockStateProperties.LIT),"Soul lantern can still be switched off through native debug edits");
        h.assertTrue(LightFuel.remaining(h.getLevel(),soulPos)==fuel,"Soul lantern does not gain fuel burnout");
        var copper=LanternCompatibilityBlocks.copper.defaultBlockState().setValue(BlockStateProperties.LIT,true);var copperPos=place(h,copper,6);
        select(admin,copper,BlockStateProperties.LIT);use(admin,copperPos);
        h.assertFalse(h.getLevel().getBlockState(copperPos).getValue(BlockStateProperties.LIT),"Other mods retain their native lit-state edits");
        var saved=LightFuel.get(h.getLevel()).save(new CompoundTag(),h.getLevel().registryAccess());
        h.assertFalse(saved.getList("Lights",Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast).anyMatch(tag->tag.getLong("Pos")==copperPos.asLong()),"Items does not create fuel records for another mod's lantern");h.succeed();
    }
    @GameTest(template="empty")
    public static void real_item_use_preserves_partial_fuel_and_cancelled_placement(GameTestHelper h){
        var player=player(h,2,GameType.SURVIVAL);
        for(var item:List.of(Items.LANTERN,Items.TORCH)){
            var pos=h.absolutePos(new BlockPos(item==Items.LANTERN?2:4,2,2));h.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());
            var stack=new ItemStack(item);LightItemFuel.setStage(stack,1,false);GlowOil.set(stack,true);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var result=stack.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit(pos.below())));
            h.assertTrue(result.consumesAction(),"Real ItemStack.useOn placement succeeds");
            var state=h.getLevel().getBlockState(pos);
            h.assertTrue(LightFuel.remaining(h.getLevel(),pos)==LightItemFuel.boundary(LightFuel.capacity(state),1),"Deferred onPlace cannot overwrite the partial fuel stage");
            h.assertFalse(state.getValue(BlockStateProperties.LIT),"Placed off item remains off");
            if(item==Items.LANTERN)h.assertTrue(GlowOil.fueled(state),"Placed lantern retains Glow Oil identity");
            h.assertTrue(stack.isEmpty(),"The last placed item is consumed normally");
        }
        var cancelled=h.absolutePos(new BlockPos(6,2,2));h.getLevel().setBlockAndUpdate(cancelled.below(),Blocks.STONE.defaultBlockState());
        var empty=new ItemStack(Items.LANTERN);LightItemFuel.setStage(empty,0,false);player.setItemInHand(InteractionHand.MAIN_HAND,empty);
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent> cancel=event->{if(event.getLevel()==h.getLevel()&&event.getPos().equals(cancelled))event.setCanceled(true);};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cancel);
        try{
            var result=empty.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit(cancelled.below())));
            h.assertTrue(result==InteractionResult.FAIL&&h.getLevel().getBlockState(cancelled).isAir(),"Cancelled placement restores the world");
            h.assertTrue(empty.getCount()==1&&LightItemFuel.stage(empty)==0,"Cancelled placement preserves the burnt-out item");
            var saved=LightFuel.get(h.getLevel()).save(new CompoundTag(),h.getLevel().registryAccess());
            h.assertFalse(saved.getList("Lights",Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast).anyMatch(tag->tag.getLong("Pos")==cancelled.asLong()),"Cancelled placement leaves no stray fuel record");
            h.getLevel().setBlockAndUpdate(cancelled,Blocks.LANTERN.defaultBlockState());LightFuel.setRemaining(h.getLevel(),cancelled,12345);
            player.setGameMode(GameType.CREATIVE);select(player,h.getLevel().getBlockState(cancelled),BlockStateProperties.LIT);
            h.assertTrue(use(player,cancelled)==InteractionResult.FAIL,"NeoForge can also cancel a debug-stick state edit");
            h.assertTrue(h.getLevel().getBlockState(cancelled).getValue(BlockStateProperties.LIT)&&LightFuel.remaining(h.getLevel(),cancelled)==12345,"Cancelled debug-stick edits preserve both block state and fuel");
        }finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(cancel);}h.succeed();
    }

}
