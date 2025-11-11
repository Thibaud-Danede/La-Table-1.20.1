package net.ravadael.tablemod.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;

public class ResultSlot extends Slot {
    private final AlchemyTableBlockEntity blockEntity;

    public ResultSlot(Container container, AlchemyTableBlockEntity blockEntity, int index, int x, int y) {
        super(container, index, x, y);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false; // slot d’output
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        // Côté serveur uniquement
        if (!player.level().isClientSide) {
            // Consomme 1 input et 1 fuel (adapte si tes coûts diffèrent)
            blockEntity.removeItem(0, 1);
            blockEntity.removeItem(1, 1);

            // Recalculer les recettes dispos + reposer le bon output
            if (player.containerMenu instanceof net.ravadael.tablemod.menu.AlchemyTableMenu m) {
                // Regénère ta liste (via slotsChanged) et réapplique l’index courant
                m.slotsChanged(blockEntity); // déclenche updateAvailableRecipes() + selectRecipe(...)
            }

            blockEntity.setChanged();
        }

        super.onTake(player, stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        // pickup seulement si un item réel est présent
        return !this.getItem().isEmpty();
    }
}

