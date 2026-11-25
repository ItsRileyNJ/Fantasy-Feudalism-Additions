package com.fantasyfeudalism.items.client;
public final class LanternPendulumTest {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){
        var p=new LanternPendulum();p.step(0,0,0,0,0);
        p.step(.2,0,0,0,0);check(Math.abs(p.side(1))>1,"Acceleration must produce swing");
        var response=new LanternPendulum();response.step(0,0,0,0,0);response.step(.05,0,0,0,0);
        check(Math.abs(response.side(1)-(-2*1.15*.86))<.00001,"Movement impulse is 15 percent stronger below safety limits");
        for(int yaw:new int[]{0,90,180,270}){
            double r=Math.toRadians(yaw),dx=-Math.sin(r)*.05,dz=Math.cos(r)*.05;
            var forward=new LanternPendulum();forward.step(0,0,0,yaw,0);forward.step(dx,0,dz,yaw,0);
            double expected=((.05*40+.05*.9)*1.15+.05*60*1.15*.12)*.86;
            check(Math.abs(forward.back(1)-expected)<.00001,"Forward acceleration drives rearward swing at yaw "+yaw);
            check(Math.abs(forward.side(1))<.00001,"Forward motion stays on the fore/aft axis at yaw "+yaw);
            for(int i=0;i<400;i++)forward.step(dx*(i+2),0,dz*(i+2),yaw,0);
            float cruising=forward.back(1);
            forward.step(dx*401,0,dz*401,yaw,0);
            check(forward.back(1)<cruising,"Braking produces the opposite impulse at yaw "+yaw);
        }
        for(int yaw:new int[]{0,90,180,270}){
            double r=Math.toRadians(yaw),x=0,z=0;
            var running=new LanternPendulum();running.step(x,0,z,yaw,0);
            for(int i=1;i<=400;i++){
                x-=Math.sin(r)*.2;z+=Math.cos(r)*.2;running.step(x,0,z,yaw,i*.7);
                if(i>100)check(running.back(1)>10,"Continuous running keeps a rearward trail throughout the gait at yaw "+yaw);
            }
            float fast=running.back(1);
            for(int i=1;i<=400;i++){
                x-=Math.sin(r)*.05;z+=Math.cos(r)*.05;running.step(x,0,z,yaw,i*.7);
            }
            check(running.back(1)>1&&running.back(1)<fast,"Slower forward movement reduces the trailing angle at yaw "+yaw);
            for(int i=0;i<400;i++)running.step(x,0,z,yaw,0);
            check(Math.abs(running.back(1))<.001,"Stopping returns the trailing lantern to rest at yaw "+yaw);
        }
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
        check(SaddlebagAttachment.X==6.5/16&&SaddlebagAttachment.Y==-6.5/16&&SaddlebagAttachment.Z==5.5/16,"Chain attaches to the middle of the brass peg");
        double rz=Math.toRadians(SaddlebagAttachment.OUTWARD_TILT),rx=Math.toRadians(SaddlebagAttachment.REARWARD_TILT);
        double half=3*SaddlebagAttachment.LANTERN_SCALE/16;
        double yy=SaddlebagAttachment.CHAIN_LENGTH+SaddlebagAttachment.LANTERN_SCALE/8;
        double rotatedY=Math.cos(rx)*yy+Math.sin(rx)*half;
        double contactX=SaddlebagAttachment.X-Math.cos(rz)*half+Math.sin(rz)*rotatedY;
        double contactY=SaddlebagAttachment.Y+Math.sin(rz)*half+Math.cos(rz)*rotatedY;
        double contactZ=SaddlebagAttachment.Z+Math.sin(rx)*yy-Math.cos(rx)*half;
        check(contactX<=.5&&contactX>=.3125&&contactY>=-.5&&contactY<=0&&contactZ>=.30&&contactZ<=.3125,"Resting lantern touches the saddlebag rear face without a floating gap");
        System.out.println("Lantern physics passed: acceleration, sustained forward trailing, braking, turning, damping, interpolation, bounds and teleport reset.");
    }
}
