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

    // --------------------------------------------------------
    // JSON → Recipe
    // --------------------------------------------------------
    @Override
    public AlchemyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));

        // Catalyst OPTIONNELLE
        Ingredient catalyst =
                json.has("catalyst")
                        ? Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "catalyst"))
                        : Ingredient.EMPTY;

        // catalyst_required : true = catalyseur obligatoire, false = pas besoin (défaut)
        boolean catalystRequired = GsonHelper.getAsBoolean(json, "catalyst_required", false);

        // Results (liste explicite OU tag pour compatibilité mods)
        List<ItemStack> results = new ArrayList<>();

        if (json.has("results_tag")) {
            // Compatibilité mods : NE PAS résoudre ici — les tags ne sont pas prêts pendant fromJson.
            // La résolution se fait à la première utilisation (voir AlchemyRecipe.getResults).
            String tagId = GsonHelper.getAsString(json, "results_tag");
            ResourceLocation tagLoc = ResourceLocation.tryParse(tagId);
            if (tagLoc != null) {
                return new AlchemyRecipe(recipeId, input, catalyst, catalystRequired, List.of(), tagLoc);
            }
            throw new JsonParseException("Invalid results_tag: " + tagId);
        }
        else if (json.has("results")) {
            JsonArray arr = GsonHelper.getAsJsonArray(json, "results");
            for (JsonElement e : arr) {
                results.add(
                        ShapedRecipe.itemStackFromJson(GsonHelper.convertToJsonObject(e, "result entry"))
                );
            }
        }
        else if (json.has("result")) {
            results.add(
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"))
            );
        }
        else {
            throw new JsonParseException("Alchemy recipe must have 'result', 'results', or 'results_tag'");
        }

        return new AlchemyRecipe(recipeId, input, catalyst, catalystRequired, results, null);
    }

    // --------------------------------------------------------
    // Network → Recipe
    // --------------------------------------------------------
    @Override
    public @Nullable AlchemyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {

        Ingredient input = Ingredient.fromNetwork(buf);

        boolean hasCatalyst = buf.readBoolean();
        Ingredient catalyst = hasCatalyst ? Ingredient.fromNetwork(buf) : Ingredient.EMPTY;
        boolean catalystRequired = buf.readBoolean();

        int count = buf.readInt();
        List<ItemStack> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            results.add(buf.readItem());
        }

        return new AlchemyRecipe(recipeId, input, catalyst, catalystRequired, results, null);
    }

    // --------------------------------------------------------
    // Recipe → Network
    // --------------------------------------------------------
    @Override
    public void toNetwork(FriendlyByteBuf buf, AlchemyRecipe recipe) {

        recipe.getInput().toNetwork(buf);

        // Catalyst optionnelle → un bool + éventuellement l'Ingredient
        boolean hasCatalyst = recipe.getCatalyst() != Ingredient.EMPTY
                && recipe.getCatalyst().getItems().length > 0;

        buf.writeBoolean(hasCatalyst);
        if (hasCatalyst)
            recipe.getCatalyst().toNetwork(buf);
        buf.writeBoolean(recipe.isCatalystRequired());

        buf.writeInt(recipe.getResults().size());
        for (ItemStack out : recipe.getResults()) {
            buf.writeItem(out);
        }
    }
}
