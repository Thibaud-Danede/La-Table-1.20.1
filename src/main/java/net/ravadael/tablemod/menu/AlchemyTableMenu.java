// net.ravadael.tablemod.menu.AlchemyTableMenu
package net.ravadael.tablemod.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;

public class AlchemyTableMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos pos;
    private AlchemyTableBlockEntity be;           // peut être null côté client si résolution échoue (fallback)
    private final ContainerData data;             // 0 = progress, 1 = maxProgress

    // CLIENT : lit le BlockPos
    public AlchemyTableMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, buf.readBlockPos());
    }

    // COMMUN : résout la BE et place les slots
    public AlchemyTableMenu(int id, Inventory playerInv, BlockPos pos) {
        super(ModMenus.ALCHEMY_TABLE_MENU.get(), id);
        this.level = playerInv.player.level();
        this.pos = pos;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AlchemyTableBlockEntity table) {
            this.be = table;
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                    .ifPresent(handler -> setupSlots(playerInv, handler));
        } else {
            setupSlots(playerInv, null);
        }

        // ---- Synchro progress/maxProgress vers le client ----
        this.data = new ContainerData() {
            @Override public int get(int index) {
                if (be == null) return 0;
                return switch (index) {
                    case 0 -> be.getProgress();
                    case 1 -> be.getMaxProgress();
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {
                // côté client, MC va appeler set(...) pour maj la copie locale
                if (be == null) return;
                // on ne modifie pas la BE serveur ici; pas nécessaire
            }
            @Override public int getCount() { return 2; }
        };
        this.addDataSlots(this.data);
    }

    private void setupSlots(Inventory playerInv, IItemHandler handler) {
        // Slots BE
        if (handler != null) {
            this.addSlot(new SlotItemHandler(handler, 0, 44, 35));   // input
            this.addSlot(new SlotItemHandler(handler, 1, 62, 35));   // catalyst
            this.addSlot(new SlotItemHandler(handler, 2, 120, 35));  // output
        } else {
            // Fallback client (évite NPE si pas de capability)
            this.addSlot(new Slot(new SimpleContainer(1), 0, 44, 35));
            this.addSlot(new Slot(new SimpleContainer(1), 1, 62, 35));
            this.addSlot(new Slot(new SimpleContainer(1), 2, 120, 35));
        }

        // Inventaire joueur (27) + hotbar (9)
        int startX = 8, startY = 84, hotbarY = 142;
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, startX + col * 18, startY + row * 18));
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(playerInv, col, startX + col * 18, hotbarY));
    }

    @Override
    public boolean stillValid(Player player) {
        return ContainerLevelAccess.create(level, pos)
                .evaluate((lvl, p) -> player.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) <= 64.0, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            newStack = stack.copy();

            int beSlots = 3;
            if (index < beSlots) {
                if (!this.moveItemStackTo(stack, beSlots, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(stack, 0, 2, false)) return ItemStack.EMPTY; // essaye input/catalyst
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return newStack;
    }

    // ---- Méthode appelée par l'écran pour dessiner la barre ----
    public int getProgressScaled(int width) {
        int prog = this.data.get(0);
        int max  = this.data.get(1);
        return max == 0 ? 0 : prog * width / max;
    }
}
