package net.ravadael.tablemod.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.ravadael.tablemod.recipe.AlchemyRecipe;
import net.ravadael.tablemod.recipe.AlchemyRecipeType;

import java.util.List;

public class AlchemyTableMenu extends AbstractContainerMenu {
    private final SimpleContainer input = new SimpleContainer(1);
    private final SimpleContainer result = new SimpleContainer(1);
    private final ContainerLevelAccess access;
    private final Level level;
    private List<RecipeHolder<AlchemyRecipe>> recipes;
    private int selectedRecipeIndex = -1;

    public AlchemyTableMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level(), buf.readBlockPos());
    }

    public AlchemyTableMenu(int id, Inventory inv, Level level, BlockPos pos) {
        super(ModMenuTypes.ALCHEMY_TABLE_MENU.get(), id);
        this.level = level;
        this.access = ContainerLevelAccess.create(level, pos);

        this.addSlot(new Slot(input, 0, 20, 33) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(input);
            }
        });

        this.addSlot(new Slot(result, 0, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                input.getItem(0).shrink(1);
                slotsChanged(input);
                super.onTake(player, stack);
            }
        });

        // Player inventory slots
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int k = 0; k < 9; ++k)
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));

        this.recipes = level.getRecipeManager().getAllRecipesFor(AlchemyRecipeType.INSTANCE);
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        if (container == this.input) {
            updateRecipes();
        }
    }

    private void updateRecipes() {
        this.recipes = level.getRecipeManager()
            .getAllRecipesFor(AlchemyRecipeType.INSTANCE)
            .stream()
            .filter(recipe -> recipe.value().matches(input, level))
            .toList();

        if (!recipes.isEmpty()) {
            selectedRecipeIndex = 0;
            result.setItem(0, recipes.get(0).value().getResultItem().copy());
        } else {
            selectedRecipeIndex = -1;
            result.setItem(0, ItemStack.EMPTY);
        }

        broadcastChanges();
    }

    public void setSelectedRecipeIndex(int index) {
        if (index >= 0 && index < recipes.size()) {
            selectedRecipeIndex = index;
            result.setItem(0, recipes.get(index).value().getResultItem().copy());
            broadcastChanges();
        }
    }

    public List<RecipeHolder<AlchemyRecipe>> getCurrentRecipes() {
        return recipes;
    }

    public int getSelectedRecipeIndex() {
        return selectedRecipeIndex;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, pos) -> 
            player.distanceToSqr(pos.getCenter()) < 64.0D, true);
    }
}