package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.mount.*;
import com.fantasyfeudalism.items.sleep.*;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class SleepingBagTests {
    private static ServerPlayer player(GameTestHelper h){
        var profile=new GameProfile(UUID.randomUUID(),"sleep-test");
        var player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,ClientInformation.createDefault());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,new net.minecraft.server.network.CommonListenerCookie(profile,0,ClientInformation.createDefault(),false,net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE));
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        player.setGameMode(GameType.CREATIVE);return player;
    }
    private static BlockPos bed(GameTestHelper h,BedBlock block){
        var head=h.absolutePos(new BlockPos(3,3,3));
        for(int x=-1;x<=1;x++)for(int z=-1;z<=2;z++)for(int y=0;y<=2;y++)h.getLevel().setBlock(head.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
        h.getLevel().setBlock(head.below(),Blocks.STONE.defaultBlockState(),3);
        h.getLevel().setBlock(head.south().below(),Blocks.STONE.defaultBlockState(),3);
        h.getLevel().setBlock(head.south(),block.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT),2);
        h.getLevel().setBlock(head,block.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD),3);
        return head;
    }
    @GameTest(template="empty")
    public static void all_colors_craft_with_matching_wool_and_leather(GameTestHelper h){
        h.assertTrue(SleepingBags.ITEMS.size()==16&&SleepingBags.BLOCKS.size()==16,"All sixteen bed colors registered");
        for(var color:DyeColor.values()){
            var wool=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace(color.getName()+"_wool"));
            var grid=new ArrayList<ItemStack>();for(int i=0;i<3;i++)grid.add(new ItemStack(wool));for(int i=0;i<3;i++)grid.add(new ItemStack(Items.LEATHER));
            var input=CraftingInput.of(3,2,grid);var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
            var result=recipe.value().assemble(input,h.getLevel().registryAccess());
            h.assertTrue(result.is(SleepingBags.ITEMS.get(color).get())&&result.getCount()==1&&result.getMaxStackSize()==1,"Matching wool crafts one portable sleeping bag");
            h.assertTrue(SleepingBags.color(result)==color,"Item preserves chosen color");
            grid.set(0,new ItemStack(color==DyeColor.WHITE?Items.RED_WOOL:Items.WHITE_WOOL));
            h.assertFalse(h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,CraftingInput.of(3,2,grid),h.getLevel()).isPresent(),"Mixed wool colors cannot craft a bag");
        }h.succeed();
    }
    @GameTest(template="empty")
    public static void floor_placement_and_single_item_drop(GameTestHelper h){
        var p=player(h);var head=bed(h,SleepingBags.BLOCKS.get(DyeColor.BLUE).get());var foot=head.south();
        h.getLevel().removeBlock(head,false);h.getLevel().removeBlock(foot,false);
        p.setPos(Vec3.atCenterOf(foot));p.setYRot(180);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SleepingBags.ITEMS.get(DyeColor.BLUE).get()));
        var result=p.getMainHandItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(foot.below()),Direction.UP,foot.below(),false)));
        h.assertTrue(result.consumesAction(),"Sleeping bag places from item");
        h.assertTrue(h.getLevel().getBlockState(head).is(SleepingBags.BLOCKS.get(DyeColor.BLUE).get()),"Placement includes matching head half");
        h.assertTrue(h.getLevel().getBlockState(foot).getShape(h.getLevel(),foot).max(Direction.Axis.Y)==.1875,"Sleeping bag sits three pixels above floor");
        h.assertTrue(h.getLevel().getBlockEntity(head)==null,"Floor model needs no bed block-entity renderer");
        var drops=new ArrayList<ItemStack>();for(var pos:List.of(head,foot))drops.addAll(Block.getDrops(h.getLevel().getBlockState(pos),h.getLevel(),pos,null));
        h.assertTrue(drops.size()==1&&drops.getFirst().is(SleepingBags.ITEMS.get(DyeColor.BLUE).get()),"Two halves drop exactly one matching bag");h.succeed();
    }
    @GameTest(template="empty")
    public static void sleeping_bags_preserve_spawn_on_success_and_daytime_failure(GameTestHelper h){
        var level=h.getLevel();long time=level.getDayTime();var p=player(h);var original=h.absolutePos(new BlockPos(1,2,1));
        try{
            var head=bed(h,SleepingBags.BLOCKS.get(DyeColor.RED).get());p.setPos(Vec3.atCenterOf(head));p.setRespawnPosition(Level.OVERWORLD,original,72,true,false);level.setDayTime(18000);level.updateSkyBrightness();
            var result=p.startSleepInBed(head);h.assertTrue(result.right().isPresent()&&p.isSleeping(),"Can sleep in a sleeping bag at night: "+result);
            h.assertTrue(original.equals(p.getRespawnPosition())&&p.getRespawnAngle()==72&&p.isRespawnForced(),"Sleep preserves original spawn and flags");
            h.assertTrue(Math.abs(p.getY()-head.getY()-.1875)<.00001,"Sleeping player rests at bag height");
            p.stopSleepInBed(true,true);p.setPos(Vec3.atCenterOf(head));level.setDayTime(6000);level.updateSkyBrightness();
            h.assertTrue(p.startSleepInBed(head).left().isPresent(),"Daytime use retains vanilla restriction");
            h.assertTrue(original.equals(p.getRespawnPosition()),"Failed daytime use also preserves spawn");
            p.setRespawnPosition(Level.OVERWORLD,null,0,false,false);p.startSleepInBed(head);
            h.assertTrue(p.getRespawnPosition()==null,"Sleeping bag does not create a spawn for players without one");
            head=bed(h,(BedBlock)Blocks.RED_BED);p.setPos(Vec3.atCenterOf(head));p.startSleepInBed(head);
            h.assertTrue(head.equals(p.getRespawnPosition()),"Ordinary beds still update spawn normally");
        }finally{if(p.isSleeping())p.stopSleepInBed(true,true);level.setDayTime(time);level.updateSkyBrightness();}h.succeed();
    }
    @GameTest(template="empty")
    public static void sleeping_bags_need_200_ticks_and_normal_beds_need_100(GameTestHelper h){
        var level=h.getLevel();long time=level.getDayTime();var p=player(h);
        try{
            level.setDayTime(18000);level.updateSkyBrightness();var head=bed(h,SleepingBags.BLOCKS.get(DyeColor.GREEN).get());p.setPos(Vec3.atCenterOf(head));var result=p.startSleepInBed(head);h.assertTrue(result.right().isPresent(),"Sleep starts: "+result);
            for(int i=0;i<199;i++)p.doTick();
            h.assertTrue(p.isSleeping(),"Player remains asleep");h.assertFalse(p.isSleepingLongEnough(),"Bag is not ready after 199 ticks");
            p.doTick();h.assertTrue(p.isSleepingLongEnough(),"Bag is ready after exactly 200 ticks");
            p.stopSleepInBed(true,true);p.setPos(Vec3.atCenterOf(head));p.startSleepInBed(head);p.doTick();h.assertFalse(p.isSleepingLongEnough(),"A new sleep starts its own timer");
            p.stopSleepInBed(true,true);head=bed(h,(BedBlock)Blocks.RED_BED);p.setPos(Vec3.atCenterOf(head));p.startSleepInBed(head);
            for(int i=0;i<99;i++)p.doTick();h.assertFalse(p.isSleepingLongEnough(),"Normal bed not ready before 100 ticks");
            p.doTick();h.assertTrue(p.isSleepingLongEnough(),"Normal bed retains vanilla 100-tick sleep");
        }finally{if(p.isSleeping())p.stopSleepInBed(true,true);level.setDayTime(time);level.updateSkyBrightness();}h.succeed();
    }
    @GameTest(template="empty")
    public static void dedicated_roll_slot_requires_bags_and_blocks_bag_removal(GameTestHelper h){
        var p=player(h);var roll=new ItemStack(SleepingBags.ITEMS.get(DyeColor.PURPLE).get());
        for(var type:List.of(EntityType.DONKEY,EntityType.MULE)){
            var mount=type.create(h.getLevel());mount.setTamed(true);var inventory=new MountInventory(mount);var menu=new MountMenu(1,p.getInventory(),inventory);
            h.assertFalse(menu.getSlot(5).isActive()||menu.getSlot(5).mayPlace(roll),"Roll slot requires saddlebags");
            inventory.setItem(4,new ItemStack(MountContent.SADDLEBAG.get()));
            h.assertTrue(menu.getSlot(5).isActive()&&menu.getSlot(5).mayPlace(roll),"Sleeping bag has a dedicated slot");
            h.assertFalse(menu.getSlot(5).mayPlace(new ItemStack(Items.RED_BED)),"Normal beds do not fit roll slot");
            inventory.setItem(5,roll.copy());inventory.setItem(3,new ItemStack(Items.LANTERN));inventory.setChanged();
            h.assertTrue(MountVisuals.sleepingBag(mount).is(roll.getItem())&&MountVisuals.lantern(mount).is(Items.LANTERN),"Roll and lantern synchronize separately");
            h.assertFalse(menu.getSlot(4).mayPickup(p),"Saddlebags cannot be removed with mounted roll");
            h.assertTrue(menu.quickMoveStack(p,4).isEmpty(),"Shift-click cannot remove saddlebags first");
            for(var click:List.of(ClickType.PICKUP,ClickType.QUICK_MOVE,ClickType.SWAP,ClickType.THROW)){menu.clicked(4,0,click,p);h.assertFalse(inventory.getItem(4).isEmpty(),"Click cannot bypass removal order");}
            inventory.removeItem(5,1);inventory.setChanged();h.assertTrue(menu.getSlot(4).mayPickup(p),"Removing sleeping bag unlocks saddlebags");
            h.assertTrue(MountVisuals.sleepingBag(mount).isEmpty(),"Removal clears optional model");
            inventory.removeItem(4,1);inventory.setChanged();h.assertFalse(menu.getSlot(9).isActive(),"Roll slot never adds extra cargo");
        }
        h.assertFalse(MountInventory.allowed(EntityType.HORSE.create(h.getLevel()),5,roll,true),"Horses do not gain donkey roll slot");h.succeed();
    }
    @GameTest(template="empty")
    public static void mounted_sleeping_bag_survives_save_and_drops_on_death(GameTestHelper h){
        var mount=EntityType.MULE.create(h.getLevel());
        MountEquipment.set(mount,4,new ItemStack(MountContent.SADDLEBAG.get()));
        MountEquipment.set(mount,5,new ItemStack(SleepingBags.ITEMS.get(DyeColor.CYAN).get()));
        var restored=EntityType.MULE.create(h.getLevel());restored.load(mount.saveWithoutId(new net.minecraft.nbt.CompoundTag()));
        MountEquipment.join(new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(restored,h.getLevel()));
        h.assertTrue(new MountInventory(restored).getItem(5).is(SleepingBags.ITEMS.get(DyeColor.CYAN).get()),"Mounted sleeping bag and color survive save/load");
        h.assertTrue(SleepingBags.color(MountVisuals.sleepingBag(restored))==DyeColor.CYAN,"Loaded roll is synchronized for rendering");
        var drops=new ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        MountEquipment.drops(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(restored,h.getLevel().damageSources().generic(),drops,false));
        h.assertTrue(drops.size()==2&&drops.stream().anyMatch(e->e.getItem().is(SleepingBags.ITEMS.get(DyeColor.CYAN).get())),"Mount death drops saddlebags and sleeping bag separately");
        h.assertTrue(MountEquipment.item(restored,5).isEmpty(),"Dropped sleeping bag is cleared from equipment");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=400)
    public static void sleeping_bags_explode_in_nether_and_end(GameTestHelper h){
        var p=player(h);
        for(var dimension:List.of(Level.NETHER,Level.END)){
            var level=h.getLevel().getServer().getLevel(dimension);var head=new BlockPos(0,240,0);var foot=head.south();
            var state=SleepingBags.BLOCKS.get(DyeColor.WHITE).get().defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH);
            level.setBlock(foot,state.setValue(BedBlock.PART,BedPart.FOOT),2);level.setBlock(head,state.setValue(BedBlock.PART,BedPart.HEAD),3);
            h.assertTrue(level.getBlockState(head).is(state.getBlock())&&level.getBlockState(foot).is(state.getBlock()),"Both halves exist before unsafe use");
            state.setValue(BedBlock.PART,BedPart.HEAD).useWithoutItem(level,p,new BlockHitResult(Vec3.atCenterOf(head),Direction.UP,head,false));
            h.assertTrue(level.getBlockState(head).isAir()&&level.getBlockState(foot).isAir(),"Unsafe dimension destroys both halves through vanilla explosion");
        }h.succeed();
    }
}
