package com.fantasyfeudalism.additions.trophy;
import com.fantasyfeudalism.additions.FeudalAdditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.*;
public final class TrophyRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(Registries.RECIPE_SERIALIZER, FeudalAdditions.ID);
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<TrophyRecipe>> SERIALIZER = REGISTRY.register("trophy", () -> new SimpleCraftingRecipeSerializer<>(TrophyRecipe::new));
}
