package com.fantasyfeudalism.additions.mount;
import net.minecraft.world.item.crafting.*;
public final class MountRecipes {
    public static final java.util.function.Supplier<RecipeSerializer<NetheriteHorseshoeRecipe>> SMITHING=com.fantasyfeudalism.additions.trophy.TrophyRecipes.REGISTRY.register("netherite_horseshoe",NetheriteHorseshoeRecipe.Serializer::new);
    public static void init(){}
}
