package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.lighting.LightItemFuel;
import com.fantasyfeudalism.items.mount.*;
import com.fantasyfeudalism.items.sleep.SleepingBags;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;

/** Compact equipment overview, expanding only when the mount has saddlebag cargo. */
public final class MountScreen extends AbstractContainerScreen<MountMenu> {
    private final boolean equine;
    private final int[] equipment;
    private final ItemStack[] icons;
    private int lastHealth=-1,lastMaxHealth=-1;
    private Component health=Component.empty();
    public MountScreen(MountMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);equine=menu.getRowCount()==1;
        equipment=equine?new int[]{0,1,2,3}:new int[]{0,4,5,3};
        icons=equine?new ItemStack[]{new ItemStack(Items.SADDLE),new ItemStack(Items.IRON_HORSE_ARMOR),new ItemStack(MountContent.IRON.get()),new ItemStack(Items.LANTERN)}:
            new ItemStack[]{new ItemStack(Items.SADDLE),new ItemStack(MountContent.SADDLEBAG.get()),new ItemStack(SleepingBags.ITEMS.get(DyeColor.WHITE).get()),new ItemStack(Items.LANTERN)};
        imageHeight=menu.panelHeight();inventoryLabelY=menu.playerInventoryY()-12;
    }
    private void updateLayout(){
        boolean changed=menu.refreshLayout();int newHeight=menu.panelHeight();
        if(changed||imageHeight!=newHeight){
            imageHeight=newHeight;inventoryLabelY=menu.playerInventoryY()-12;topPos=(height-imageHeight)/2;
            // Slot objects and screen position have changed; do not carry a drag across layouts.
            quickCraftSlots.clear();isQuickCrafting=false;
        }
    }
    private void slot(GuiGraphics gui,Slot slot){
        int x=leftPos+slot.x,y=topPos+slot.y;
        gui.fill(x-1,y-1,x+17,y+17,0xFF373737);gui.fill(x,y,x+17,y+17,0xFFFFFFFF);
        gui.fill(x,y,x+16,y+16,slot.isActive()?0xFF8B8B8B:0xFF666666);
    }
    private void emptyIcon(GuiGraphics gui,Slot slot,ItemStack icon){
        int x=leftPos+slot.x,y=topPos+slot.y;
        gui.renderFakeItem(icon,x,y);
        gui.pose().pushPose();gui.pose().translate(0,0,200);
        gui.fill(x,y,x+16,y+16,slot.isActive()?0xB08B8B8B:0xCC666666);
        if(!slot.isActive()){
            // Small lock badge distinguishes unavailable equipment from an empty active slot.
            gui.fill(x+10,y+8,x+14,y+13,0xFF333333);gui.fill(x+9,y+11,x+15,y+16,0xFF333333);
            gui.fill(x+11,y+9,x+13,y+11,0xFFB8B8B8);gui.fill(x+11,y+12,x+13,y+15,0xFFB8B8B8);
        }
        gui.pose().popPose();
    }
    @Override protected void renderBg(GuiGraphics gui,float partial,int mouseX,int mouseY){
        int x=leftPos,y=topPos;
        gui.fill(x,y,x+imageWidth,y+imageHeight,0xFF373737);
        gui.fill(x+1,y+1,x+imageWidth-1,y+imageHeight-1,0xFFC6C6C6);
        gui.fill(x+1,y+1,x+imageWidth-1,y+2,0xFFFFFFFF);gui.fill(x+1,y+1,x+2,y+imageHeight-1,0xFFFFFFFF);
        gui.fill(x+imageWidth-2,y+2,x+imageWidth-1,y+imageHeight-1,0xFF555555);gui.fill(x+2,y+imageHeight-2,x+imageWidth-1,y+imageHeight-1,0xFF555555);
        gui.fill(x+7,y+19,x+63,y+67,0xFF373737);gui.fill(x+8,y+20,x+62,y+66,0xFF686868);
        if(menu.horse!=null)InventoryScreen.renderEntityInInventoryFollowsMouse(gui,x+9,y+21,x+61,y+65,23,0,mouseX,mouseY,menu.horse);
        for(var s:menu.slots){if(s.x<0||(s.index>=9&&s.index<36&&!equine&&!menu.hasCargo()))continue;slot(gui,s);}
        for(int i=0;i<equipment.length;i++){var s=menu.getSlot(equipment[i]);if(s.getItem().isEmpty())emptyIcon(gui,s,icons[i]);}
        gui.fill(x+8,y+68,x+168,y+69,0xFFAAAAAA);
        if(menu.hasCargo())gui.fill(x+8,y+139,x+168,y+140,0xFFAAAAAA);
    }
    @Override protected void renderLabels(GuiGraphics gui,int mouseX,int mouseY){
        var name=menu.horse==null?title:menu.horse.getDisplayName();
        gui.drawString(font,net.minecraft.locale.Language.getInstance().getVisualOrder(font.substrByWidth(name,160)),8,6,0x404040,false);
        gui.drawString(font,Component.translatable("container.fantasy_feudalism_items.equipment"),70,20,0x404040,false);
        if(menu.horse!=null){
            int current=net.minecraft.util.Mth.ceil(menu.horse.getHealth()),maximum=net.minecraft.util.Mth.ceil(menu.horse.getMaxHealth());
            if(current!=lastHealth||maximum!=lastMaxHealth){health=Component.translatable("container.fantasy_feudalism_items.mount_health",current,maximum);lastHealth=current;lastMaxHealth=maximum;}
            gui.drawString(font,health,70,54,0x404040,false);
        }
        if(menu.hasCargo())gui.drawString(font,Component.translatable("container.fantasy_feudalism_items.bag_storage"),8,71,0x404040,false);
        gui.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);
    }
    private Component slotName(int index){return switch(index){
        case 0->Component.translatable("item.minecraft.saddle");
        case 1->Component.translatable("container.fantasy_feudalism_items.horse_armor");
        case 2->Component.translatable("container.fantasy_feudalism_items.horseshoe");
        case 4->Component.translatable("container.fantasy_feudalism_items.saddlebags");
        case 5->Component.translatable("container.fantasy_feudalism_items.sleeping_bag");
        default->Component.translatable("block.minecraft.lantern");
    };}
    private void hints(List<Component> lines,int index,ItemStack stack){
        if(index==4){
            lines.add(Component.translatable("tooltip.fantasy_feudalism_items.mount.storage").withStyle(ChatFormatting.GRAY));
            if(!menu.getContainer().getItem(5).isEmpty())lines.add(Component.translatable("message.fantasy_feudalism_items.remove_sleeping_bag").withStyle(ChatFormatting.YELLOW));
        }else if(index==5&&!menu.hasCargo())lines.add(Component.translatable("tooltip.fantasy_feudalism_items.mount.requires_bags").withStyle(ChatFormatting.YELLOW));
        else if(index==3){
            if(!stack.isEmpty())lines.add(Component.translatable(LightItemFuel.lit(stack)?"tooltip.fantasy_feudalism_items.mount.turn_off":"tooltip.fantasy_feudalism_items.mount.turn_on").withStyle(ChatFormatting.GRAY));
            if(!stack.is(Items.SOUL_LANTERN))lines.add(Component.translatable("tooltip.fantasy_feudalism_items.mount.refuel").withStyle(ChatFormatting.GRAY));
        }
    }
    @Override protected List<Component> getTooltipFromContainerItem(ItemStack stack){
        var lines=new ArrayList<>(super.getTooltipFromContainerItem(stack));
        if(hoveredSlot!=null&&hoveredSlot.index<9)hints(lines,hoveredSlot.index,stack);return lines;
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float partial){
        updateLayout();super.render(gui,mouseX,mouseY,partial);renderTooltip(gui,mouseX,mouseY);
        if(menu.getCarried().isEmpty())for(int index:equipment){
            var s=menu.getSlot(index);if(!s.hasItem()&&isHovering(s.x,s.y,16,16,mouseX,mouseY)){
                var lines=new ArrayList<Component>();lines.add(slotName(index));hints(lines,index,ItemStack.EMPTY);
                lines.add(Component.translatable("tooltip.fantasy_feudalism_items.mount.shift_click").withStyle(ChatFormatting.GRAY));
                gui.renderComponentTooltip(font,lines,mouseX,mouseY);break;
            }
        }
    }
}
