package net.ravadael.tablemod.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlchemyRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient input;
    private final Ingredient catalyst;
    private final boolean catalystRequired;
    private final List<ItemStack> results;
    /** Tag pour résultats (résolution paresseuse : les tags ne sont pas prêts pendant fromJson) */
    @javax.annotation.Nullable
    private final ResourceLocation resultsTag;

    /** Cache des résultats résolus depuis le tag (après chargement complet) */
    private volatile List<ItemStack> resolvedResults;

    public AlchemyRecipe(ResourceLocation id, Ingredient input, Ingredient catalyst, List<ItemStack> results) {
        this(id, input, catalyst, false, results, null);
    }

    public AlchemyRecipe(ResourceLocation id, Ingredient input, Ingredient catalyst, List<ItemStack> results,
                         @javax.annotation.Nullable ResourceLocation resultsTag) {
        this(id, input, catalyst, false, results, resultsTag);
    }

    public AlchemyRecipe(ResourceLocation id, Ingredient input, Ingredient catalyst, boolean catalystRequired,
                         List<ItemStack> results, @javax.annotation.Nullable ResourceLocation resultsTag) {
        this.id = id;
        this.input = input;
        this.catalyst = catalyst;
        this.catalystRequired = catalystRequired;
        this.results = results != null ? results : List.of();
        this.resultsTag = resultsTag;
        this.resolvedResults = null;
    }

    // === Getters used by your menu ===

    public Ingredient getInput() {
        return input;
    }

    public Ingredient getCatalyst() {
        return catalyst;
    }

    /** true = catalyseur obligatoire, false = pas de catalyseur requis (défaut) */
    public boolean isCatalystRequired() {
        return catalystRequired;
    }

    /** Retourne les résultats. Si resultsTag est défini, résolution paresseuse (tags non prêts pendant fromJson). */
    public List<ItemStack> getResults() {
        if (resultsTag != null) {
            if (resolvedResults == null) {
                resolvedResults = resolveResultsFromTag();
            }
            return resolvedResults != null ? resolvedResults : List.of();
        }
        return results;
    }

    /** Résout le tag après chargement complet (évite "Empty Tag" pendant la désérialisation) */
    private List<ItemStack> resolveResultsFromTag() {
        RegistryAccess ra = getRegistryAccess();
        if (ra == null) return List.of();

        var registry = ra.registry(Registries.ITEM);
        if (registry.isEmpty()) return List.of();

        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, resultsTag);
        List<ItemStack> list = new ArrayList<>();
        for (var holder : registry.get().getTagOrEmpty(tagKey)) {
            list.add(new ItemStack(holder.value()));
        }
        return Collections.unmodifiableList(list);
    }

    /** Obtient RegistryAccess (serveur ou client) */
    @javax.annotation.Nullable
    private static RegistryAccess getRegistryAccess() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) return mc.level.registryAccess();
        }
        var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.registryAccess() : null;
    }

    // === Vanilla-required methods ===

    /** Vérifie si l'input seul matche (pour afficher les recettes possibles même sans catalyseur) */
    public boolean matchesInputOnly(Container container) {
        return input.test(container.getItem(0));
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack inputStack = container.getItem(0);
        ItemStack catalystStack = container.getItem(1);

        // Vérifie l'input
        if (!input.test(inputStack)) {
            return false;
        }

        // Pas de catalyseur requis (catalyst_required: false)
        if (!catalystRequired) {
            return true;
        }

        // Catalyseur requis → vérifier qu'il matche
        return catalyst.test(catalystStack);
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
        List<ItemStack> list = getResults();
        return list.isEmpty() ? ItemStack.EMPTY : list.get(0);
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
        List<ItemStack> list = getResults();
        if (input.isEmpty()) return list;

        // Si l'input n'est pas dans les résultats, renvoie la liste intacte (zéro allocation)
        boolean contains = false;
        for (ItemStack r : list) {
            if (ItemStack.isSameItemSameTags(r, input)) {
                contains = true;
                break;
            }
        }
        if (!contains) return list;

        // Sinon construire une liste filtrée
        return list.stream()
                .filter(r -> !ItemStack.isSameItemSameTags(r, input))
                .toList();
    }
}
