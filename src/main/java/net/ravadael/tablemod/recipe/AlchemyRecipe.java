package net.ravadael.tablemod.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public class AlchemyRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient input;
    private final Ingredient catalyst;
    private final List<ItemStack> results; // <<<<<< NEW

    public AlchemyRecipe(ResourceLocation id, Ingredient input, Ingredient catalyst, List<ItemStack> results) {
        this.id = id;
        this.input = input;
        this.catalyst = catalyst;
        this.results = results;
    }

    // === Getters used by your menu ===

    public Ingredient getInput() {
        return input;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    public List<ItemStack> getResults() {       // <<<<<< NEW
        return results;
    }

    // === Vanilla-required methods ===

    @Override
    public boolean matches(Container container, net.minecraft.world.level.Level level) {
        return input.test(container.getItem(0)) &&
                catalyst.test(container.getItem(1));
    }

    @Override
    public ItemStack assemble(Container pContainer, RegistryAccess registryAccess) {
        return ItemStack.EMPTY; // You manually handle output selection
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return results.isEmpty() ? ItemStack.EMPTY : results.get(0);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALCHEMY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ALCHEMY_RECIPE_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true; // prevents recipe book integration problems
    }

    public List<ItemStack> getFilteredResults(ItemStack input) {
        if (input.isEmpty()) return results;

        // Si l'input n'est pas dans les résultats, renvoie la liste intacte (zéro allocation)
        boolean contains = false;
        for (ItemStack r : results) {
            if (ItemStack.isSameItemSameTags(r, input)) {
                contains = true;
                break;
            }
        }
        if (!contains) return results;

        // Sinon construire une liste filtrée
        return results.stream()
                .filter(r -> !ItemStack.isSameItemSameTags(r, input))
                .toList();
    }
}
