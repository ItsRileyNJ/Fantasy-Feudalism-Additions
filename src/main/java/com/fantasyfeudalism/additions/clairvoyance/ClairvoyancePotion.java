package com.fantasyfeudalism.additions.clairvoyance;

import com.fantasyfeudalism.additions.FeudalAdditions;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

/** Resolve server-synced duration when used, rather than baking a config value into registry creation. */
public final class ClairvoyancePotion extends Potion {
    public ClairvoyancePotion() { super(new MobEffectInstance(FeudalAdditions.CLAIRVOYANCE, 2400)); }
    @Override
    public List<MobEffectInstance> getEffects() {
        return List.of(new MobEffectInstance(FeudalAdditions.CLAIRVOYANCE, ClairvoyanceConfig.durationTicks()));
    }
}
