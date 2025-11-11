
package net.ravadael.tablemod.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.RegistryAccess;
import net.ravadael.tablemod.TableMod;

import java.util.ArrayList;
import java.util.List;

public class AlchemyRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final Ingredient catalyst;
    private final List<ItemStack> dynamicResults;
    private final boolean isDynamic;

    public AlchemyRecipe(ResourceLocation id, Ingredient input, Ingredient catalyst, List<ItemStack> dynamicResults, boolean isDynamic) {
        this.id = id;
        this.input = input;
        this.catalyst = catalyst;
        this.dynamicResults = dynamicResults;
        this.isDynamic = isDynamic;
    }

    public boolean matchesStacks(ItemStack inputStack, ItemStack catalystStack) {
        return input.test(inputStack) && catalyst.test(catalystStack);
    }

    public List<ItemStack> getDynamicResults(ItemStack inputStack, Level level) {
        if (!isDynamic || !input.test(inputStack)) return List.of();

        List<ItemStack> results = new ArrayList<>();
        var tag = level.registryAccess()
                .registryOrThrow(Registries.ITEM)
                .getTagOrEmpty(ItemTags.PLANKS);

        tag.forEach(holder -> {
            Item item = holder.value();
            if (!ItemStack.isSameItemSameTags(new ItemStack(item), inputStack)) {
                results.add(new ItemStack(item));
            }
        });

        return results;
    }

    public Ingredient getInput() {
        return input;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 2) return false;
        return matchesStacks(container.getItem(0), container.getItem(1));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return dynamicResults.isEmpty() ? ItemStack.EMPTY : dynamicResults.get(0);
    }

    public List<ItemStack> getAllResults() {
        return dynamicResults;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ALCHEMY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ALCHEMY_TYPE.get();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public static class Serializer implements RecipeSerializer<AlchemyRecipe> {

        @Override
        public AlchemyRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));


            Ingredient catalyst = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "catalyst"));
            boolean dynamic = GsonHelper.getAsBoolean(json, "dynamic_outputs", false);

            return new AlchemyRecipe(id, input, catalyst, new ArrayList<>(), dynamic);
        }

        @Override
        public AlchemyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            Ingredient catalyst = Ingredient.fromNetwork(buf);
            boolean dynamic = buf.readBoolean();

            int size = buf.readVarInt();
            List<ItemStack> results = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                results.add(buf.readItem());
            }

            return new AlchemyRecipe(id, input, catalyst, results, dynamic);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AlchemyRecipe recipe) {
            recipe.input.toNetwork(buf);
            recipe.catalyst.toNetwork(buf);
            buf.writeBoolean(recipe.isDynamic);

            buf.writeVarInt(recipe.dynamicResults.size());
            for (ItemStack stack : recipe.dynamicResults) {
                buf.writeItem(stack);
            }
        }
    }
}
