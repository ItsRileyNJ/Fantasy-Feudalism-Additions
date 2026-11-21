package com.fantasyfeudalism.items.mount;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class MountMenu extends ChestMenu {
    public final net.minecraft.world.entity.animal.horse.AbstractHorse horse;
    private static final class ClientInventory extends SimpleContainer {
        final net.minecraft.world.entity.animal.horse.AbstractHorse horse;
        ClientInventory(net.minecraft.world.entity.animal.horse.AbstractHorse horse){super(36);this.horse=horse;}
        @Override public boolean canPlaceItem(int slot,ItemStack item){return horse!=null&&MountInventory.allowed(horse,slot,item,!getItem(4).isEmpty());}
    }
    private static net.minecraft.world.entity.animal.horse.AbstractHorse mount(Container container){return container instanceof MountInventory server?server.horse():container instanceof ClientInventory client?client.horse:null;}
    public static MountMenu client(int id,Inventory inventory,net.minecraft.network.RegistryFriendlyByteBuf data){var entity=inventory.player.level().getEntity(data.readVarInt());return new MountMenu(id,inventory,new ClientInventory(entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse h?h:null));}
    public boolean disabled(int index){return index<9&&(index>=5||(horse instanceof net.minecraft.world.entity.animal.horse.Horse?index==4:index==1||index==2));}
    public MountMenu(int id,Inventory inventory,Container container){super(MountContent.MOUNT_MENU.get(),id,inventory,container,mount(container) instanceof net.minecraft.world.entity.animal.horse.Horse?1:4);horse=mount(container);}
    @Override protected Slot addSlot(Slot slot){
        int number=slots.size();int storage=getRowCount()*9;
        boolean equine=getRowCount()==1;
        boolean hidden=number<9&&(equine?number>3:number!=0&&number!=3&&number!=4);
        int x,y;
        if(number<9){x=hidden?-1000:equine?17+number*42:number==0?23:number==4?79:135;y=32;}
        else if(number<storage){int cargo=number-9;x=8+cargo%9*18;y=72+cargo/9*18;}
        else{int inventory=number-storage;x=8+inventory%9*18;y=(equine?74:146)+(inventory<27?inventory/9*18:58);}
        return super.addSlot(new Slot(slot.container,slot.getSlotIndex(),x,y){
            @Override public boolean isActive(){return !hidden&&(number<9||number>=storage||!getContainer().getItem(4).isEmpty());}
            @Override public boolean mayPickup(Player player){return isActive()&&super.mayPickup(player);}
            @Override public boolean mayPlace(ItemStack item){return !hidden&&(number>=storage||slot.container.canPlaceItem(slot.getSlotIndex(),item));}
            @Override public int getMaxStackSize(){return number<9?1:super.getMaxStackSize();}
        });
    }

    @Override public void clicked(int slot,int button,ClickType type,Player player){
        if(slot==3&&button==1&&!getContainer().getItem(3).isEmpty()){
            var lantern=getContainer().getItem(3);
            if(type==ClickType.QUICK_MOVE){MountEquipment.toggle(lantern);getContainer().setChanged();broadcastChanges();return;}
            if(type==ClickType.PICKUP&&com.fantasyfeudalism.items.lighting.GlowOil.isOil(getCarried())&&lantern.is(Items.LANTERN)){
                MountEquipment.refill(lantern,getCarried().is(com.fantasyfeudalism.items.content.Content.GLOW_OIL.get()));
                if(horse!=null)com.fantasyfeudalism.items.lighting.LightItemFuel.resetProgress(horse);
                if(!player.getAbilities().instabuild){getCarried().shrink(1);var bottle=new ItemStack(Items.GLASS_BOTTLE);if(getCarried().isEmpty())setCarried(bottle);else if(!player.getInventory().add(bottle))player.drop(bottle,false);}
                getContainer().setChanged();broadcastChanges();return;
            }
        }
        super.clicked(slot,button,type,player);
    }
}
