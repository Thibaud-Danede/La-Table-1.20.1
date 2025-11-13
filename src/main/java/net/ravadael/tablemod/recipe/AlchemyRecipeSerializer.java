package net.ravadael.tablemod.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class AlchemyRecipeSerializer implements RecipeSerializer<AlchemyRecipe> {

    @Override
    public AlchemyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input = Ingredient.fromJson(json.get("ingredient"));
        Ingredient catalyst = Ingredient.fromJson(json.get("catalyst")); // <-- Add this
        ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

        return new AlchemyRecipe(recipeId, input, catalyst, output);

    }

    @Override
    public AlchemyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        Ingredient catalyst = Ingredient.fromNetwork(buffer); // <-- read second ingredient
        ItemStack result = buffer.readItem();
        return new AlchemyRecipe(recipeId, input, catalyst, result);
    }


    @Override
    public void toNetwork(FriendlyByteBuf buffer, AlchemyRecipe recipe) {
        recipe.getInput().toNetwork(buffer);      // <-- must match read order
        recipe.getCatalyst().toNetwork(buffer);   // <-- new line for catalyst
        buffer.writeItem(recipe.getResult());
    }

}
