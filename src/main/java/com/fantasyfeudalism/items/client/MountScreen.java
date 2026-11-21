package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.mount.MountMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Equipment has labeled, separated slots; saddlebag cargo has its own section. */
public final class MountScreen extends AbstractContainerScreen<MountMenu> {
    private final boolean equine;
    public MountScreen(MountMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);equine=menu.getRowCount()==1;imageHeight=equine?158:230;inventoryLabelY=(equine?74:146)-12;
    }
    private void slot(GuiGraphics gui,int x,int y,boolean active){
        gui.fill(x-1,y-1,x+17,y+17,0xFF373737);gui.fill(x,y,x+17,y+17,0xFFFFFFFF);gui.fill(x,y,x+16,y+16,active?0xFF8B8B8B:0xFFB0B0B0);
    }
    @Override protected void renderBg(GuiGraphics gui,float partial,int mouseX,int mouseY){
        int x=leftPos,y=topPos;
        gui.fill(x,y,x+imageWidth,y+imageHeight,0xFF373737);gui.fill(x+1,y+1,x+imageWidth-1,y+imageHeight-1,0xFFC6C6C6);
        gui.fill(x+2,y+2,x+imageWidth-2,y+3,0xFFFFFFFF);
        for(var s:menu.slots){if(s.x<0||(!equine&&s.index>=9&&s.index<36&&menu.getContainer().getItem(4).isEmpty()))continue;slot(gui,x+s.x,y+s.y,s.isActive());}
        if(!equine)for(int column=1;column<4;column++)gui.fill(x+8+column*42,y+18,x+9+column*42,y+52,0xFFAAAAAA);
        gui.fill(x+7,y+54,x+169,y+55,0xFFAAAAAA);
    }
    private void label(GuiGraphics gui,String key,int center){
        var text=Component.translatable(key);gui.drawString(font,text,center-font.width(text)/2,20,0x404040,false);
    }
    @Override protected void renderLabels(GuiGraphics gui,int mouseX,int mouseY){
        gui.drawString(font,title,8,6,0x404040,false);
        if(equine){label(gui,"item.minecraft.saddle",25);label(gui,"container.fantasy_feudalism_items.armor",67);label(gui,"container.fantasy_feudalism_items.shoes",109);label(gui,"block.minecraft.lantern",151);}
        else{label(gui,"item.minecraft.saddle",25);label(gui,"container.fantasy_feudalism_items.bags",67);label(gui,"container.fantasy_feudalism_items.bedroll",109);label(gui,"block.minecraft.lantern",151);
            gui.drawString(font,Component.translatable("container.fantasy_feudalism_items.bag_storage"),8,60,0x404040,false);
            if(menu.getContainer().getItem(4).isEmpty()){var text=Component.translatable("container.fantasy_feudalism_items.equip_bags");gui.drawString(font,text,(imageWidth-font.width(text))/2,94,0x404040,false);}
        }
        gui.drawString(font,playerInventoryTitle,8,inventoryLabelY,0x404040,false);
    }
    @Override public void render(GuiGraphics gui,int mouseX,int mouseY,float partial){super.render(gui,mouseX,mouseY,partial);renderTooltip(gui,mouseX,mouseY);}
}
