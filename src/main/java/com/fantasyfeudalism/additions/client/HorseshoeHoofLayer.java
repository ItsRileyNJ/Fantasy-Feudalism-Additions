package com.fantasyfeudalism.additions.client;

import com.fantasyfeudalism.additions.client.mixin.HorseBodyAccessor;
import com.fantasyfeudalism.additions.mount.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;

/** Textured metal bands follow each animated hoof, with a bright rim and darker sole. */
public final class HorseshoeHoofLayer<T extends AbstractHorse,M extends HorseModel<T>> extends RenderLayer<T,M> {
    private static final ResourceLocation TEXTURE=ResourceLocation.withDefaultNamespace("textures/block/iron_block.png");
    private final ModelPart[] legs;
    private final ModelPart[][] hooves=new ModelPart[8][3];
    public HorseshoeHoofLayer(RenderLayerParent<T,M> parent){
        super(parent);var model=(HorseBodyAccessor)parent.getModel();
        legs=new ModelPart[]{model.feudalAdditions$rightHindLeg(),model.feudalAdditions$leftHindLeg(),model.feudalAdditions$rightFrontLeg(),model.feudalAdditions$leftFrontLeg(),model.feudalAdditions$rightHindBabyLeg(),model.feudalAdditions$leftHindBabyLeg(),model.feudalAdditions$rightFrontBabyLeg(),model.feudalAdditions$leftFrontBabyLeg()};
        var mesh=new MeshDefinition();
        for(int i=0;i<8;i++){
            float x=i%2==0?-1:-3,y=i<4?7.975f:13.475f,z=i%4<2?-1:-1.9f;
            // Three adjoining bands retain the existing silhouette. Only X/Z expand,
            // so the differently shaded vertical surfaces never overlap or flicker.
            var expand=new CubeDeformation(.015f,0,.015f);
            mesh.getRoot().addOrReplaceChild("hoof"+i+"_0",CubeListBuilder.create().texOffs(0,0).addBox(x,y,z,4,.3f,4,expand),PartPose.ZERO);
            mesh.getRoot().addOrReplaceChild("hoof"+i+"_1",CubeListBuilder.create().texOffs(0,4).addBox(x,y+.3f,z,4,1.43f,4,expand),PartPose.ZERO);
            mesh.getRoot().addOrReplaceChild("hoof"+i+"_2",CubeListBuilder.create().texOffs(0,10).addBox(x,y+1.73f,z,4,.3f,4,expand),PartPose.ZERO);
        }
        var root=LayerDefinition.create(mesh,16,16).bakeRoot();
        for(int i=0;i<8;i++)for(int band=0;band<3;band++)hooves[i][band]=root.getChild("hoof"+i+"_"+band);
    }
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,T horse,float swing,float amplitude,float partial,float age,float yaw,float pitch){
        var shoe=MountVisuals.shoe(horse);if(!(shoe.getItem() instanceof Horseshoe)||horse.isInvisible())return;
        int color=Horseshoe.itemColor(shoe,0),rim=shade(color,1.08f),sole=shade(color,.62f);pose.pushPose();
        if(horse.isBaby()){pose.scale(.5f,.5f,.5f);pose.translate(0,20f/16,0);}
        var vertices=buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for(int i=0;i<8;i++)if(legs[i].visible){
            pose.pushPose();legs[i].translateAndRotate(pose);
            if(horse instanceof Horse){
                // Shorten toward the hoof bottom, keeping the sole at its original
                // height rather than letting the scaled band float above the ground.
                float heightScale=.65f,bottom=i<4?10.005f:15.505f;
                pose.translate(0,bottom*(1-heightScale)/16,0);
                pose.scale(1,heightScale,1);
            }
            hooves[i][0].render(pose,vertices,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,rim);
            hooves[i][1].render(pose,vertices,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,color);
            hooves[i][2].render(pose,vertices,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,sole);
            pose.popPose();
        }
        pose.popPose();
    }
    private static int shade(int color,float brightness){
        int r=Math.min(255,Math.round((color>>16&255)*brightness));
        int g=Math.min(255,Math.round((color>>8&255)*brightness));
        int b=Math.min(255,Math.round((color&255)*brightness));
        return 0xFF000000|r<<16|g<<8|b;
    }
}
