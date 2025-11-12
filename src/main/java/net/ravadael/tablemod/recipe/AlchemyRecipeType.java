package net.ravadael.tablemod.recipe;

import net.minecraft.world.item.crafting.RecipeType;

public class AlchemyRecipeType {
    public static final RecipeType<AlchemyRecipe> INSTANCE = new RecipeType<>() {
        @Override
        public String toString() {
            return "tablemod:alchemy";
        }
    };
}