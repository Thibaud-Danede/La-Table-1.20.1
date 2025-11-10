// net.ravadael.tablemod.recipe.ModRecipeTypes
package net.ravadael.tablemod.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ravadael.tablemod.TableMod;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TableMod.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, TableMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<TransmuteRecipe>> TRANSMUTE_SERIALIZER =
            SERIALIZERS.register("transmute", TransmuteRecipe.Serializer::new);

    public static final RegistryObject<RecipeType<TransmuteRecipe>> TRANSMUTE_TYPE =
            TYPES.register("transmute", () -> new RecipeType<>() {
                public String toString() { return TableMod.MOD_ID + ":transmute"; }
            });
}
