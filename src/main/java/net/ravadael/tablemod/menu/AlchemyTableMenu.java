
package net.ravadael.tablemod.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerListener;
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

public class AlchemyTableMenu extends AbstractContainerMenu implements ContainerListener {
    private final AlchemyTableBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final Level level;
    private final Player player;
    private final List<ItemStack> recipeResults = new ArrayList<>();
    private int selectedIndex = -1;

    private int startIndex = 0;

    public static final int DISPLAY_COUNT = 12;

    private final net.minecraft.world.inventory.ResultContainer result = new net.minecraft.world.inventory.ResultContainer();


    public AlchemyTableMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, (AlchemyTableBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()));

    }

    public AlchemyTableMenu(int id, Inventory inv, AlchemyTableBlockEntity blockEntity) {
        super(ModMenuTypes.ALCHEMY_TABLE_MENU.get(), id);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.level = inv.player.level();
        this.addSlotListener(this);


        this.addSlot(new Slot(blockEntity, 0, 20, 23));
        this.addSlot(new Slot(blockEntity, 1, 20, 42));
        this.addSlot(new ResultSlot(this.result, this.blockEntity, 0, 143, 32));

        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        for (int k = 0; k < 9; ++k)
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));

        if (!this.level.isClientSide) {
            // Initial server-side recipe build and default selection
            refreshRecipeList();
            if (this.selectedIndex < 0 && !this.recipeResults.isEmpty()) {
                this.selectedIndex = 0;
            }
            this.refreshOutput();
        } else {
            refreshRecipeList();
        }
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
            if (!this.level.isClientSide) { this.result.setItem(0, ItemStack.EMPTY); } }
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
        ItemStack ret = ItemStack.EMPTY;
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        final int SLOT_INPUT = 0;
        final int SLOT_FUEL = 1;
        final int SLOT_RESULT = 2;

        final int TE_SLOTS = 3;

        final int PLAYER_INV_START = TE_SLOTS;            // 3
        final int PLAYER_INV_END   = PLAYER_INV_START + 27; // 30 (exclusive)
        final int HOTBAR_START     = PLAYER_INV_END;        // 30
        final int HOTBAR_END       = HOTBAR_START + 9;      // 39 (exclusive)

        // 1) Shift-click from result -> to player inventory/hotbar
        if (index == SLOT_RESULT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, ret);
        }
        // 2) Shift-click from TE slots (input/fuel) -> to player inventory
        else if (index == SLOT_INPUT || index == SLOT_FUEL) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        }
        // 3) Shift-click from player inventory/hotbar
        else {
            // Try to move to fuel
            Slot fuelSlot = this.slots.get(SLOT_FUEL);
            boolean moved = false;
            if (fuelSlot != null && fuelSlot.mayPlace(stack)) {
                if (this.moveItemStackTo(stack, SLOT_FUEL, SLOT_FUEL + 1, false)) {
                    moved = true;
                }
            }
            // Try to move to input if not moved
            if (!moved) {
                Slot inputSlot = this.slots.get(SLOT_INPUT);
                if (inputSlot != null && inputSlot.mayPlace(stack)) {
                    if (this.moveItemStackTo(stack, SLOT_INPUT, SLOT_INPUT + 1, false)) {
                        moved = true;
                    }
                }
            }
            // Otherwise, move between main inventory and hotbar
            if (!moved) {
                if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                    if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == ret.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return ret;
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
            result.setItem(0, recipeResults.get(index).copy());
        } else {
            this.selectedIndex = -1;
            result.setItem(0, ItemStack.EMPTY);
        }
    }

    public void selectRecipe(int index) {
        if (this.level.isClientSide) return;

        this.selectedIndex = index;

        if (index >= 0 && index < recipeResults.size()) {
            ItemStack out = recipeResults.get(index).copy();
            result.setItem(0, out);
        } else {
            result.setItem(0, ItemStack.EMPTY);
        }

        this.broadcastChanges(); // sync to client
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.level.isClientSide) return true; // on laisse le client “croire” que c’est OK

        if (id >= 0 && id < this.recipeResults.size()) {
            this.selectedIndex = id;
            this.refreshOutput();   // remplit immédiatement le slot résultat
            this.broadcastChanges();
            return true;
        }
        return false;
    }

    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        if (this.level.isClientSide) return;

        this.refreshRecipeList();
        if (this.selectedIndex < 0 || this.selectedIndex >= this.recipeResults.size()) {
            this.selectedIndex = -1;
        }
        if (this.selectedIndex < 0 && !this.recipeResults.isEmpty()) {
            this.selectedIndex = 0;
        }
        this.refreshOutput();
        System.out.println("AlchemyTableMenu: slotsChanged triggered");

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
            result.setItem(0, ItemStack.EMPTY);
        }
    }

    public void rebuildAvailable() {
        if (this.level.isClientSide) return;

        if (this.selectedIndex < 0 || this.selectedIndex >= this.recipeResults.size()) {
            this.selectedIndex = -1;
        }
        this.refreshOutput();
    }

    // Donne la recette sélectionnée, ou null si index invalide
    public @org.jetbrains.annotations.Nullable net.minecraft.world.item.ItemStack getSelectedOutputCopy() {
        if (this.selectedIndex >= 0 && this.selectedIndex < this.recipeResults.size()) {
            return this.recipeResults.get(this.selectedIndex).copy();
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    public boolean canCraftOnce() {
        if (this.selectedIndex < 0 || this.selectedIndex >= this.recipeResults.size()) return false;

        // Lis l’état courant des inputs dans le BE
        net.minecraft.world.item.ItemStack in   = this.blockEntity.getItem(0);
        net.minecraft.world.item.ItemStack fuel = this.blockEntity.getItem(1);
        if (in.isEmpty() || fuel.isEmpty()) return false;
        int inCost = 1;
        int fuelCost = 1;
        return in.getCount() >= inCost && fuel.getCount() >= fuelCost;
    }

    public void refreshOutput() {
        // au début de refreshOutput()
        if (this.blockEntity.getItem(0).isEmpty() || this.blockEntity.getItem(1).isEmpty()) {
            this.result.setItem(0, net.minecraft.world.item.ItemStack.EMPTY);
            this.broadcastChanges();
            return;
        }

        if (this.level.isClientSide) return;


        // Auto-select first recipe if none selected (server-side)
        if (this.selectedIndex < 0 && !this.recipeResults.isEmpty()) {
            this.selectedIndex = 0;
        }
        net.minecraft.world.item.ItemStack out = net.minecraft.world.item.ItemStack.EMPTY;
        if (this.canCraftOnce()) {
            out = getSelectedOutputCopy();
        }
        this.result.setItem(0, out);
        this.broadcastChanges();
    }

    @Override
    public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {

    }

    @Override
    public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {

    }
}
