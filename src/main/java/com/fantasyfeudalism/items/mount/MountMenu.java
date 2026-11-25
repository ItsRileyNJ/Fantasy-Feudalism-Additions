package com.fantasyfeudalism.items.mount;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class MountMenu extends ChestMenu {
    public final net.minecraft.world.entity.animal.horse.AbstractHorse horse;
    private boolean cargoLayout;
    private static final class ClientInventory extends SimpleContainer {
        final net.minecraft.world.entity.animal.horse.AbstractHorse horse;
        ClientInventory(net.minecraft.world.entity.animal.horse.AbstractHorse horse){super(36);this.horse=horse;}
        @Override public boolean canPlaceItem(int slot,ItemStack item){return horse!=null&&MountInventory.allowed(horse,slot,item,!getItem(4).isEmpty());}
    }
    private static net.minecraft.world.entity.animal.horse.AbstractHorse mount(Container container){return container instanceof MountInventory server?server.horse():container instanceof ClientInventory client?client.horse:null;}
    public static MountMenu client(int id,Inventory inventory,net.minecraft.network.RegistryFriendlyByteBuf data){var entity=inventory.player.level().getEntity(data.readVarInt());return new MountMenu(id,inventory,new ClientInventory(entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse h?h:null));}
    public boolean disabled(int index){return index<9&&(getRowCount()==1?index>2:index!=0&&index!=2&&index!=3&&index!=4&&index!=5);}

    public MountMenu(int id,Inventory inventory,Container container){super(MountContent.MOUNT_MENU.get(),id,inventory,container,mount(container) instanceof net.minecraft.world.entity.animal.horse.Horse?1:4);horse=mount(container);refreshLayout();}
    public boolean hasCargo(){return getRowCount()>1&&!getContainer().getItem(4).isEmpty();}
    public int playerInventoryY(){return getRowCount()==1?86:hasCargo()?154:98;}
    public int panelHeight(){return playerInventoryY()+82;}
    /** Slot indices never change, so in-flight clicks and remote stack updates stay valid. */
    public boolean refreshLayout(){
        boolean cargo=hasCargo();if(cargo==cargoLayout)return false;cargoLayout=cargo;
        for(int i=getRowCount()*9;i<slots.size();i++){var replacement=layoutSlot(slots.get(i),i);replacement.index=i;slots.set(i,replacement);}
        return true;
    }
    @Override public void broadcastChanges(){refreshLayout();super.broadcastChanges();}
    @Override protected Slot addSlot(Slot slot){
        return super.addSlot(layoutSlot(slot,slots.size()));
    }
    private Slot layoutSlot(Slot slot,int number){
        int storage=getRowCount()*9;
        Container source=slot.container;int sourceIndex=slot.getSlotIndex();
        boolean equine=getRowCount()==1;
        boolean hidden=number<9&&(equine?number>2:number!=0&&number!=2&&number!=3&&number!=4&&number!=5);
        int x,y;
        if(number<9){x=hidden?-1000:(equine?76+number*22:70+(number==0?0:number==2?1:number==4?2:number==5?3:4)*20);y=32;}
        else if(number<storage){int cargo=number-9;x=8+cargo%9*18;y=82+cargo/9*18;}
        else{int inventory=number-storage;x=8+inventory%9*18;y=playerInventoryY()+(inventory<27?inventory/9*18:58);}
        return new Slot(source,sourceIndex,x,y){
            @Override public boolean isActive(){if(number==5&&getContainer().getItem(4).isEmpty()&&getContainer().getItem(5).isEmpty())return false;return !hidden&&(number<9||number>=storage||!getContainer().getItem(4).isEmpty());}
            @Override public boolean mayPickup(Player player){return isActive()&&(number!=4||getContainer().getItem(5).isEmpty())&&super.mayPickup(player);}
            @Override public boolean mayPlace(ItemStack item){return !hidden&&(number>=storage||source.canPlaceItem(sourceIndex,item));}
            @Override public int getMaxStackSize(){return number<9?1:super.getMaxStackSize();}
        };
    }

    @Override public ItemStack quickMoveStack(Player player,int slot){
        if(slot==4&&!getContainer().getItem(5).isEmpty())return ItemStack.EMPTY;
        var result=super.quickMoveStack(player,slot);refreshLayout();return result;
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player){
        if(slot==4&&!getContainer().getItem(4).isEmpty()&&!getContainer().getItem(5).isEmpty()&&(type==ClickType.PICKUP||type==ClickType.QUICK_MOVE||type==ClickType.SWAP||type==ClickType.THROW)){
            if(!player.level().isClientSide)player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.fantasy_feudalism_items.remove_sleeping_bag"),true);
            return;
        }
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
        super.clicked(slot,button,type,player);refreshLayout();
    }
}
