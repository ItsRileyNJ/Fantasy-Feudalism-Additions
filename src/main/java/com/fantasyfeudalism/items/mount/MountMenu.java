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
        ClientInventory(net.minecraft.world.entity.animal.horse.AbstractHorse horse){super(horse instanceof net.minecraft.world.entity.animal.horse.Horse ? 36 : MountInventory.SIZE);this.horse=horse;}
        @Override public boolean canPlaceItem(int slot,ItemStack item){return horse!=null&&MountInventory.allowed(horse,slot,item,!getItem(4).isEmpty(),getItem(MountInventory.PACK_SLOT).is(MountContent.PACK_STORAGE.get()));}
    }
    private static net.minecraft.world.entity.animal.horse.AbstractHorse mount(Container container){return container instanceof MountInventory server?server.horse():container instanceof ClientInventory client?client.horse:null;}
    public static MountMenu client(int id,Inventory inventory,net.minecraft.network.RegistryFriendlyByteBuf data){var entity=inventory.player.level().getEntity(data.readVarInt());return new MountMenu(id,inventory,new ClientInventory(entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse h?h:null));}
    private static boolean requiresBags(int slot) {
        return slot == 5 || slot == MountInventory.PACK_SLOT || slot == MountInventory.SPYGLASS_SLOT;
    }

    public boolean disabled(int index) {
        return index < 9 && (getRowCount() == 1 ? index > 2 : index == 1 || index == 8);
    }

    public MountMenu(int id, Inventory inventory, Container container) {
        super(MountContent.MOUNT_MENU.get(), id, inventory, container,
                mount(container) instanceof net.minecraft.world.entity.animal.horse.Horse ? 1 : 4);
        horse = mount(container);
        if (getRowCount() > 1) {
            for (int i = 0; i < MountInventory.PACK_CAPACITY; i++) {
                addSlot(new Slot(container, MountInventory.PACK_CARGO_START + i, 184 + i % 5 * 18, 82 + i / 5 * 18));
            }
        }
        refreshLayout();
    }

    public boolean hasCargo(){return getRowCount()>1&&!getContainer().getItem(4).isEmpty();}
    public boolean hasPack() {
        return hasCargo() && getContainer().getItem(MountInventory.PACK_SLOT).is(MountContent.PACK_STORAGE.get());
    }
    public boolean hasBagAttachments() {
        return !getContainer().getItem(5).isEmpty() || !getContainer().getItem(MountInventory.PACK_SLOT).isEmpty()
                || !getContainer().getItem(MountInventory.SPYGLASS_SLOT).isEmpty();
    }
    private int packMenuStart() { return getRowCount() * 9 + 36; }
    public int playerInventoryY(){return getRowCount()==1?86:hasCargo()?154:98;}
    public int panelHeight(){return playerInventoryY()+82;}
    /** Slot indices never change, so in-flight clicks and remote stack updates stay valid. */
    public boolean refreshLayout(){
        boolean cargo=hasCargo();if(cargo==cargoLayout)return false;cargoLayout=cargo;
        for(int i=getRowCount()*9;i<packMenuStart();i++){var replacement=layoutSlot(slots.get(i),i);replacement.index=i;slots.set(i,replacement);}
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
        boolean hidden = disabled(number);
        boolean packCargo = number >= packMenuStart();
        int x,y;
        if (packCargo) {
            x = slot.x;
            y = slot.y;
        } else if (number < 9) {
            if (hidden) x = -1000;
            else if (equine) x = 76 + number * 22;
            else x = switch (number) {
                case 0 -> 70;
                case 2 -> 90;
                case 4 -> 110;
                case MountInventory.PACK_SLOT -> 184;
                case MountInventory.SPYGLASS_SLOT -> 204;
                case 3 -> 224;
                case 5 -> 244;
                default -> throw new IllegalStateException("Unexpected mount equipment slot: " + number);
            };
            y = 32;
        }
        else if(number<storage){int cargo=number-9;x=8+cargo%9*18;y=82+cargo/9*18;}
        else{int inventory=number-storage;x=8+inventory%9*18;y=playerInventoryY()+(inventory<27?inventory/9*18:58);}
        return new Slot(source,sourceIndex,x,y){
            @Override public boolean isActive() {
                if (hidden) return false;
                if (packCargo) return hasPack();
                if (requiresBags(number)) return hasCargo() || hasItem();
                return number < 9 || number >= storage || hasCargo();
            }
            @Override public boolean mayPickup(Player player) {
                return isActive() && (number != 4 || !hasBagAttachments()) && super.mayPickup(player);
            }
            @Override public boolean mayPlace(ItemStack item) {
                return isActive() && (number >= storage && !packCargo || source.canPlaceItem(sourceIndex, item));
            }
            @Override public int getMaxStackSize(){return number<9?1:super.getMaxStackSize();}
        };
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        var slot = getSlot(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;

        var stack = slot.getItem();
        var original = stack.copy();
        int playerStart = getRowCount() * 9;
        int packStart = packMenuStart();
        boolean moved;
        if (index < playerStart || index >= packStart) {
            moved = moveItemStackTo(stack, playerStart, packStart, true);
        } else {
            moved = moveItemStackTo(stack, 0, playerStart, false);
            if (!stack.isEmpty() && hasPack()) {
                moved |= moveItemStackTo(stack, packStart, slots.size(), false);
            }
        }
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        refreshLayout();
        return original;
    }

    @Override public void clicked(int slot,int button,ClickType type,Player player){
        if(slot==4&&!getContainer().getItem(4).isEmpty()&&hasBagAttachments()&&(type==ClickType.PICKUP||type==ClickType.QUICK_MOVE||type==ClickType.SWAP||type==ClickType.THROW)){
            if(!player.level().isClientSide)player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    getContainer().getItem(5).isEmpty() ? "message.fantasy_feudalism_items.remove_bag_addons"
                            : "message.fantasy_feudalism_items.remove_sleeping_bag"),true);
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
