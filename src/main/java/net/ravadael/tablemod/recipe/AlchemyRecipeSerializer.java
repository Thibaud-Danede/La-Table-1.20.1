
package net.ravadael.tablemod.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AlchemyRecipeSerializer implements RecipeSerializer<AlchemyRecipe> {

    @Override
    public AlchemyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
        Ingredient catalyst = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "catalyst"));

        List<ItemStack> results = new ArrayList<>();

        if (json.has("results")) {
            JsonArray resultArray = GsonHelper.getAsJsonArray(json, "results");
            for (JsonElement element : resultArray) {
                results.add(ShapedRecipe.itemStackFromJson(GsonHelper.convertToJsonObject(element, "result entry")));
            }
        } else if (json.has("result")) {
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            results.add(result);
        } else {
            throw new JsonParseException("Alchemy recipe must have either 'result' or 'results'");
        }

        return new AlchemyRecipe(recipeId, input, catalyst, results);
    }

    @Override
    public @Nullable AlchemyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient input = Ingredient.fromNetwork(buffer);
        Ingredient catalyst = Ingredient.fromNetwork(buffer);

        int count = buffer.readInt();
        List<ItemStack> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            results.add(buffer.readItem());
        }

        return new AlchemyRecipe(recipeId, input, catalyst, results);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AlchemyRecipe recipe) {
        recipe.getInput().toNetwork(buffer);
        recipe.getCatalyst().toNetwork(buffer);

        buffer.writeInt(recipe.getResults().size());
        for (ItemStack result : recipe.getResults()) {
            buffer.writeItem(result);
        }
    }
}
