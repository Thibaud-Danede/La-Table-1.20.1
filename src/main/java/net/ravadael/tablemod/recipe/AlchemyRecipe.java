package net.ravadael.tablemod.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class AlchemyRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final Ingredient catalyst;
    private final ItemStack output;

    public AlchemyRecipe(ResourceLocation id, Ingredient input, Ingredient catalyst, ItemStack output) {
        this.id = id;
        this.input = input;
        this.catalyst = catalyst;
        this.output = output;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(0)) && catalyst.test(container.getItem(1));
    }


    @Override
    public ItemStack assemble(Container container, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ALCHEMY_RECIPE_TYPE.get(); // or whatever your correct registered object is
    }


    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALCHEMY_SERIALIZER.get();
    }

    public ItemStack getResult() {
        return this.output;
    }

    public Ingredient getInput() {
        return input;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }
}