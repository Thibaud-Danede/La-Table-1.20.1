package net.ravadael.tablemod.menu;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;
import net.ravadael.tablemod.recipe.AlchemyRecipe;
import net.ravadael.tablemod.recipe.AlchemyRecipeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AlchemyTableMenu extends AbstractContainerMenu {

    private final SimpleContainer input = new SimpleContainer(2);
    private final SimpleContainer result = new SimpleContainer(1);
    private final ContainerLevelAccess access;
    private final Level level;

    private List<AlchemyRecipe> recipes = List.of();
    private ItemStack selectedOutput = ItemStack.EMPTY;

    private ItemStack lastInputItem = ItemStack.EMPTY;
    private ItemStack lastCatalystItem = ItemStack.EMPTY;

    public AlchemyTableMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level(), buf.readBlockPos());
    }

    public AlchemyTableMenu(int id, Inventory inv) {
        this(id, inv, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(BlockPos.ZERO));
    }

    public AlchemyTableMenu(int id, Inventory inv, Level level, BlockPos pos) {
        super(ModMenuTypes.ALCHEMY_TABLE_MENU.get(), id);
        this.level = level;
        this.access = ContainerLevelAccess.create(level, pos);

        // INPUT SLOT
        this.addSlot(new Slot(input, 0, 20, 23) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(input);
            }
        });

        // CATALYST SLOT (PEUT ÊTRE VIDE)
        this.addSlot(new Slot(input, 1, 20, 42) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(input);
            }
        });

        // OUTPUT SLOT
        this.addSlot(new Slot(result, 0, 143, 33) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());

                // Consume input
                ItemStack in = input.getItem(0);
                in.shrink(1);
                if (in.isEmpty()) input.setItem(0, ItemStack.EMPTY);

                // Consume catalyst ONLY IF RECIPE REQUIRES ONE
                AlchemyRecipe recipe = recipes.isEmpty() ? null : recipes.get(0);
                if (recipe != null && recipe.isCatalystRequired()) {
                    ItemStack cat = input.getItem(1);
                    cat.shrink(1);
                    if (cat.isEmpty()) input.setItem(1, ItemStack.EMPTY);
                }

                updateRecipes();
                assembleSelectedOutput();

                player.playSound(SoundEvents.BREWING_STAND_BREW, 0.3F, 1.0F);

                super.onTake(player, stack);
            }
        });

        // INVENTORY + HOTBAR
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        for (int k = 0; k < 9; ++k)
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack inputItem = input.getItem(0);
        ItemStack catalystItem = input.getItem(1);

        boolean changed =
                !ItemStack.isSameItemSameTags(inputItem, lastInputItem) ||
                        !ItemStack.isSameItemSameTags(catalystItem, lastCatalystItem);

        lastInputItem = inputItem.copy();
        lastCatalystItem = catalystItem.copy();

        if (changed) {
            selectedOutput = ItemStack.EMPTY;
        }

        updateRecipes();
    }

    /** RECHERCHE DES RECETTES POSSIBLES */
    private void updateRecipes() {
        ItemStack inp = input.getItem(0);

        // Input vide = aucune recette
        if (inp.isEmpty()) {
            recipes = List.of();
            selectedOutput = ItemStack.EMPTY;
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }

        List<AlchemyRecipe> all = level.getRecipeManager().getAllRecipesFor(AlchemyRecipeType.INSTANCE);
        List<AlchemyRecipe> valid = new ArrayList<>();

        // Afficher les recettes dès que l'input matche (même sans catalyseur)
        for (AlchemyRecipe r : all)
            if (r.matchesInputOnly(input))
                valid.add(r);

        valid.sort(Comparator.comparing(r ->
                r.getResultItem(level.registryAccess()).getDisplayName().getString()
        ));

        recipes = valid;

        assembleSelectedOutput();
    }

    /** Applique selectedOutput si encore valide */
    public void assembleSelectedOutput() {
        if (selectedOutput.isEmpty()) {
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }

        ItemStack inp = input.getItem(0);
        if (inp.isEmpty()) {
            selectedOutput = ItemStack.EMPTY;
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }

        for (AlchemyRecipe r : recipes) {
            for (ItemStack out : r.getFilteredResults(inp)) {
                if (ItemStack.isSameItemSameTags(out, selectedOutput)) {
                    // N'afficher le résultat que si la recette complète matche (input + catalyseur)
                    if (r.matches(input, level)) {
                        result.setItem(0, out.copy());
                    } else {
                        result.setItem(0, ItemStack.EMPTY);
                    }
                    broadcastChanges();
                    return;
                }
            }
        }

        selectedOutput = ItemStack.EMPTY;
        result.setItem(0, ItemStack.EMPTY);
        broadcastChanges();
    }

    /** Packet → sélection utilisateur */
    public void setSelectedOutput(ItemStack output) {
        this.selectedOutput = output.copy();
        assembleSelectedOutput();
    }

    public ItemStack getInputItem() {
        return input.getItem(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();

        // OUTPUT SHIFT-CLICK
        if (index == 2) {

            if (selectedOutput.isEmpty())
                return ItemStack.EMPTY;

            ItemStack out = selectedOutput.copy();
            ItemStack inp = input.getItem(0);
            ItemStack cat = input.getItem(1);

            int maxCrafts = inp.getCount();

            // Si catalyst obligatoire
            if (!recipes.isEmpty() && recipes.get(0).isCatalystRequired()) {
                maxCrafts = Math.min(maxCrafts, cat.getCount());
            }

            maxCrafts = Math.min(maxCrafts, out.getMaxStackSize());

            boolean crafted = false;

            for (int i = 0; i < maxCrafts; i++) {

                if (!this.moveItemStackTo(out.copy(), 3, 39, true))
                    break;

                // Consume input
                inp.shrink(1);
                if (inp.isEmpty()) input.setItem(0, ItemStack.EMPTY);

                // Consume catalyst if needed
                if (!recipes.isEmpty() && recipes.get(0).isCatalystRequired()) {
                    cat.shrink(1);
                    if (cat.isEmpty()) input.setItem(1, ItemStack.EMPTY);
                }

                crafted = true;
            }

            if (crafted && player != null)
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BREWING_STAND_BREW,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.3F, 1.0F);

            updateRecipes();
            assembleSelectedOutput();

            return result;
        }

        // MOVE FROM INPUT SLOTS → INVENTORY
        if (index < 2) {
            if (!this.moveItemStackTo(stackInSlot, 3, 39, true))
                return ItemStack.EMPTY;
        }

        // INVENTORY → INPUT
        else {
            // Try input first
            if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                // Then catalyst
                if (!this.moveItemStackTo(stackInSlot, 1, 2, false))
                    return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, stackInSlot);

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate(
                (level, pos) -> player.distanceToSqr(
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D
                ) <= 64,
                true
        );
    }

    public List<AlchemyRecipe> getCurrentRecipes() { return recipes; }

    /** Retourne la recette qui produit ce résultat (pour afficher le catalyseur dans le tooltip) */
    public AlchemyRecipe getRecipeForResult(ItemStack result) {
        ItemStack inp = input.getItem(0);
        if (inp.isEmpty()) return null;
        for (AlchemyRecipe r : recipes) {
            for (ItemStack out : r.getFilteredResults(inp)) {
                if (ItemStack.isSameItemSameTags(out, result))
                    return r;
            }
        }
        return null;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        this.access.execute((lvl, pos) -> {
            // --- Joue le son de fermeture ---
            lvl.playSound(
                    null,
                    pos,
                    SoundEvents.BOOK_PAGE_TURN,   // ⬅ change selon tes préférences
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            // Récupère le BlockEntity
            if (lvl.getBlockEntity(pos) instanceof AlchemyTableBlockEntity be) {
                be.removeUser(player);   // 👉 indispensable
            }

            // Comportement vanilla : drop l’input si nécessaire
            this.clearContainer(player, this.input);
        });
    }

}
