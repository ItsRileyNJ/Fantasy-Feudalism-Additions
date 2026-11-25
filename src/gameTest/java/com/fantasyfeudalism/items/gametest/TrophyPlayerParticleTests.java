package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.trophy.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class TrophyPlayerParticleTests {
    private record Observer(ServerPlayer player,List<ClientboundLevelParticlesPacket> packets){}
    private static Observer observer(GameTestHelper h){
        var packets=new ArrayList<ClientboundLevelParticlesPacket>();
        var profile=new com.mojang.authlib.GameProfile(UUID.randomUUID(),"particle-test");
        var player=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,ClientInformation.createDefault());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,new net.minecraft.server.network.CommonListenerCookie(profile,0,ClientInformation.createDefault(),false,net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE)){
            @Override public void send(Packet<?> packet){
                if(packet instanceof ClientboundLevelParticlesPacket particles)packets.add(particles);
                super.send(packet);
            }
        };
        player.setGameMode(GameType.CREATIVE);player.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(3,2,3))));h.getLevel().addNewPlayer(player);
        return new Observer(player,packets);
    }
    private static void select(ServerPlayer player,TrophyKind kind){
        var ledger=TrophyLedger.get(player.serverLevel());ledger.recordPlacement(player.getUUID(),kind);ledger.attune(player.getUUID(),kind);TrophyEffects.changed(player);
    }
    private static void tick(ServerPlayer player,int tick){player.tickCount=tick;TrophyEffects.tick(new PlayerTickEvent.Post(player));}
    private static void remove(GameTestHelper h,Observer observer){h.getLevel().removePlayerImmediately(observer.player(),Entity.RemovalReason.DISCARDED);}
    @GameTest(template="empty")
    public static void every_attuned_player_emits_matching_particles_at_twice_the_old_enderman_rate(GameTestHelper h){
        var actor=observer(h);var nearby=observer(h);
        try{
            for(var kind:TrophyKind.values()){
                select(actor.player(),kind);actor.packets().clear();nearby.packets().clear();
                for(int i=1;i<=40;i++)tick(actor.player(),i);
                for(var viewer:List.of(actor,nearby)){
                    h.assertTrue(viewer.packets().size()==2,"Each trophy sends one particle batch per second, including to its owner");
                    for(var packet:viewer.packets()){
                        h.assertTrue(packet.getCount()==2,"Every trophy emits two particles per second, twice the old Enderman amount");
                        h.assertTrue(packet.getX()==actor.player().getX()&&packet.getY()==actor.player().getY()+1&&packet.getZ()==actor.player().getZ(),"Aura follows the attuned player");
                        h.assertTrue(packet.getXDist()==.2f&&packet.getYDist()==.3f&&packet.getZDist()==.2f&&packet.getMaxSpeed()==.01f,"All player auras use the Enderman effect's spread and speed");
                        ParticleType<?> expected=switch(kind){case WITHER_SKELETON->ParticleTypes.SMOKE;case STRAY->ParticleTypes.SNOWFLAKE;case ENDER_DRAGON->ParticleTypes.PORTAL;case PLAYER->ParticleTypes.ENCHANT;default->ParticleTypes.DUST;};
                        h.assertTrue(packet.getParticle().getType()==expected,"Player particle type matches "+kind);
                        if(packet.getParticle() instanceof DustParticleOptions dust)h.assertTrue(dust.getColor().equals(Vec3.fromRGB24(TrophyParticles.color(kind)&0xFFFFFF).toVector3f()),"Dust uses the selected trophy's exact aura color");
                        h.assertFalse(packet.isOverrideLimiter(),"Auras retain normal client particle limits");
                    }
                }
            }
            nearby.player().setPos(actor.player().position().add(64,0,0));nearby.packets().clear();tick(actor.player(),60);
            h.assertTrue(nearby.packets().isEmpty(),"Aura packets retain the normal nearby-player range");
        }finally{remove(h,actor);remove(h,nearby);}h.succeed();
    }
    @GameTest(template="empty")
    public static void player_particles_switch_with_attunement_and_stop_when_attunement_ends(GameTestHelper h){
        var actor=observer(h);var player=actor.player();
        try{
            select(player,TrophyKind.ZOMBIE);actor.packets().clear();tick(player,20);
            h.assertTrue(actor.packets().size()==1&&actor.packets().getFirst().getParticle() instanceof DustParticleOptions,"Zombie attunement creates its own dust aura");
            select(player,TrophyKind.ENDER_DRAGON);actor.packets().clear();tick(player,40);
            h.assertTrue(actor.packets().size()==1&&actor.packets().getFirst().getParticle().getType()==ParticleTypes.PORTAL&&actor.packets().getFirst().getCount()==2,"Switching emits only the new doubled Enderman aura");
            TrophyLedger.get(h.getLevel()).clear(player.getUUID());TrophyEffects.changed(player);actor.packets().clear();tick(player,60);
            h.assertTrue(actor.packets().isEmpty(),"Clearing attunement stops particle emission");
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(TrophyAttunement.EFFECTS.get(TrophyKind.ZOMBIE),200));tick(player,80);
            h.assertTrue(actor.packets().isEmpty(),"A trophy status icon alone cannot create an attunement aura");
            select(player,TrophyKind.CREEPER);TrophyEffects.clone(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(player,player,true));actor.packets().clear();tick(player,100);
            h.assertTrue(actor.packets().isEmpty(),"Death clears the aura along with attunement");
        }finally{remove(h,actor);}h.succeed();
    }
}
