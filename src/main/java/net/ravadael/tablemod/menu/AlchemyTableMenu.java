
package net.ravadael.tablemod.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;
import net.ravadael.tablemod.menu.slot.ResultSlot;

import java.util.ArrayList;
import java.util.List;

public class AlchemyTableMenu extends AbstractContainerMenu {
    private final AlchemyTableBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final Level level;

    private final List<ItemStack> recipeResults = new ArrayList<>();
    private int startIndex = 0;

    public static final int DISPLAY_COUNT = 12;

    public AlchemyTableMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, (AlchemyTableBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public AlchemyTableMenu(int id, Inventory inv, AlchemyTableBlockEntity blockEntity) {
        super(ModMenuTypes.ALCHEMY_TABLE_MENU.get(), id);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.level = inv.player.level();

        // Input (Plank)
        this.addSlot(new Slot(blockEntity, 0, 20, 23));
        // Fuel (Glowstone)
        this.addSlot(new Slot(blockEntity, 1, 20, 42));
        // Output (not used for static result, just here to mirror logic)
        this.addSlot(new ResultSlot(inv.player, blockEntity, 2, 143, 32));

        // Player inventory
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        for (int k = 0; k < 9; ++k)
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));

        refreshRecipeList();
    }

    public void refreshRecipeList() {
        recipeResults.clear();
        ItemStack input = blockEntity.getItem(0);
        ItemStack fuel = blockEntity.getItem(1);
        if (!input.isEmpty() && input.is(Items.OAK_PLANKS) && !fuel.isEmpty() && fuel.is(Items.GLOWSTONE_DUST)) {
            recipeResults.add(new ItemStack(Items.OAK_SLAB));
            recipeResults.add(new ItemStack(Items.OAK_STAIRS));
            recipeResults.add(new ItemStack(Items.OAK_FENCE));
            recipeResults.add(new ItemStack(Items.OAK_TRAPDOOR));
        }
    }

    public List<ItemStack> getVisibleRecipes() {
        int end = Math.min(startIndex + DISPLAY_COUNT, recipeResults.size());
        return recipeResults.subList(startIndex, end);
    }

    public boolean hasRecipes() {
        return !recipeResults.isEmpty();
    }

    public boolean canScroll() {
        return recipeResults.size() > DISPLAY_COUNT;
    }

    public void scrollUp() {
        if (startIndex > 0) {
            startIndex--;
        }
    }

    public void scrollDown() {
        if (startIndex + DISPLAY_COUNT < recipeResults.size()) {
            startIndex++;
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity != null && this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity &&
                player.distanceToSqr(this.blockEntity.getBlockPos().getCenter()) < 64.0;
    }

    public AlchemyTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getTotalRecipeCount() {
        return recipeResults.size();
    }

}
