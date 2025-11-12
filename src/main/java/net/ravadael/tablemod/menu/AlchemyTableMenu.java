
package net.ravadael.tablemod.menu;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ravadael.tablemod.recipe.AlchemyRecipe;
import net.ravadael.tablemod.recipe.AlchemyRecipeType;
import net.ravadael.tablemod.recipe.ModRecipes;

import java.util.List;

public class AlchemyTableMenu extends AbstractContainerMenu {
    private final SimpleContainer input = new SimpleContainer(1);
    private final SimpleContainer result = new SimpleContainer(1);
    private final ContainerLevelAccess access;
    private final Level level;
    private List<AlchemyRecipe> recipes;
    private int selectedRecipeIndex = -1;

    public AlchemyTableMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level(), buf.readBlockPos());
    }

    public AlchemyTableMenu(int id, Inventory inv) {
        this(id, inv, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(BlockPos.ZERO));
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

        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int k = 0; k < 9; ++k)
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));

        this.recipes = level.getRecipeManager().getAllRecipesFor(ModRecipes.ALCHEMY_RECIPE_TYPE.get());
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
                .filter(r -> r.matches(input, level))
                .toList();

        this.selectedRecipeIndex = -1; // Clear selection on input change
        this.result.setItem(0, ItemStack.EMPTY);
        broadcastChanges();
    }

    public void setSelectedRecipeIndex(int index) {
        if (index >= 0 && index < recipes.size()) {
            this.selectedRecipeIndex = index;
            assembleRecipe();
        }
    }

    private void assembleRecipe() {
        if (selectedRecipeIndex >= 0 && selectedRecipeIndex < recipes.size()) {
            AlchemyRecipe recipe = recipes.get(selectedRecipeIndex);
            result.setItem(0, recipe.assemble(input, level.registryAccess()));
        } else {
            result.setItem(0, ItemStack.EMPTY);
        }
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        setSelectedRecipeIndex(id);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == 1) { // Output slot
                ItemStack resultStack = result.getItem(0);
                if (resultStack.isEmpty()) return ItemStack.EMPTY;

                ItemStack originalResult = resultStack.copy();

                while (!input.getItem(0).isEmpty() && !result.getItem(0).isEmpty()) {
                    ItemStack crafted = result.getItem(0).copy();
                    if (!this.moveItemStackTo(crafted, 2, 38, true)) break;

                    input.getItem(0).shrink(1);
                    if (input.getItem(0).isEmpty()) input.setItem(0, ItemStack.EMPTY);

                    assembleRecipe();
                }

                slot.onQuickCraft(resultStack, originalResult);
                return originalResult;

            } else if (index == 0) { // Input slot
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, pos) ->
                        player.distanceToSqr((double) pos.getX() + 0.5D,
                                (double) pos.getY() + 0.5D,
                                (double) pos.getZ() + 0.5D) <= 64.0D,
                true);
    }

    public List<AlchemyRecipe> getCurrentRecipes() {
        return recipes;
    }
}
