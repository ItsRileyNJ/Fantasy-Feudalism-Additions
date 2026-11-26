package com.fantasyfeudalism.items.mount;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;

/** Mount equipment followed by the saddlebag and Pack Storage cargo. */
public final class MountInventory extends SimpleContainer {
    public static final int PACK_SLOT = 6;
    public static final int SPYGLASS_SLOT = 7;
    public static final int BAG_CARGO_START = 9;
    public static final int PACK_CARGO_START = 36;
    public static final int PACK_CAPACITY = 20;
    public static final int SIZE = PACK_CARGO_START + PACK_CAPACITY;

    private final AbstractHorse horse;
    private boolean ready, writing;
    private ItemStack loadedBag=ItemStack.EMPTY;
    private ItemStack loadedPack=ItemStack.EMPTY;
    public MountInventory(AbstractHorse horse) {
        super(horse instanceof Horse ? PACK_CARGO_START : SIZE);this.horse=horse;writing=true;
        super.setItem(0,horse.getSlot(400).get());super.setItem(1,horse.getBodyArmorItem());
        for(int i=2;i<=SPYGLASS_SLOT;i++)super.setItem(i,MountEquipment.item(horse,i));
        loadedBag = loadCargo(4, BAG_CARGO_START, PACK_CARGO_START);
        if (!(horse instanceof Horse)) loadedPack = loadCargo(PACK_SLOT, PACK_CARGO_START, SIZE);
        writing=false;ready=true;
    }
    private ItemStack loadCargo(int slot, int start, int end) {
        var equipment = getItem(slot);
        var items = NonNullList.withSize(end - start, ItemStack.EMPTY);
        if (!equipment.isEmpty()) equipment.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
        for (int index = start; index < end; index++) super.setItem(index, items.get(index - start));
        return equipment;
    }

    private void saveCargo(ItemStack equipment, int start, int end) {
        if (equipment.isEmpty()) return;
        var items = java.util.stream.IntStream.range(start, end).mapToObj(this::getItem).toList();
        equipment.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    public AbstractHorse horse(){return horse;}
    @Override public boolean canPlaceItem(int slot,ItemStack item) {
        return allowed(horse, slot, item, !getItem(4).isEmpty(), getItem(PACK_SLOT).is(MountContent.PACK_STORAGE.get()));
    }
    public static boolean allowed(AbstractHorse horse,int slot,ItemStack item,boolean bagPresent){
        return allowed(horse, slot, item, bagPresent, false);
    }

    static boolean allowed(AbstractHorse horse, int slot, ItemStack item, boolean bagPresent, boolean packPresent) {
        boolean packMount = horse instanceof Donkey || horse instanceof Mule;
        return switch(slot) {
            case 0->item.is(Items.SADDLE);
            case 1->horse instanceof Horse && item.getItem() instanceof AnimalArmorItem armor && armor.getBodyType()==AnimalArmorItem.BodyType.EQUESTRIAN;
            case 2->MountEquipment.supported(horse) && item.getItem() instanceof Horseshoe;
            case 3->packMount&&(item.is(Items.LANTERN)||item.is(Items.SOUL_LANTERN));
            case 5->packMount&&bagPresent&&com.fantasyfeudalism.items.sleep.SleepingBags.isSleepingBag(item);
            case 4->packMount&&item.is(MountContent.SADDLEBAG.get());
            case PACK_SLOT -> packMount && bagPresent && item.is(MountContent.PACK_STORAGE.get());
            case SPYGLASS_SLOT -> packMount && bagPresent && item.is(Items.SPYGLASS);
            default -> packMount && bagPresent && slot >= BAG_CARGO_START && slot < SIZE
                    && (slot < PACK_CARGO_START || packPresent) && SaddlebagItem.allowed(item);
        };
    }
    @Override public void setChanged() {
        super.setChanged();if(!ready||writing)return;writing=true;
        try {
            if (getItem(4).isEmpty() || getItem(4) != loadedBag) loadedBag = loadCargo(4, BAG_CARGO_START, PACK_CARGO_START);
            saveCargo(loadedBag, BAG_CARGO_START, PACK_CARGO_START);
            if (!(horse instanceof Horse)) {
                if (getItem(PACK_SLOT).isEmpty() || getItem(PACK_SLOT) != loadedPack) loadedPack = loadCargo(PACK_SLOT, PACK_CARGO_START, SIZE);
                saveCargo(loadedPack, PACK_CARGO_START, SIZE);
            }
            horse.getSlot(400).set(getItem(0));horse.setBodyArmorItem(getItem(1));
            for(int i=2;i<=SPYGLASS_SLOT;i++)MountEquipment.set(horse,i,getItem(i));
            MountEquipment.apply(horse);
        } finally {
            writing = false;
        }
    }
    @Override public boolean stillValid(Player player) {return horse.isAlive()&&horse.distanceToSqr(player)<64;}
}
