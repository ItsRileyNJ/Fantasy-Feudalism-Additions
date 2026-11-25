package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.mount.MountVisuals;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.animal.horse.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Visual-only simulation, independent of frame rate and inventory preview renders. */
@EventBusSubscriber(modid=FeudalItems.ID,value=Dist.CLIENT)
public final class MountedLanternMotion {
    private static final WeakHashMap<AbstractHorse,LanternPendulum> MOTION=new WeakHashMap<>();
    private static ClientLevel level;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var client=Minecraft.getInstance();
        if(level!=client.level){MOTION.clear();level=client.level;}
        if(level==null||client.isPaused())return;
        for(var entity:level.entitiesForRendering())if(entity instanceof Donkey||entity instanceof Mule){
            var mount=(AbstractHorse)entity;
            if(MountVisuals.bag(mount).isEmpty()||MountVisuals.lantern(mount).isEmpty()){MOTION.remove(mount);continue;}
            MOTION.computeIfAbsent(mount,ignored->new LanternPendulum()).step(mount.getX(),mount.getY(),mount.getZ(),mount.yBodyRot,mount.walkAnimation.position());
        }
    }
    public static float side(AbstractHorse mount,float partial){var state=MOTION.get(mount);return state==null?0:state.side(partial);}
    public static float lift(AbstractHorse mount,float partial){var state=MOTION.get(mount);return state==null?0:state.lift(partial);}
    public static float back(AbstractHorse mount,float partial){var state=MOTION.get(mount);return state==null?0:state.back(partial);}
    private MountedLanternMotion(){}
}
