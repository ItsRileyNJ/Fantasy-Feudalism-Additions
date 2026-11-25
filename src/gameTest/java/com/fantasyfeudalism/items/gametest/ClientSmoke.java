package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.content.ModAttachments;
import com.fantasyfeudalism.items.lighting.LightItemFuel;
import com.fantasyfeudalism.items.mount.*;
import java.util.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.*;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Development-only checks against the supplied binary mods, never packaged in the release. */
@EventBusSubscriber(modid=FeudalItems.ID,value=Dist.CLIENT)
public final class ClientSmoke {
    private static int phase,ticks,hoofIndex;
    private static volatile UUID horseId,donkeyId,muleId;
    private static final List<String> PASSED=new ArrayList<>();
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);PASSED.add(message);}
    private static Entity entity(Minecraft client,UUID id){for(var entity:client.level.entitiesForRendering())if(entity.getUUID().equals(id))return entity;return null;}
    private static int itemLight(ItemStack stack){return com.fantasyfeudalism.items.client.DynamicLighting.itemLight(stack);}
    private static int entityLight(Entity entity){return com.fantasyfeudalism.items.client.DynamicLighting.entityLight(entity);}
    private static void screenshot(Minecraft client,String name){net.minecraft.client.Screenshot.grab(client.gameDirectory,"mount-ui-"+name+".png",client.getMainRenderTarget(),message->{});}
    private static void report(boolean complete,Throwable error){
        try{var json=new com.google.gson.JsonObject();json.addProperty("complete",complete);json.add("passed",new com.google.gson.Gson().toJsonTree(PASSED));if(error!=null)json.addProperty("error",error.toString());var path=Path.of(System.getProperty("fantasyfeudalism.smokeReport"));Files.createDirectories(path.getParent());Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json));}catch(Exception exception){exception.printStackTrace();}
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("fantasyfeudalism.clientSmoke")||phase<0)return;
        var client=Minecraft.getInstance();if(client.level==null||client.player==null||client.getSingleplayerServer()==null)return;
        try{
            if(++ticks>1200)throw new AssertionError("Client smoke phase timed out: "+phase);
            if(phase==0){
                client.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                var source=new com.fantasyfeudalism.items.client.DynamicLighting.Source(0,0,0,15);
                var lights=List.of(source);
                check(com.fantasyfeudalism.items.client.DynamicLighting.lightAt(lights,0,0,0,0)==240,"Dynamic source emits full brightness at its origin");
                check(com.fantasyfeudalism.items.client.DynamicLighting.lightAt(lights,5,0,0,0)==160,"Dynamic brightness falls off with distance");
                check(com.fantasyfeudalism.items.client.DynamicLighting.lightAt(lights,15,0,0,0)==0,"Dynamic brightness ends at the source radius");
                check(com.fantasyfeudalism.items.client.DynamicLighting.lightAt(lights,5,0,0,0xF000E0)==0xF000E0,"Dynamic illumination preserves brighter block light and sky light");
                check(com.fantasyfeudalism.items.client.DynamicLighting.lightAt(List.of(),0,0,0,0)==0,"Removed sources leave no dynamic illumination");
                var glowLantern = new ItemStack(Items.LANTERN);
                com.fantasyfeudalism.items.lighting.GlowOil.set(glowLantern, true);
                int glowReach = itemLight(glowLantern);
                check(glowReach == 25, "Glow Oil lantern has a 25-block light reach");
                var glowLights = List.of(new com.fantasyfeudalism.items.client.DynamicLighting.Source(0, 0, 0, glowReach));
                check(com.fantasyfeudalism.items.client.DynamicLighting.lightAt(glowLights,0,0,0,0)==240,"Glow Oil clamps at vanilla maximum brightness");
                check(com.fantasyfeudalism.items.client.DynamicLighting.lightAt(glowLights,24,0,0,0)==16,"Glow Oil lights beyond the normal lantern radius");
                check(com.fantasyfeudalism.items.client.DynamicLighting.lightAt(glowLights,25,0,0,0)==0,"Glow Oil light ends at 25 blocks");
                var drop=new net.minecraft.world.entity.item.ItemEntity(client.level,0,0,0,new ItemStack(Items.TORCH));
                check(entityLight(drop)==14,"Dropped torches emit light without optional mods");
                var holder=EntityType.HORSE.create(client.level);
                holder.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SOUL_TORCH));
                check(entityLight(holder)==10,"Offhand soul lights emit their vanilla brightness");
                holder.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.LANTERN));
                check(entityLight(holder)==15,"Multiple held lights use the brightest source");
                var renderPlayer=client.player;
                var savedMain=renderPlayer.getMainHandItem().copy();var savedOff=renderPlayer.getOffhandItem().copy();
                try{
                    renderPlayer.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);
                    var renderer=client.getEntityRenderDispatcher();
                    for(var item:List.of(Items.TORCH,Items.SOUL_TORCH,Items.LANTERN,Items.SOUL_LANTERN)){
                        renderPlayer.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));
                        int packed=renderer.getPackedLightCoords(renderPlayer,1);
                        check((packed&0xFFFF)>=entityLight(renderPlayer)*16,"Player render light includes held "+item);
                        check((packed&0xFFFF)<=240,"Held light stays within the player lightmap bounds");
                    }
                    renderPlayer.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
                    renderPlayer.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.TORCH));
                    check((renderer.getPackedLightCoords(renderPlayer,1)&0xFFFF)>=224,"Offhand torch lights the player and first-person hands");
                }finally{
                    renderPlayer.setItemSlot(EquipmentSlot.MAINHAND,savedMain);renderPlayer.setItemSlot(EquipmentSlot.OFFHAND,savedOff);
                }
                var before=new ItemStack(Items.LANTERN);var after=before.copy();LightItemFuel.set(after,80,true);
                net.neoforged.neoforge.client.ClientHooks.shouldCauseReequipAnimation(before,before,0);
                check(!net.neoforged.neoforge.client.ClientHooks.shouldCauseReequipAnimation(before,after,0),"Fuel countdown does not replay main-hand equip animation");
                check(!net.neoforged.neoforge.client.ClientHooks.shouldCauseReequipAnimation(before,after,-1),"Fuel countdown does not replay offhand equip animation");
                check(net.neoforged.neoforge.client.ClientHooks.shouldCauseReequipAnimation(before,after,1),"Changing hotbar slot still equips the lantern");
                after.set(DataComponents.CUSTOM_NAME,Component.literal("Different lantern"));
                check(net.neoforged.neoforge.client.ClientHooks.shouldCauseReequipAnimation(before,after,1),"Unrelated item component changes retain equip behavior");
                check(itemLight(new ItemStack(Items.TORCH))==14,"Built-in lighting uses vanilla torch brightness");
                check(itemLight(new ItemStack(Items.LANTERN))==15,"Built-in lighting uses vanilla lantern brightness");
                var off=new ItemStack(Items.LANTERN);LightItemFuel.set(off,100,false);check(itemLight(off)==0,"Switched-off lantern produces no dynamic light");
                var empty=new ItemStack(Items.TORCH);LightItemFuel.set(empty,0,false);check(itemLight(empty)==0,"Burned-out torch produces no dynamic light");
                check(itemLight(new ItemStack(Items.SOUL_LANTERN))==10,"Soul lantern retains vanilla brightness");
                client.getSingleplayerServer().execute(()->{
                    var server=client.getSingleplayerServer();var player=server.getPlayerList().getPlayer(client.player.getUUID());var level=player.serverLevel();
                    player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
                    for(int x=-6;x<=6;x++)for(int z=-4;z<=8;z++)level.setBlockAndUpdate(new net.minecraft.core.BlockPos(x,149,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    level.getEntitiesOfClass(AbstractHorse.class,new net.minecraft.world.phys.AABB(-6,145,-4,6,155,8)).forEach(Entity::discard);
                    player.connection.teleport(0.5,150,0.5,45,12);level.setBlockAndUpdate(new net.minecraft.core.BlockPos(2,149,2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(new net.minecraft.core.BlockPos(-2,149,2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    var horse=EntityType.HORSE.create(level);horse.setPos(2.5,150,2.5);horse.setTamed(true);horse.setOwnerUUID(player.getUUID());horse.setNoAi(true);MountEquipment.set(horse,2,new ItemStack(MountContent.GOLD.get()));level.addFreshEntity(horse);horseId=horse.getUUID();
                    var donkey=EntityType.DONKEY.create(level);donkey.setPos(-1.5,150,2.5);donkey.setTamed(true);donkey.getSlot(400).set(new ItemStack(Items.SADDLE));donkey.setNoAi(true);donkey.setYRot(315);donkey.yBodyRot=315;donkey.yHeadRot=315;var bag=new ItemStack(MountContent.SADDLEBAG.get());MountEquipment.set(donkey,4,bag);MountEquipment.set(donkey,3,new ItemStack(Items.LANTERN));level.addFreshEntity(donkey);donkeyId=donkey.getUUID();
                });phase=1;ticks=0;return;
            }
            var horse=horseId==null?null:entity(client,horseId);
            if(horse==null)return;
            if(phase==1){
                var litMount=donkeyId==null?null:entity(client,donkeyId);if(litMount==null)return;var data=litMount.getData(ModAttachments.MOUNT);if(!data.contains("Slot3"))return;
                check(entityLight(litMount)==15,"Mounted lantern contributes light through built-in lighting");
                var donkey=donkeyId==null?null:entity(client,donkeyId);if(donkey==null||!donkey.getData(ModAttachments.MOUNT).contains("Slot4"))return;
                var bag=ItemStack.parse(client.level.registryAccess(),donkey.getData(ModAttachments.MOUNT).getCompound("Slot4")).orElseThrow();check(!bag.has(DataComponents.DYED_COLOR),"Equipped saddlebag uses its fixed leather finish");
                var bagModel=client.getItemRenderer().getModel(bag,client.level,client.player,0);
                var quads=bagModel.getQuads(null,null,net.minecraft.util.RandomSource.create(0));
                check(quads.size()==78,"Supplied saddlebag renders thirteen permanent model pieces without the sleeping bag");
                check(quads.stream().noneMatch(net.minecraft.client.renderer.block.model.BakedQuad::isTinted),"Every saddlebag face uses its finished texture without dye tint");
                check(quads.stream().allMatch(q->q.getSprite().contents().name().toString().equals(FeudalItems.ID+":item/saddlebag_materials")),"All saddlebag faces use the custom leather and hardware atlas");
                check(client.getItemColors().getColor(bag,0)==-1,"Saddlebag no longer registers a dye color handler");
                var half=client.getItemRenderer().getModel(new ItemStack(MountContent.HALF.get()),client.level,client.player,0);
                var halfQuads=half.getQuads(null,null,net.minecraft.util.RandomSource.create(0));
                check(halfQuads.size()==42&&halfQuads.stream().noneMatch(net.minecraft.client.renderer.block.model.BakedQuad::isTinted),"Crafting half has the same seven-piece textured model");
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var mount=(AbstractHorse)player.serverLevel().getEntity(donkeyId);var lantern=MountEquipment.item(mount,3);LightItemFuel.set(lantern,100,false);MountEquipment.set(mount,3,lantern);});phase=2;ticks=0;return;
            }
            if(phase==2){
                var lamp=ItemStack.parse(client.level.registryAccess(),entity(client,donkeyId).getData(ModAttachments.MOUNT).getCompound("Slot3")).orElse(ItemStack.EMPTY);if(LightItemFuel.lit(lamp))return;
                check(entityLight(entity(client,donkeyId))==0,"Switching mounted lantern off removes dynamic light");
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var bag=new ItemStack(MountContent.SADDLEBAG.get());bag.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.EMERALD,3))));player.setItemSlot(EquipmentSlot.MAINHAND,bag);player.openMenu(new SimpleMenuProvider((id,inventory,p)->new BagMenu(id,inventory,bag),Component.literal("Saddlebag smoke test")));});phase=3;ticks=0;return;
            }
            if(phase==3){
                if(!(client.player.containerMenu instanceof BagMenu bag)||client.screen==null)return;
                if(bag.getContainer().getItem(0).isEmpty())return;
                check(bag.getContainer().getContainerSize()==27&&bag.getContainer().getItem(0).getCount()==3,"Saddlebag screen opens and synchronizes twenty-seven-slot cargo");
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var mount=(AbstractHorse)player.serverLevel().getEntity(horseId);player.openMenu(new SimpleMenuProvider((id,inventory,p)->new MountMenu(id,inventory,MountEquipment.inventory(mount)),Component.literal("Mount equipment")),buffer->buffer.writeVarInt(mount.getId()));});phase=4;ticks=0;return;
            }
            if(phase==4){
                if(!(client.player.containerMenu instanceof MountMenu menu)||client.screen==null )return;
                if(ticks<8)return;
                check(client.screen instanceof com.fantasyfeudalism.items.client.MountScreen screen&&screen.getYSize()==168,"Horse uses compact equipment screen");
                screenshot(client,"horse");
                check(menu.getContainer().getContainerSize()==36&&!menu.getSlot(3).isActive()&&!menu.getSlot(3).mayPlace(new ItemStack(Items.LANTERN)),"Horse equipment screen has no lantern slot");
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var mount=(AbstractHorse)player.serverLevel().getEntity(donkeyId);player.openMenu(new SimpleMenuProvider((id,inventory,p)->new MountMenu(id,inventory,MountEquipment.inventory(mount)),Component.literal("Donkey Equipment")),buffer->buffer.writeVarInt(mount.getId()));});phase=5;ticks=0;return;
            }
            if(phase==5){
                if(!(client.player.containerMenu instanceof MountMenu menu)||!(menu.horse instanceof Donkey)||menu.getContainer().getItem(4).isEmpty()||menu.getContainer().getItem(0).isEmpty())return;
                if(ticks<8)return;menu.refreshLayout();
                check(menu.getSlot(0).x==70&&menu.getSlot(2).x==90&&menu.getSlot(4).x==110&&menu.getSlot(5).x==130&&menu.getSlot(3).x==150,"Donkey saddle and bags synchronize in separate equipment slots");
                check(menu.getSlot(9).y==82&&menu.getSlot(36).y==154,"Donkey cargo and player inventory occupy separate sections");
                var equippedModel=client.getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(FeudalItems.ID,"item/saddlebag_equipped")));
                check(equippedModel.getQuads(null,null,net.minecraft.util.RandomSource.create(0)).size()==78,"Dedicated equipped saddlebag model is registered and baked");
                check(MountVisuals.sleepingBag(menu.horse).isEmpty(),"No sleeping bag renders before one is equipped");
                for(var color:DyeColor.values()){
                    var texture = FeudalItems.ID + ":block/sleeping_bag/" + color.getName();
                    var rollModel = client.getModelManager().getModel(com.fantasyfeudalism.items.client.MountLayer.sleepingBagModel(color));
                    var quads = rollModel.getQuads(null, null, net.minecraft.util.RandomSource.create(0));
                    check(rollModel != client.getModelManager().getMissingModel()
                            && quads.size() == 54
                            && quads.stream().noneMatch(net.minecraft.client.renderer.block.model.BakedQuad::isTinted)
                            && quads.stream().filter(q -> q.getSprite().contents().name().toString().equals(texture)).count() == 46,
                            "Mounted sleeping bag bakes dyed fabric, two straps, and two buckles: " + color.getName());
                    var roll=new ItemStack(com.fantasyfeudalism.items.sleep.SleepingBags.ITEMS.get(color).get());
                    var model=client.getItemRenderer().getModel(roll,client.level,client.player,0);
                    check(model != client.getModelManager().getMissingModel()
                            && model.getQuads(null,null,net.minecraft.util.RandomSource.create(0)).size() == 6
                            && model.getParticleIcon().contents().name().toString().equals(texture),
                            "Sleeping bag item uses its own texture: " + color.getName());
                    var block=com.fantasyfeudalism.items.sleep.SleepingBags.BLOCKS.get(color).get();
                    for(var part:net.minecraft.world.level.block.state.properties.BedPart.values()){
                        var state=block.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.PART,part);
                        var floorModel = client.getBlockRenderer().getBlockModel(state);
                        check(floorModel != client.getModelManager().getMissingModel()
                                && floorModel.getParticleIcon().contents().name().toString().equals(texture),
                                "Floor sleeping bag uses its own texture: " + color.getName() + " " + part);
                    }
                }
                client.getSingleplayerServer().execute(()->{var mount=(AbstractHorse)client.getSingleplayerServer().overworld().getEntity(donkeyId);MountEquipment.set(mount,5,new ItemStack(com.fantasyfeudalism.items.sleep.SleepingBags.ITEMS.get(DyeColor.BLUE).get()));});phase=6;ticks=0;return;
            }
            if(phase==6){
                var donkey=entity(client,donkeyId);if(donkey==null||MountVisuals.sleepingBag(donkey).isEmpty())return;
                check(com.fantasyfeudalism.items.sleep.SleepingBags.color(MountVisuals.sleepingBag(donkey))==DyeColor.BLUE,"Mounted sleeping bag and color synchronize to client");
                client.getSingleplayerServer().execute(()->{var mount=(AbstractHorse)client.getSingleplayerServer().overworld().getEntity(donkeyId);MountEquipment.set(mount,5,ItemStack.EMPTY);});phase=7;ticks=0;return;
            }
            if(phase==7){
                var donkey=entity(client,donkeyId);if(donkey==null||!MountVisuals.sleepingBag(donkey).isEmpty())return;
                check(MountVisuals.bag(donkey).is(com.fantasyfeudalism.items.mount.MountContent.SADDLEBAG.get()),"Removing roll hides it without removing saddlebags");
                // Keep the preview menu and entity equipped for visual inspection.
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var mount=(AbstractHorse)player.serverLevel().getEntity(donkeyId);var inventory=MountEquipment.inventory(mount);inventory.setItem(5,new ItemStack(com.fantasyfeudalism.items.sleep.SleepingBags.ITEMS.get(DyeColor.BLUE).get()));player.closeContainer();player.openMenu(new SimpleMenuProvider((id,inv,p)->new MountMenu(id,inv,inventory),Component.literal("Donkey Equipment")),buffer->buffer.writeVarInt(mount.getId()));});phase=8;ticks=0;return;
            }
            if(phase==8){
                if(!(client.player.containerMenu instanceof MountMenu menu)||menu.getContainer().getItem(5).isEmpty())return;
                if(ticks<8)return;
                check(client.screen instanceof com.fantasyfeudalism.items.client.MountScreen screen&&screen.getYSize()==236,"Fitted donkey screen shows all cargo and player inventory");
                screenshot(client,"donkey-fitted");
                check(!menu.getSlot(4).mayPickup(client.player)&&menu.getSlot(5).mayPickup(client.player),"Client equipment slots enforce sleeping bag removal first");
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var level=player.serverLevel();var mule=EntityType.MULE.create(level);mule.setPos(-3.5,150,3.5);mule.setTamed(true);mule.setOwnerUUID(player.getUUID());mule.setNoAi(true);level.addFreshEntity(mule);muleId=mule.getUUID();player.closeContainer();player.openMenu(new SimpleMenuProvider((id,inv,p)->new MountMenu(id,inv,MountEquipment.inventory(mule)),Component.literal("Mount Equipment")),buffer->buffer.writeVarInt(mule.getId()));});phase=9;ticks=0;return;
            }
            if(phase==9){
                if(!(client.player.containerMenu instanceof MountMenu menu)||!(menu.horse instanceof Mule)||menu.hasCargo()||ticks<8)return;
                if(!(client.screen instanceof com.fantasyfeudalism.items.client.MountScreen screen)||screen.getYSize()!=180)return;
                check(menu.getSlot(36).y==98&&!menu.getSlot(9).isActive()&&!menu.getSlot(5).isActive(),"Unfitted mule has compact inventory and locked storage/bedroll");
                check(screen.getGuiTop()>=0&&screen.getGuiTop()+screen.getYSize()<=client.getWindow().getGuiScaledHeight(),"Compact screen fits the client viewport");
                screenshot(client,"mule-empty");
                client.getSingleplayerServer().execute(()->{var mount=(AbstractHorse)client.getSingleplayerServer().overworld().getEntity(muleId);var inventory=MountEquipment.inventory(mount);inventory.setItem(4,new ItemStack(MountContent.SADDLEBAG.get()));inventory.setItem(9,new ItemStack(Items.EMERALD,7));});phase=10;ticks=0;return;
            }
            if(phase==10){
                if(!(client.player.containerMenu instanceof MountMenu menu)||!menu.hasCargo()||ticks<8)return;
                if(!(client.screen instanceof com.fantasyfeudalism.items.client.MountScreen screen)||screen.getYSize()!=236)return;
                check(menu.getSlot(36).y==154&&menu.getSlot(9).getItem().getCount()==7,"Live bag installation expands the same open screen and syncs cargo");
                check(screen.getGuiTop()>=0&&screen.getGuiTop()+screen.getYSize()<=client.getWindow().getGuiScaledHeight(),"Expanded screen fits the client viewport");
                screenshot(client,"mule-expanded");
                client.getSingleplayerServer().execute(()->{var mount=(AbstractHorse)client.getSingleplayerServer().overworld().getEntity(muleId);MountEquipment.inventory(mount).removeItem(4,1);});phase=11;ticks=0;return;
            }
            if(phase==11){
                if(!(client.player.containerMenu instanceof MountMenu menu)||menu.hasCargo()||ticks<8)return;
                if(!(client.screen instanceof com.fantasyfeudalism.items.client.MountScreen screen)||screen.getYSize()!=180)return;
                check(menu.getSlot(36).y==98&&!menu.getSlot(9).isActive(),"Live bag removal collapses the same open screen and disables cargo");
                hoofIndex=0;client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var mount=(AbstractHorse)player.serverLevel().getEntity(horseId);var inv=MountEquipment.inventory(mount);inv.setItem(2,new ItemStack(MountContent.COPPER.get()));player.closeContainer();player.openMenu(new SimpleMenuProvider((id,inventory,p)->new MountMenu(id,inventory,inv),Component.literal("Horse")),buffer->buffer.writeVarInt(mount.getId()));});phase=12;ticks=0;return;
            }
            if(phase==12){
                var shoes=List.of(MountContent.COPPER.get(),MountContent.IRON.get(),MountContent.GOLD.get(),MountContent.DIAMOND.get(),MountContent.NETHERITE.get());
                if(!(client.player.containerMenu instanceof MountMenu menu)||menu.horse!=horse||ticks<8||!MountVisuals.shoe(horse).is(shoes.get(hoofIndex)))return;
                check(Horseshoe.itemColor(MountVisuals.shoe(horse),0)==Horseshoe.itemColor(new ItemStack(shoes.get(hoofIndex)),0),"Hoof material synchronizes: "+hoofIndex);
                screenshot(client,"hoof-"+hoofIndex);hoofIndex++;
                client.getSingleplayerServer().execute(()->{var mount=(AbstractHorse)client.getSingleplayerServer().overworld().getEntity(horseId);MountEquipment.inventory(mount).setItem(2,hoofIndex<5?new ItemStack(shoes.get(hoofIndex)):ItemStack.EMPTY);});ticks=0;if(hoofIndex==5)phase=13;return;
            }
            if(phase==13){
                if(!MountVisuals.shoe(horse).isEmpty()||ticks<8)return;
                check(MountVisuals.shoe(horse).isEmpty(),"Removing horseshoe restores original hoof appearance");screenshot(client,"hoof-none");
                report(true,null);phase=-1;
            }
        }catch(Throwable error){error.printStackTrace();report(false,error);phase=-1;}
    }
}
