package com.fantasyfeudalism.items.trophy;

import com.fantasyfeudalism.items.FeudalItems;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.*;

public final class TrophyParticles {
    /** The old Enderman player effect emitted one particle per second. */
    public static final int PLAYER_COUNT=2;
    private static final java.util.Map<TrophyKind,ParticleOptions> PLAYER_OPTIONS=playerOptions();
    public record Profile(int interval,int count,double rise,double jitter,int lifetime,float size) {}
    public static final Profile IDLE_PROFILE=new Profile(20,1,.012,.001,70,.045f);
    public static final Profile ACTIVE_PROFILE=new Profile(2,6,.05,.015,36,.065f);
    public static final DeferredRegister<ParticleType<?>> REGISTRY=DeferredRegister.create(Registries.PARTICLE_TYPE,FeudalItems.ID);
    public static final DeferredHolder<ParticleType<?>,ParticleType<ColorParticleOption>> IDLE=REGISTRY.register("trophy_aura",TrophyParticles::type);
    public static final DeferredHolder<ParticleType<?>,ParticleType<ColorParticleOption>> ACTIVE=REGISTRY.register("active_trophy_aura",TrophyParticles::type);
    private static ParticleType<ColorParticleOption> type(){
        return new ParticleType<>(false){
            @Override public MapCodec<ColorParticleOption> codec(){return ColorParticleOption.codec(this);}
            @Override public StreamCodec<? super ByteBuf,ColorParticleOption> streamCodec(){return ColorParticleOption.streamCodec(this);}
        };
    }
    public static int color(TrophyKind kind){return switch(kind){
        case ZOMBIE->0xFFCC3333;case SKELETON->0xFFD9D9D9;case CREEPER->0xFF19CC19;
        case WITHER_SKELETON->0xFF777077;case STRAY->0xFFB4E8FF;case BOGGED->0xFF66B31A;
        case PIGLIN->0xFFFFBF19;case ENDER_DRAGON->0xFFAA44EE;case PLAYER->0xFF8BBAFF;
    };}
    private static java.util.Map<TrophyKind,ParticleOptions> playerOptions(){
        var options=new java.util.EnumMap<TrophyKind,ParticleOptions>(TrophyKind.class);
        for(var kind:TrophyKind.values())options.put(kind,switch(kind){
            case WITHER_SKELETON->ParticleTypes.SMOKE;
            case STRAY->ParticleTypes.SNOWFLAKE;
            case ENDER_DRAGON->ParticleTypes.PORTAL;
            case PLAYER->ParticleTypes.ENCHANT;
            default->new DustParticleOptions(net.minecraft.world.phys.Vec3.fromRGB24(color(kind)&0xFFFFFF).toVector3f(),.65f);
        });
        return options;
    }
    public static ParticleOptions playerOption(TrophyKind kind){return PLAYER_OPTIONS.get(kind);}
    public static void playerAura(net.minecraft.server.level.ServerPlayer player,TrophyKind kind){
        player.serverLevel().sendParticles(playerOption(kind),player.getX(),player.getY()+1,player.getZ(),PLAYER_COUNT,.2,.3,.2,.01);
    }
    public static double upwardVelocity(Profile profile,double supplied){return profile.rise()+Math.abs(supplied);}
    private TrophyParticles() {}
}
