
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
        return false; // Can't manually place items in result
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);

        ItemStack input = blockEntity.getItem(0);
        ItemStack fuel = blockEntity.getItem(1);

        if (!input.isEmpty() && !fuel.isEmpty()) {
            input.shrink(1);
            fuel.shrink(1);
            blockEntity.setChanged();
        }
    }
}
