
package net.ravadael.tablemod.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;

public class ResultSlot extends Slot {
    private final Player player;
    private final AlchemyTableBlockEntity blockEntity;

    public ResultSlot(Player player, AlchemyTableBlockEntity blockEntity, int index, int x, int y) {
        super(blockEntity, index, x, y);
        this.player = player;
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !this.getItem().isEmpty();
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        ItemStack input = blockEntity.getItem(0);
        ItemStack fuel = blockEntity.getItem(1);

        if (!input.isEmpty() && input.getCount() > 0 && !fuel.isEmpty() && fuel.getCount() > 0) {
            input.shrink(1);
            fuel.shrink(1);
        }

        this.set(ItemStack.EMPTY);
        blockEntity.setChanged();
    }

}
