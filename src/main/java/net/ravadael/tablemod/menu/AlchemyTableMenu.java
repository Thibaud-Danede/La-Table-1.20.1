
package net.ravadael.tablemod.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;
import net.ravadael.tablemod.recipe.AlchemyRecipe;
import net.ravadael.tablemod.util.ModTags;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

public class AlchemyTableMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos blockPos;
    private final IItemHandler itemHandler;

    public AlchemyTableMenu(int id, Inventory playerInventory, AlchemyTableBlockEntity blockEntity) {
        super(ModMenus.ALCHEMY_TABLE_MENU.get(), id);
        this.level = blockEntity.getLevel();
        this.blockPos = blockEntity.getBlockPos();
        this.itemHandler = blockEntity.getItemHandler();
        this.blockEntity = blockEntity;
        this.availableRecipes = blockEntity.getMatchingRecipes();

        setupSlots(playerInventory);
    }

    public AlchemyTableMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, (AlchemyTableBlockEntity) playerInventory.player.level()
                .getBlockEntity(buf.readBlockPos()));
    }


    private void setupSlots(Inventory playerInventory) {
        // Table inventory slots
        this.addSlot(new SlotItemHandler(itemHandler, 0, 20, 23)); // Input
        this.addSlot(new SlotItemHandler(itemHandler, 1, 20, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModTags.Items.ALCHEMY_FUEL);
            }
        });
        this.addSlot(new SlotItemHandler(itemHandler, 2, 143, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                blockEntity.onTakeOutput();
            }
        });

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Implement later if needed
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(this.blockPos).getBlock() == net.ravadael.tablemod.block.ModBlocks.ALCHEMY_TABLE.get();

    }

    private final AlchemyTableBlockEntity blockEntity;

    // Store visible recipes
    private List<AlchemyRecipe> availableRecipes = new ArrayList<>();

    public void selectRecipeIndex(int index) {
        if (index >= 0 && index < availableRecipes.size()) {
            blockEntity.selectRecipe(index);
        }
    }

    public List<AlchemyRecipe> getAvailableRecipes() {
        return this.availableRecipes;
    }



}
