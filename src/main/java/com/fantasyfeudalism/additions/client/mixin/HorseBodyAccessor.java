package com.fantasyfeudalism.additions.client.mixin;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(HorseModel.class)
public interface HorseBodyAccessor { @Accessor("body") ModelPart feudalAdditions$body();
    @Accessor("rightHindLeg") ModelPart feudalAdditions$rightHindLeg();
    @Accessor("leftHindLeg") ModelPart feudalAdditions$leftHindLeg();
    @Accessor("rightFrontLeg") ModelPart feudalAdditions$rightFrontLeg();
    @Accessor("leftFrontLeg") ModelPart feudalAdditions$leftFrontLeg();
    @Accessor("rightHindBabyLeg") ModelPart feudalAdditions$rightHindBabyLeg();
    @Accessor("leftHindBabyLeg") ModelPart feudalAdditions$leftHindBabyLeg();
    @Accessor("rightFrontBabyLeg") ModelPart feudalAdditions$rightFrontBabyLeg();
    @Accessor("leftFrontBabyLeg") ModelPart feudalAdditions$leftFrontBabyLeg();
}
