
package net.ravadael.tablemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.ravadael.tablemod.menu.AlchemyTableMenu;
import net.ravadael.tablemod.recipe.AlchemyRecipe;
import net.ravadael.tablemod.recipe.ModRecipeTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlchemyTableBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == 0 || slot == 1) {
                updateMatchingRecipes();
            }
        }
    };

    private LazyOptional<IItemHandler> lazyHandler = LazyOptional.of(() -> itemHandler);

    public AlchemyTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_TABLE_BE.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }


    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyHandler.invalidate();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AlchemyTableBlockEntity be) {
        // Not used anymore (tickless logic)
    }

    public Component getDisplayName() {
        return Component.literal("Alchemy Table");
    }

    @Nullable
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new AlchemyTableMenu(id, playerInv, this);
    }

    private List<AlchemyRecipe> matchingRecipes = new ArrayList<>();
    private int selectedRecipeIndex = -1;

    public void updateMatchingRecipes() {
        if (level == null || level.isClientSide) return;

        ItemStack input = itemHandler.getStackInSlot(0);
        ItemStack cost = itemHandler.getStackInSlot(1);

        SimpleContainer container = new SimpleContainer(2);
        container.setItem(0, input);
        container.setItem(1, cost);

        matchingRecipes = new ArrayList<>();

        for (AlchemyRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.ALCHEMY_TYPE.get())) {
            if (recipe.matchesStacks(input, cost)) {
                if (recipe.getId().getPath().contains("planks") && recipe.getType() == ModRecipeTypes.ALCHEMY_TYPE.get()) {
                    List<ItemStack> outputs = recipe.getDynamicResults(input, level);
                    //Debug
                    List<ItemStack> results = recipe.getDynamicResults(input, level);
                    System.out.println("[Alchemy DEBUG] Dynamic results: " + results.size());

                    for (ItemStack stack : outputs) {
                        matchingRecipes.add(new AlchemyRecipe(
                                recipe.getId(),
                                recipe.getInput(),
                                recipe.getCatalyst(),
                                List.of(stack),
                                false
                        ));
                    }
                } else {
                    matchingRecipes.add(recipe);
                }
            }
            //Debug
            System.out.println("[Alchemy DEBUG] Found recipe: " + recipe.getId());

            if (recipe.matchesStacks(input, cost)) {
                System.out.println("[Alchemy DEBUG] Recipe matches!");
            }

        }


        // Reset selection when input changes
        selectedRecipeIndex = -1;
        itemHandler.setStackInSlot(2, ItemStack.EMPTY);
    }

    public void selectRecipe(int index) {
        if (index >= 0 && index < matchingRecipes.size()) {
            selectedRecipeIndex = index;
            AlchemyRecipe selected = matchingRecipes.get(index);
            itemHandler.setStackInSlot(2, selected.getResultItem(getLevel().registryAccess()).copy());
        }
    }

    public List<AlchemyRecipe> getMatchingRecipes() {
        return matchingRecipes;
    }

    public void onTakeOutput() {
        if (level == null || level.isClientSide || selectedRecipeIndex == -1) return;

        ItemStack input = itemHandler.getStackInSlot(0);
        ItemStack cost = itemHandler.getStackInSlot(1);

        AlchemyRecipe selected = matchingRecipes.get(selectedRecipeIndex);
        if (selected.matchesStacks(input, cost)) {
            itemHandler.extractItem(0, 1, false);
            itemHandler.extractItem(1, 1, false);
            updateMatchingRecipes();  // Re-check available options after taking output
        }
    }

}
