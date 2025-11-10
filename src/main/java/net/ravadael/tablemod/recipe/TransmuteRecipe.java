// net.ravadael.tablemod.recipe.TransmuteRecipe
package net.ravadael.tablemod.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.ravadael.tablemod.TableMod;

public class TransmuteRecipe implements Recipe<Container> {
    public static final String TYPE_ID = TableMod.MOD_ID + ":transmute";

    private final ResourceLocation id;
    private final Ingredient input;
    private final Ingredient catalyst;
    private final ItemStack result;
    private final int time;
    private final float xp;

    public TransmuteRecipe(ResourceLocation id, Ingredient input, Ingredient catalyst, ItemStack result, int time, float xp) {
        this.id = id; this.input = input; this.catalyst = catalyst; this.result = result; this.time = time; this.xp = xp;
    }

    // On match sur slots 0 (input) et 1 (catalyseur)
    @Override public boolean matches(Container inv, Level level) {
        return input.test(inv.getItem(0)) && catalyst.test(inv.getItem(1));
    }

    @Override public ItemStack assemble(Container inv, net.minecraft.core.RegistryAccess access) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public ItemStack getResultItem(net.minecraft.core.RegistryAccess access) { return result; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeTypes.TRANSMUTE_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.TRANSMUTE_TYPE.get(); }

    public int getTime() { return time; }
    public float getXp() { return xp; }

    // ------ Serializer ------
    public static class Serializer implements RecipeSerializer<TransmuteRecipe> {
        @Override public TransmuteRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            Ingredient catalyst = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "catalyst"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int time = GsonHelper.getAsInt(json, "time", 200);
            float xp = GsonHelper.getAsFloat(json, "experience", 0.0f);
            return new TransmuteRecipe(id, input, catalyst, result, time, xp);
        }
        @Override public TransmuteRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            Ingredient catalyst = Ingredient.fromNetwork(buf);
            ItemStack result = buf.readItem();
            int time = buf.readVarInt();
            float xp = buf.readFloat();
            return new TransmuteRecipe(id, input, catalyst, result, time, xp);
        }
        @Override public void toNetwork(FriendlyByteBuf buf, TransmuteRecipe r) {
            r.input.toNetwork(buf);
            r.catalyst.toNetwork(buf);
            buf.writeItem(r.result);
            buf.writeVarInt(r.time);
            buf.writeFloat(r.xp);
        }
    }
}
