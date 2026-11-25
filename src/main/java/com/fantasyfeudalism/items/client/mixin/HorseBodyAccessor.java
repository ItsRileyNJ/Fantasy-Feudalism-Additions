package com.fantasyfeudalism.items.client.mixin;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(HorseModel.class)
public interface HorseBodyAccessor { @Accessor("body") ModelPart feudalItems$body();
    @Accessor("rightHindLeg") ModelPart feudalItems$rightHindLeg();
    @Accessor("leftHindLeg") ModelPart feudalItems$leftHindLeg();
    @Accessor("rightFrontLeg") ModelPart feudalItems$rightFrontLeg();
    @Accessor("leftFrontLeg") ModelPart feudalItems$leftFrontLeg();
    @Accessor("rightHindBabyLeg") ModelPart feudalItems$rightHindBabyLeg();
    @Accessor("leftHindBabyLeg") ModelPart feudalItems$leftHindBabyLeg();
    @Accessor("rightFrontBabyLeg") ModelPart feudalItems$rightFrontBabyLeg();
    @Accessor("leftFrontBabyLeg") ModelPart feudalItems$leftFrontBabyLeg();
}
