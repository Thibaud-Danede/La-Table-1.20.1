
package net.ravadael.tablemod.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;
import net.ravadael.tablemod.menu.slot.ResultSlot;

public class AlchemyTableMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final AlchemyTableBlockEntity blockEntity;
    private final Level level;

    public AlchemyTableMenu(int id, Inventory inv, AlchemyTableBlockEntity blockEntity) {
        super(ModMenuTypes.ALCHEMY_TABLE_MENU.get(), id);
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.blockEntity = blockEntity;
        this.level = inv.player.level();

        // Input Slot (Planks)
        this.addSlot(new Slot(blockEntity, 0, 20, 23));
        // Power Slot (Glowstone)
        this.addSlot(new Slot(blockEntity, 1, 20, 42));
        // Result Slot (Dynamic)
        this.addSlot(new ResultSlot(inv.player, blockEntity, 2, 143, 33));

        // Player Inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // Optional: implement shift-click later
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity != null && this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity
                && player.distanceToSqr(this.blockEntity.getBlockPos().getCenter()) < 64.0;
    }

    public AlchemyTableMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, (AlchemyTableBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }


    public AlchemyTableBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
