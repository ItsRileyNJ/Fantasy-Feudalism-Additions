package com.fantasyfeudalism.additions;

import com.fantasyfeudalism.additions.clairvoyance.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.*;

@Mod(FeudalAdditions.ID)
public final class FeudalAdditions {
    public static final String ID = "fantasy_feudalism_additions";
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ID);
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredHolder<MobEffect, MobEffect> CLAIRVOYANCE = EFFECTS.register("clairvoyance",
        () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xB3A1E6) {});
    public static final DeferredHolder<Potion, Potion> CLAIRVOYANCE_POTION = POTIONS.register("clairvoyance", ClairvoyancePotion::new);

    static {
        TABS.register("items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + ID))
            .icon(() -> potionStack(Items.POTION))
            .displayItems((parameters, output) -> {
                output.accept(potionStack(Items.POTION));
                output.accept(potionStack(Items.SPLASH_POTION));
                output.accept(potionStack(Items.LINGERING_POTION));
                output.accept(potionStack(Items.TIPPED_ARROW));
                com.fantasyfeudalism.additions.content.Content.creative(output::accept);
                output.accept(com.fantasyfeudalism.additions.content.AnimalAllegiance.NORMAL.get());
                output.accept(com.fantasyfeudalism.additions.content.AnimalAllegiance.EXTENDED.get());
            }).build());
    }

    public static ItemStack potionStack(Item item) {
        var stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(CLAIRVOYANCE_POTION));
        return stack;
    }

    public FeudalAdditions(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, ClairvoyanceConfig.SPEC);
        container.registerConfig(ModConfig.Type.SERVER, com.fantasyfeudalism.additions.lighting.WeatherLighting.SPEC, "fantasy_feudalism_additions-weather.toml");
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.lighting.WeatherLighting::chunkLoaded);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.lighting.LightInteractions::interact);
        com.fantasyfeudalism.additions.spawner.SpawnerEyes.init();
        com.fantasyfeudalism.additions.mount.MountContent.init();
        com.fantasyfeudalism.additions.sleep.SleepingBags.init();
        com.fantasyfeudalism.additions.mount.MountContent.MENUS.register(bus);
        com.fantasyfeudalism.additions.mount.SaddlebagLoot.REGISTRY.register(bus);
        com.fantasyfeudalism.additions.mount.MountRecipes.init();
        com.fantasyfeudalism.additions.content.ModAttachments.TYPES.register(bus);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.mount.MountEquipment::interact);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.mount.MountEquipment::drops);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.mount.MountEquipment::damage);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.mount.MountEquipment::tick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.mount.MountEquipment::join);
        com.fantasyfeudalism.additions.trophy.Trophies.init();
        com.fantasyfeudalism.additions.trophy.TrophyAttunement.REGISTRY.register(bus);
        com.fantasyfeudalism.additions.trophy.TrophyParticles.REGISTRY.register(bus);
        com.fantasyfeudalism.additions.lighting.TorchRefreshRecipe.init();
        com.fantasyfeudalism.additions.trophy.TrophyRecipes.REGISTRY.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, com.fantasyfeudalism.additions.trophy.HeadDrops.SPEC, "fantasy_feudalism_additions-head-drops.toml");
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.trophy.HeadDrops::drops);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.trophy.TrophyEffects::damage);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.trophy.TrophyEffects::tick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.trophy.TrophyEffects::xp);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.trophy.TrophyEffects::resistance);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.trophy.TrophyEffects::phantoms);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.trophy.TrophyEffects::clone);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.trophy.TrophyEffects::target);
        com.fantasyfeudalism.additions.content.AnimalAllegiance.register(bus);
        com.fantasyfeudalism.additions.content.Content.register(bus);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.content.AnimalAllegiance::brewing);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.content.AnimalAllegiance::attract);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.content.AnimalAllegiance::join);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.content.MilkCauldron::interact);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.content.MilkVial::tick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.content.VillagerTrades::register);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.compat.CaravanTrades::join);
        container.registerConfig(ModConfig.Type.SERVER, com.fantasyfeudalism.additions.lighting.LightFuel.SPEC, "fantasy_feudalism_additions-fuel.toml");
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.lighting.LightFuel::tick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.lighting.LightItemFuel::itemTick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.lighting.LightItemFuel::playerTick);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.lighting.LightItemFuel::containerOpened);
        NeoForge.EVENT_BUS.addListener(com.fantasyfeudalism.additions.lighting.LightItemFuel::drops);
        EFFECTS.register(bus); POTIONS.register(bus); TABS.register(bus);
        bus.addListener(FeudalAdditions::network);
        NeoForge.EVENT_BUS.addListener(FeudalAdditions::brewing);
        NeoForge.EVENT_BUS.addListener(ClairvoyanceTracker::tick);
    }

    private static void brewing(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(Potions.THICK, Items.AMETHYST_SHARD, CLAIRVOYANCE_POTION);
    }

    private static void network(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(ClairvoyancePayload.TYPE, ClairvoyancePayload.CODEC,
            (payload, context) -> com.fantasyfeudalism.additions.client.ClairvoyanceClient.receive(payload));
    }
}
