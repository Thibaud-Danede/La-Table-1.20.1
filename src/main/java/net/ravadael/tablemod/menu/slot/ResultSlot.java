package net.ravadael.tablemod.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

public class ResultSlot extends Slot {
    private final AlchemyTableBlockEntity blockEntity;

    public ResultSlot(Container container, AlchemyTableBlockEntity blockEntity, int index, int x, int y) {
        super(container, index, x, y);
        this.blockEntity = blockEntity;
    }

    // ResultSlot.java
    @Override
    public void onTake(Player player, ItemStack taken) {
        if (!player.level().isClientSide && player.containerMenu instanceof AlchemyTableMenu m) {
            // consommer 1 input + 1 fuel (adapte si besoin)
            blockEntity.removeItem(0, 1);
            blockEntity.removeItem(1, 1);
            blockEntity.setChanged();

            // remettre immédiatement la prochaine sortie si craft encore possible
            m.refreshOutput();       // remet l’ItemStack dans result
            m.broadcastChanges();    // sync client
        }
        super.onTake(player, taken);
    }

    @Override
    public boolean mayPlace(ItemStack stack) { return false; } // slot sortie: pas de dépôt



    @Override
    public boolean mayPickup(net.minecraft.world.entity.player.Player player) {
        // le client peut “cliquer”, mais on ne valide que si le serveur dit OK
        if (player.level().isClientSide) return true;

        if (player.containerMenu instanceof net.ravadael.tablemod.menu.AlchemyTableMenu m) {
            return m.canCraftOnce(); // ← interdit le ghost pickup
        }
        return false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.container.setChanged();
    }

}



