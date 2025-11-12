package net.ravadael.tablemod.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ravadael.tablemod.TableMod;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TableMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<AlchemyRecipe>> ALCHEMY_SERIALIZER =
        SERIALIZERS.register("alchemy", AlchemyRecipeSerializer::new);

    public static void registerRecipeType() {
        RecipeType.register("tablemod:alchemy", AlchemyRecipeType.INSTANCE);
    }
}