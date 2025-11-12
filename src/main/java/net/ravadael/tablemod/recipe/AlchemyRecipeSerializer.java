package net.ravadael.tablemod.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class AlchemyRecipeSerializer implements RecipeSerializer<AlchemyRecipe> {
    @Override
    public AlchemyRecipe fromJson(ResourceLocation id, JsonObject json) {
        Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
        ItemStack result = ItemStack.CODEC.parse(JsonObject.create(GsonHelper.getAsJsonObject(json, "result"))).result().orElse(ItemStack.EMPTY);
        return new AlchemyRecipe(id, ingredient, result);
    }

    @Override
    public AlchemyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();
        return new AlchemyRecipe(id, ingredient, result);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AlchemyRecipe recipe) {
        recipe.getIngredients().get(0).toNetwork(buffer);
        buffer.writeItem(recipe.getResultItem());
    }
}