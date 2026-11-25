package com.fantasyfeudalism.items.client;
public final class LanternPendulumTest {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){
        var p=new LanternPendulum();p.step(0,0,0,0,0);
        p.step(.2,0,0,0,0);check(Math.abs(p.side(1))>1,"Acceleration must produce swing");
        float a=p.side(0),b=p.side(1);check(Math.abs(p.side(.5f)-(a+b)/2)<.0001,"Rendering interpolates without advancing physics");
        for(int i=0;i<400;i++)p.step(.2,0,0,0,0);
        check(Math.abs(p.side(1))<.001&&Math.abs(p.back(1))<.001,"Motion must damp to rest");
        p.step(.2,0,0,40,0);check(Math.abs(p.side(1))>.1,"Turning on the spot must move the lantern");
        for(int i=0;i<200;i++){p.step(.2+(i%2)*.8,(i%2)*.8,0,i*30,i);check(p.side(1)>=-12&&p.side(1)<=30&&p.back(1)>=-16&&p.back(1)<=32,"Extreme motion remains bounded");}
        p.step(100,0,0,0,0);check(p.side(1)==0&&p.back(1)==0,"Teleport resets old momentum");
        var jumping=new LanternPendulum();jumping.step(0,0,0,0,0);jumping.step(0,.5,0,0,0);
        check(jumping.lift(1)>0,"Jumping lifts the lantern on its tether");
        var falling=new LanternPendulum();falling.step(0,4,0,0,0);falling.step(0,3.5,0,0,0);
        check(falling.lift(1)>0,"Falling lifts the lantern relative to its mount");
        check(Math.abs(falling.lift(.5f)-(falling.lift(0)+falling.lift(1))/2)<.00001,"Vertical bounce is interpolated");
        for(int i=0;i<400;i++)falling.step(0,3.5,0,0,0);
        check(falling.lift(1)<.001,"Vertical bounce settles after landing");
        falling.step(0,100,0,0,0);check(falling.lift(1)==0,"Teleport resets vertical bounce");
        double rz=Math.toRadians(SaddlebagAttachment.OUTWARD_TILT),rx=Math.toRadians(SaddlebagAttachment.REARWARD_TILT);
        double half=3*SaddlebagAttachment.LANTERN_SCALE/16;
        double yy=SaddlebagAttachment.CHAIN_LENGTH+SaddlebagAttachment.LANTERN_SCALE/8;
        double rotatedY=Math.cos(rx)*yy+Math.sin(rx)*half;
        double contactX=SaddlebagAttachment.X-Math.cos(rz)*half+Math.sin(rz)*rotatedY;
        double contactY=SaddlebagAttachment.Y+Math.sin(rz)*half+Math.cos(rz)*rotatedY;
        double contactZ=SaddlebagAttachment.Z+Math.sin(rx)*yy-Math.cos(rx)*half;
        check(contactX<=.5&&contactX>=.49&&contactY>=-.5&&contactY<=0&&contactZ>=-.1875&&contactZ<=.3125,"Resting lantern touches the saddlebag at its rear corner without a floating gap");
        System.out.println("Lantern physics passed: acceleration, turning, damping, interpolation, bounds and teleport reset.");
    }
}
