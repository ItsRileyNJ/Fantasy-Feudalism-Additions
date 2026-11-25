package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.client.mixin.HorseBodyAccessor;
import com.fantasyfeudalism.items.mount.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

/** Small material-colored overlays follow each existing animated hoof, including foals. */
public final class HorseshoeHoofLayer<T extends AbstractHorse,M extends HorseModel<T>> extends RenderLayer<T,M> {
    private static final ResourceLocation TEXTURE=ResourceLocation.withDefaultNamespace("textures/block/white_concrete.png");
    private final ModelPart[] legs,hooves=new ModelPart[8];
    public HorseshoeHoofLayer(RenderLayerParent<T,M> parent){
        super(parent);var model=(HorseBodyAccessor)parent.getModel();
        legs=new ModelPart[]{model.feudalItems$rightHindLeg(),model.feudalItems$leftHindLeg(),model.feudalItems$rightFrontLeg(),model.feudalItems$leftFrontLeg(),model.feudalItems$rightHindBabyLeg(),model.feudalItems$leftHindBabyLeg(),model.feudalItems$rightFrontBabyLeg(),model.feudalItems$leftFrontBabyLeg()};
        var mesh=new MeshDefinition();
        for(int i=0;i<8;i++)mesh.getRoot().addOrReplaceChild("hoof"+i,CubeListBuilder.create().addBox(i%2==0?-1:-3,i<4?7.99f:13.49f,i%4<2?-1:-1.9f,4,2,4,new CubeDeformation(.015f)),PartPose.ZERO);
        var root=LayerDefinition.create(mesh,16,16).bakeRoot();for(int i=0;i<8;i++)hooves[i]=root.getChild("hoof"+i);
    }
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,T horse,float swing,float amplitude,float partial,float age,float yaw,float pitch){
        var shoe=MountVisuals.shoe(horse);if(!(shoe.getItem() instanceof Horseshoe)||horse.isInvisible())return;
        int color=Horseshoe.itemColor(shoe,0);pose.pushPose();
        if(horse.isBaby()){pose.scale(.5f,.5f,.5f);pose.translate(0,20f/16,0);}
        var vertices=buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for(int i=0;i<8;i++)if(legs[i].visible){pose.pushPose();legs[i].translateAndRotate(pose);hooves[i].render(pose,vertices,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,color);pose.popPose();}
        pose.popPose();
    }
}
