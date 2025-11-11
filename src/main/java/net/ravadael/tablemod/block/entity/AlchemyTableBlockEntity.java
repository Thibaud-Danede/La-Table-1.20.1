
package net.ravadael.tablemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.ravadael.tablemod.menu.AlchemyTableMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemyTableBlockEntity extends BlockEntity implements MenuProvider, Container {

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    public AlchemyTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_TABLE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tablemod.alchemy_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AlchemyTableMenu(id, inv, this);
    }

    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 0) {
            return stack.is(Items.OAK_PLANKS); // Expand later to other planks
        } else if (slot == 1) {
            return stack.is(Items.GLOWSTONE_DUST);
        }
        return false;
    }

    public int getContainerSize() {
        return items.size();
    }

    public boolean isEmpty() {
        for (ItemStack itemstack : items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }


    public @NotNull ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    public void clearContent() {
        items.clear();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0) return stack.is(Items.OAK_PLANKS);
        if (index == 1) return stack.is(Items.GLOWSTONE_DUST);
        return false; // Slot 2 is output only
    }

    @Override
    public boolean stillValid(Player player) {
        return this.getLevel() != null &&
                this.getLevel().getBlockEntity(this.getBlockPos()) == this &&
                player.distanceToSqr(this.getBlockPos().getCenter()) <= 64.0D;
    }
}
