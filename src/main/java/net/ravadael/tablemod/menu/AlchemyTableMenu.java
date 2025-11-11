
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
    private final Player player;
    private final List<ItemStack> recipeResults = new ArrayList<>();
    private int selectedIndex = -1;

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
        this.addSlot(new ResultSlot(blockEntity, blockEntity, 2, 143, 32));

        // Player inventory
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        for (int k = 0; k < 9; ++k)
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));

        refreshRecipeList();
        this.player = inv.player;

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

        // Reset selected recipe if input/fuel changed
        if (selectedIndex >= recipeResults.size()) {
            selectedIndex = -1;
            blockEntity.setItem(2, ItemStack.EMPTY);
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

    public Player getPlayer() {
        return this.player;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < recipeResults.size()) {
            this.selectedIndex = index;
            blockEntity.setItem(2, recipeResults.get(index).copy());
        } else {
            this.selectedIndex = -1;
            blockEntity.setItem(2, ItemStack.EMPTY);
        }
    }

    public void selectRecipe(int index) {
        if (this.level.isClientSide) return; // ← important : logique serveur uniquement

        if (index >= 0 && index < recipeResults.size()) {
            this.selectedIndex = index;
            ItemStack out = recipeResults.get(index).copy();
            blockEntity.setItem(2, out);            // ← on pose l’output côté serveur
        } else {
            this.selectedIndex = -1;
            blockEntity.setItem(2, ItemStack.EMPTY);
        }
        this.broadcastChanges(); // sync slots aux clients
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        // le client envoie l’index => on l’applique côté serveur
        this.selectRecipe(id);
        return true;
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
        if (this.level.isClientSide) return;

        // Rebuild la liste des recettes possibles avec (slot0=input, slot1=fuel)
        this.updateAvailableRecipes(); // ← ajoute la méthode ci-dessous si tu ne l’as pas

        // Réapplique l’index courant pour régénérer l’output (ou vide si plus valide)
        this.selectRecipe(this.selectedIndex);
    }

    private void updateAvailableRecipes() {
        ItemStack in = blockEntity.getItem(0);
        ItemStack fuel = blockEntity.getItem(1);

        // Remplis recipeResults en fonction des règles de ton mod
        // (si tu as déjà un code qui remplit 'recipeResults', appelle-le ici)
        // Exemples :
        // this.recipeResults = MyRecipeFinder.find(level, in, fuel);
        // ou reconstruire la liste comme tu le fais déjà au moment où tu affiches

        // Si l’input ou le fuel sont vides, vide la sélection/slot 2
        if (in.isEmpty() || fuel.isEmpty()) {
            this.selectedIndex = -1;
            blockEntity.setItem(2, ItemStack.EMPTY);
        }
    }
}
