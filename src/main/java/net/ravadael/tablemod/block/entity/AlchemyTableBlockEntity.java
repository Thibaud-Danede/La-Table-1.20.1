package net.ravadael.tablemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
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
import net.ravadael.tablemod.TableMod;
import net.ravadael.tablemod.menu.AlchemyTableMenu;
import net.ravadael.tablemod.recipe.ModRecipeTypes;
import net.ravadael.tablemod.recipe.TransmuteRecipe;

import javax.annotation.Nullable;
import java.util.Optional;

public class AlchemyTableBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    // Slots: 0=input, 1=catalyst, 2=output
    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override protected void onContentsChanged(int slot) { setChanged(); }
    };

    private LazyOptional<IItemHandler> itemCap = LazyOptional.empty();

    private int progress = 0;
    private int maxProgress = 200;

    public AlchemyTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_TABLE_BE.get(), pos, state);
    }

    // ---- Capabilities ----
    @Override
    public void onLoad() {
        super.onLoad();
        itemCap = LazyOptional.of(() -> items);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCap.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @org.jetbrains.annotations.Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCap.cast();
        }
        return super.getCapability(cap, side);
    }

    // ---------- TICK SERVEUR ----------
    public static void tick(Level level, BlockPos pos, BlockState state, AlchemyTableBlockEntity be) {
        if (level.isClientSide) return;

        Optional<TransmuteRecipe> match = be.getMatchingRecipe(level);
        if (match.isPresent() && be.canOutput(match.get().getResultItem(level.registryAccess()))) {
            be.progress++;
            be.maxProgress = match.get().getTime();
            if (be.progress >= be.maxProgress) {
                be.craft(level, match.get());
                be.progress = 0;
            }
        } else {
            be.progress = 0;
        }
    }

    // ---------- RECETTES ----------
    private Optional<TransmuteRecipe> getMatchingRecipe(Level level) {
        Container container = new SimpleContainer(3);
        container.setItem(0, items.getStackInSlot(0));
        container.setItem(1, items.getStackInSlot(1));
        return level.getRecipeManager().getRecipeFor(ModRecipeTypes.TRANSMUTE_TYPE.get(), container, level);
    }

    private boolean canOutput(ItemStack result) {
        ItemStack out = items.getStackInSlot(2);
        if (out.isEmpty()) return true;
        if (!ItemStack.isSameItemSameTags(out, result)) return false;
        return out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    private void craft(Level level, TransmuteRecipe recipe) {
        items.extractItem(0, 1, false);
        items.extractItem(1, 1, false);
        ItemStack res = recipe.getResultItem(level.registryAccess()).copy();
        ItemStack out = items.getStackInSlot(2);
        if (out.isEmpty()) items.setStackInSlot(2, res);
        else { out.grow(res.getCount()); items.setStackInSlot(2, out); }
        setChanged();
    }

    // ---------- MENU ----------
    @Override public Component getDisplayName() {
        return Component.translatable("block." + TableMod.MOD_ID + ".alchemy_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        // côté serveur : NetworkHooks enverra le BlockPos au client
        return new AlchemyTableMenu(id, playerInv, this.getBlockPos());
    }

    // ---------- Accès ----------
    public ItemStackHandler getItems() { return items; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return maxProgress; }
}
