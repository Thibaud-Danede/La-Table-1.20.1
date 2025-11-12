package net.ravadael.tablemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.ravadael.tablemod.menu.AlchemyTableMenu;
import net.ravadael.tablemod.menu.ModMenuTypes;

public class AlchemyTableBlockEntity extends BlockEntity implements MenuProvider {
    private final SimpleContainer inventory = new SimpleContainer(1);

    public AlchemyTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_TABLE_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        // Optional server-side logic
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.fromTag(tag.getList("Items", 10));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.createTag());
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new AlchemyTableMenu(id, playerInv, level, worldPosition);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("container.tablemod.alchemy");
    }
}