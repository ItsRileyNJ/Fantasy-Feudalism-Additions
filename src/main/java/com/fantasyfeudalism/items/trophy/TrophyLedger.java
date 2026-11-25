package com.fantasyfeudalism.items.trophy;

import java.util.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Reservations for currently placed trophies survive death, saving and chunk unloading. */
public final class TrophyLedger extends SavedData {
    private final Map<UUID, EnumMap<TrophyKind,Integer>> placed = new HashMap<>();
    private final Map<UUID, TrophyKind> active = new HashMap<>();
    public static TrophyLedger get(ServerLevel level) { return level.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(TrophyLedger::new, TrophyLedger::load), "fantasy_feudalism_trophies"); }
    public boolean hasPlaced(UUID owner, TrophyKind kind) { return placed.containsKey(owner)&&placed.get(owner).getOrDefault(kind,0)>0; }
    public boolean recordPlacement(UUID owner, TrophyKind kind) {
        boolean added = placed.computeIfAbsent(owner, ignored -> new EnumMap<>(TrophyKind.class)).putIfAbsent(kind,1)==null; if (added) setDirty(); return added;
    }
    public void recordAdditionalPlacement(UUID owner,TrophyKind kind){
        placed.computeIfAbsent(owner,ignored->new EnumMap<>(TrophyKind.class)).merge(kind,1,Integer::sum);setDirty();
    }
    public boolean releasePlacement(UUID owner, TrophyKind kind) {
        var kinds = placed.get(owner);
        if (kinds == null || !kinds.containsKey(kind)) return false;
        int remaining=kinds.get(kind)-1;if(remaining>0)kinds.put(kind,remaining);else kinds.remove(kind);
        if (kinds.isEmpty()) placed.remove(owner);
        setDirty();
        return true;
    }
    public TrophyKind active(UUID owner) { return active.get(owner); }
    public void attune(UUID owner, TrophyKind kind) { active.put(owner, kind); setDirty(); }
    public void clear(UUID owner) { active.remove(owner); setDirty(); }
    public static TrophyLedger load(CompoundTag tag, HolderLookup.Provider registries) {
        var ledger = new TrophyLedger();
        for (var value : tag.getList("Players", Tag.TAG_COMPOUND)) {
            var player = (CompoundTag) value; if (!player.hasUUID("Owner")) continue;
            var owner = player.getUUID("Owner");
            for (var kind : TrophyKind.values()) {
                int count=player.contains("Count_"+kind.id())?Math.max(0,player.getInt("Count_"+kind.id())):player.getBoolean("Placed_"+kind.id())?1:0;
                if(count>0)ledger.placed.computeIfAbsent(owner,ignored->new EnumMap<>(TrophyKind.class)).put(kind,count);
            }
            if (player.contains("Active")) try { ledger.active.put(owner, TrophyKind.valueOf(player.getString("Active"))); } catch (IllegalArgumentException ignored) {}
        }
        return ledger;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        placed.forEach((owner, kinds) -> { var player = new CompoundTag(); player.putUUID("Owner", owner);
            kinds.forEach((kind,count)->{player.putBoolean("Placed_"+kind.id(),true);player.putInt("Count_"+kind.id(),count);});
            if (active.containsKey(owner)) player.putString("Active", active.get(owner).name()); list.add(player); });
        tag.put("Players", list); return tag;
    }
}
