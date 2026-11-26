package com.fantasyfeudalism.additions.client;
import com.fantasyfeudalism.additions.FeudalAdditions;
import com.fantasyfeudalism.additions.mount.MountContent;
import com.fantasyfeudalism.additions.mount.MountVisuals;
import com.fantasyfeudalism.additions.sleep.SleepingBags;
import java.util.EnumMap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.*;
public final class MountLayer<T extends AbstractHorse,M extends HorseModel<T>> extends RenderLayer<T,M> {
    public static final net.minecraft.client.resources.model.ModelResourceLocation SADDLEBAG_MODEL=net.minecraft.client.resources.model.ModelResourceLocation.standalone(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("fantasy_feudalism_additions","item/saddlebag_equipped"));
    public static final ModelResourceLocation PACK_STORAGE_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(FeudalAdditions.ID, "item/pack_storage_equipped"));
    public static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(FeudalAdditions.ID, "item/spyglass_equipped"));
    private static final EnumMap<DyeColor, ModelResourceLocation> SLEEPING_BAG_MODELS = new EnumMap<>(DyeColor.class);
    static {
        for (var color : DyeColor.values()) {
            var location = ResourceLocation.fromNamespaceAndPath(FeudalAdditions.ID, "item/" + color.getName() + "_sleeping_bag_equipped");
            SLEEPING_BAG_MODELS.put(color, ModelResourceLocation.standalone(location));
        }
    }

    public static ModelResourceLocation sleepingBagModel(DyeColor color) {
        return SLEEPING_BAG_MODELS.get(color);
    }
    public MountLayer(RenderLayerParent<T,M> parent){super(parent);}
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,T horse,float swing,float amplitude,float partial,float age,float yaw,float pitch){
        if(horse instanceof net.minecraft.world.entity.animal.horse.Horse)return;
        var bag = MountVisuals.bag(horse);
        if (bag.isEmpty()) return;
        var lantern = MountVisuals.lantern(horse);
        pose.pushPose();((com.fantasyfeudalism.additions.client.mixin.HorseBodyAccessor)getParentModel()).feudalAdditions$body().translateAndRotate(pose);
        if(horse.isBaby())pose.scale(.5f,.5f,.5f);
        pose.pushPose();pose.scale(1,-1,-1);
        renderAddon(bag, SADDLEBAG_MODEL, pose, buffers, light);
        var pack = MountVisuals.packStorage(horse);
        if (pack.is(MountContent.PACK_STORAGE.get())) renderAddon(pack, PACK_STORAGE_MODEL, pose, buffers, light);
        var spyglass = MountVisuals.spyglass(horse);
        if (spyglass.is(Items.SPYGLASS)) renderAddon(spyglass, SPYGLASS_MODEL, pose, buffers, light);
        pose.popPose();
        var sleepingBag=com.fantasyfeudalism.additions.mount.MountVisuals.sleepingBag(horse);
        if (SleepingBags.isSleepingBag(sleepingBag)) {
            pose.pushPose();pose.scale(1,-1,-1);
            var model=Minecraft.getInstance().getModelManager().getModel(sleepingBagModel(SleepingBags.color(sleepingBag)));
            Minecraft.getInstance().getItemRenderer().render(sleepingBag,ItemDisplayContext.NONE,false,pose,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,model);
            pose.popPose();
        }
        if (!lantern.isEmpty()) renderHangingLantern(lantern, pose, buffers, light, horse, partial);
        pose.popPose();
    }
    private static void renderAddon(ItemStack item, ModelResourceLocation location, PoseStack pose, MultiBufferSource buffers, int light) {
        var client = Minecraft.getInstance();
        client.getItemRenderer().render(item, ItemDisplayContext.NONE, false, pose, buffers, light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, client.getModelManager().getModel(location));
    }

    private void renderHangingLantern(ItemStack lantern,PoseStack pose,MultiBufferSource buffers,int light,T horse,float partial){
        pose.pushPose();
        pose.translate(SaddlebagAttachment.X,SaddlebagAttachment.Y,SaddlebagAttachment.Z);
        // Entity-model +Y points down; +Z points toward the animal's rear.
        float outward=SaddlebagAttachment.OUTWARD_TILT+MountedLanternMotion.side(horse,partial);
        float rearward=SaddlebagAttachment.REARWARD_TILT+MountedLanternMotion.back(horse,partial);
        pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-Math.signum((float)SaddlebagAttachment.X)*outward));
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rearward));
        float chainLength=SaddlebagAttachment.CHAIN_LENGTH-MountedLanternMotion.lift(horse,partial);
        pose.pushPose();
        pose.scale(.5f,chainLength,.5f);
        pose.translate(-.5,0,-.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(net.minecraft.world.level.block.Blocks.CHAIN.defaultBlockState(),pose,buffers,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        pose.popPose();
        float scale=SaddlebagAttachment.LANTERN_SCALE;
        // The vanilla lantern handle reaches one model pixel above its item center.
        pose.pushPose();
        pose.translate(0, chainLength + scale / 16, 0);
        pose.scale(scale, -scale, -scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(lantern, ItemDisplayContext.NONE, light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, pose, buffers, horse.level(), horse.getId());
        pose.popPose();
        pose.popPose();
    }
}
