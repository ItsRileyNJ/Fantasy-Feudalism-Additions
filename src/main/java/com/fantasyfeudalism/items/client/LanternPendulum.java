package com.fantasyfeudalism.items.client;

/** Two damped angular springs, advanced once per game tick; angles are degrees. */
public final class LanternPendulum {
    private static final double MOVEMENT_RESPONSE=1.15;
    private double x,y,z,yaw,vx,vy,vz,side,back,sideSpeed,backSpeed,previousSide,previousBack,lift,liftSpeed,previousLift;
    private boolean initialized;
    public void step(double x,double y,double z,double yaw,double walk){
        double dx=x-this.x,dy=y-this.y,dz=z-this.z;
        if(!initialized||dx*dx+dy*dy+dz*dz>16){
            initialized=true;this.x=x;this.y=y;this.z=z;this.yaw=yaw;
            vx=vy=vz=side=back=sideSpeed=backSpeed=previousSide=previousBack=lift=liftSpeed=previousLift=0;return;
        }
        previousSide=side;previousBack=back;previousLift=lift;
        double ax=dx-vx,ay=dy-vy,az=dz-vz,r=Math.toRadians(yaw),speed=Math.hypot(dx,dz);
        double turn=((yaw-this.yaw+540)%360+360)%360-180;
        double sideForce=-(ax*Math.cos(r)+az*Math.sin(r))*40-turn*(.08+speed*.3)+Math.sin(walk*.8)*speed*.35;
        // Minecraft's forward axis is (-sin(yaw), 0, cos(yaw)). Project world
        // acceleration onto it so straight travel and braking work at every heading.
        double forwardAcceleration=-ax*Math.sin(r)+az*Math.cos(r);
        double backForce=forwardAcceleration*40-ay*5+Math.cos(walk*.8)*speed*.9;
        sideSpeed=(sideSpeed+clamp(sideForce*MOVEMENT_RESPONSE,-8,8)-side*.12)*.86;
        backSpeed=(backSpeed+clamp(backForce*MOVEMENT_RESPONSE,-8,8)-back*.12)*.86;
        double next=clamp(side+sideSpeed,-12,30);if(next!=side+sideSpeed)sideSpeed=0;side=next;
        next=clamp(back+backSpeed,-16,32);if(next!=back+backSpeed)backSpeed=0;back=next;
        // Vertical travel lets the lantern rise on its tether; landing adds a short bounce.
        double targetLift=clamp(Math.abs(dy)*.2*MOVEMENT_RESPONSE,0,.14);
        liftSpeed=(liftSpeed+(targetLift-lift)*.2+Math.min(Math.abs(ay),1)*.025*MOVEMENT_RESPONSE)*.75;
        double nextLift=clamp(lift+liftSpeed,0,.14);if(nextLift!=lift+liftSpeed)liftSpeed=0;lift=nextLift;
        this.x=x;this.y=y;this.z=z;this.yaw=yaw;vx=dx;vy=dy;vz=dz;
    }
    public float side(float partial){return (float)(previousSide+(side-previousSide)*clamp(partial,0,1));}
    public float lift(float partial){return (float)(previousLift+(lift-previousLift)*clamp(partial,0,1));}
    public float back(float partial){return (float)(previousBack+(back-previousBack)*clamp(partial,0,1));}
    private static double clamp(double value,double low,double high){return Math.max(low,Math.min(high,value));}
}
