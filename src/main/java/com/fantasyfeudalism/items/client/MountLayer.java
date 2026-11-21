package com.fantasyfeudalism.items.client;
import com.fantasyfeudalism.items.content.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.*;
public final class MountLayer<T extends AbstractHorse,M extends HorseModel<T>> extends RenderLayer<T,M> {
    public static final net.minecraft.client.resources.model.ModelResourceLocation SADDLEBAG_MODEL=net.minecraft.client.resources.model.ModelResourceLocation.standalone(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("fantasy_feudalism_items","item/saddlebag_equipped"));
    public static final net.minecraft.client.resources.model.ModelResourceLocation SLEEPING_BAG_MODEL=net.minecraft.client.resources.model.ModelResourceLocation.standalone(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("fantasy_feudalism_items","item/sleeping_bag_equipped"));
    public MountLayer(RenderLayerParent<T,M> parent){super(parent);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,T horse,float swing,float amplitude,float partial,float age,float yaw,float pitch){
        var bag=com.fantasyfeudalism.items.mount.MountVisuals.bag(horse);
        var lantern=com.fantasyfeudalism.items.mount.MountVisuals.lantern(horse);
        if(bag.isEmpty()&&lantern.isEmpty())return;
        pose.pushPose();((com.fantasyfeudalism.items.client.mixin.HorseBodyAccessor)getParentModel()).feudalItems$body().translateAndRotate(pose);
        if(horse.isBaby())pose.scale(.5f,.5f,.5f);
        if(!bag.isEmpty()){
            pose.pushPose();pose.scale(1,-1,-1);
            var model=Minecraft.getInstance().getModelManager().getModel(SADDLEBAG_MODEL);
            Minecraft.getInstance().getItemRenderer().render(bag,ItemDisplayContext.NONE,false,pose,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,model);
            pose.popPose();
        }
        var sleepingBag=com.fantasyfeudalism.items.mount.MountVisuals.sleepingBag(horse);
        if(!bag.isEmpty()&&!sleepingBag.isEmpty()){
            pose.pushPose();pose.scale(1,-1,-1);
            var model=Minecraft.getInstance().getModelManager().getModel(SLEEPING_BAG_MODEL);
            Minecraft.getInstance().getItemRenderer().render(sleepingBag,ItemDisplayContext.NONE,false,pose,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,model);
            pose.popPose();
        }
        if(!lantern.isEmpty()){
            if(bag.isEmpty())renderItem(lantern,pose,buffers,light,horse,.47,-.05,-.65,.45f);
            else renderHangingLantern(lantern,pose,buffers,light,horse,swing,amplitude,age);
        }
        pose.popPose();
    }
    private void renderHangingLantern(ItemStack lantern,PoseStack pose,MultiBufferSource buffers,int light,T horse,float walk,float amplitude,float age){
        pose.pushPose();
        pose.translate(SaddlebagAttachment.X,SaddlebagAttachment.Y,SaddlebagAttachment.Z);
        // Entity-model +Y points down; +Z points toward the animal's rear.
        float movement=net.minecraft.util.Mth.clamp(amplitude,0,1);
        float phase=horse.getId()*.7f;
        float outward=SaddlebagAttachment.OUTWARD_TILT+net.minecraft.util.Mth.sin(age*.08f+phase)*.8f+net.minecraft.util.Mth.sin(walk*.8f)*movement*5;
        float rearward=SaddlebagAttachment.REARWARD_TILT+net.minecraft.util.Mth.sin(age*.065f+phase+1.1f)*.6f+net.minecraft.util.Mth.sin(walk*.8f+1.2f)*movement*4;
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-Math.copySign(outward,(float)SaddlebagAttachment.X)));
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rearward));
        pose.pushPose();
        pose.scale(.5f,SaddlebagAttachment.CHAIN_LENGTH,.5f);
        pose.translate(-.5,0,-.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(net.minecraft.world.level.block.Blocks.CHAIN.defaultBlockState(),pose,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        pose.popPose();
        // The vanilla lantern handle reaches one model pixel above its item center.
        renderItem(lantern,pose,buffers,light,horse,0,SaddlebagAttachment.CHAIN_LENGTH+SaddlebagAttachment.LANTERN_SCALE/16,0,SaddlebagAttachment.LANTERN_SCALE,ItemDisplayContext.NONE);
        pose.popPose();
    }
    private void renderItem(ItemStack item,PoseStack pose,MultiBufferSource buffers,int light,T horse,double x,double y,double z,float scale){
        renderItem(item,pose,buffers,light,horse,x,y,z,scale,ItemDisplayContext.FIXED);
    }
    private void renderItem(ItemStack item,PoseStack pose,MultiBufferSource buffers,int light,T horse,double x,double y,double z,float scale,ItemDisplayContext context){
        pose.pushPose();pose.translate(x,y,z);pose.scale(scale,-scale,-scale);Minecraft.getInstance().getItemRenderer().renderStatic(item,context,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffers,horse.level(),horse.getId());pose.popPose();
    }
}
