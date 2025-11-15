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

        // INPUT
        this.addSlot(new Slot(input, 0, 20, 23) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(input);
            }
        });

        // CATALYST
        this.addSlot(new Slot(input, 1, 20, 42) {
            @Override
            public void setChanged() {
                super.setChanged();
                slotsChanged(input);
            }
        });

        // OUTPUT
        this.addSlot(new Slot(result, 0, 143, 33) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());

                ItemStack in = input.getItem(0);
                in.shrink(1);
                if (in.isEmpty()) input.setItem(0, ItemStack.EMPTY);

                ItemStack cat = input.getItem(1);
                cat.shrink(1);
                if (cat.isEmpty()) input.setItem(1, ItemStack.EMPTY);

                // 🔥 FIX : recalcul complet dès qu’un stack change
                updateRecipes();

                // Re-assembler la sélection si encore valide
                assembleSelectedOutput();

                player.playSound(SoundEvents.BREWING_STAND_BREW, 0.3F, 1.0F);
                super.onTake(player, stack);
            }
        });

        // INVENTAIRE JOUEUR
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

    private void updateRecipes() {
        ItemStack inp = input.getItem(0);
        ItemStack cat = input.getItem(1);

        // Si un slot est vide → aucune recette
        if (inp.isEmpty() || cat.isEmpty()) {
            recipes = List.of();
            selectedOutput = ItemStack.EMPTY;
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }

        List<AlchemyRecipe> all = level.getRecipeManager().getAllRecipesFor(AlchemyRecipeType.INSTANCE);
        List<AlchemyRecipe> valid = new ArrayList<>();

        for (AlchemyRecipe r : all)
            if (r.matches(input, level))
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
        ItemStack cat = input.getItem(1);

        if (inp.isEmpty() || cat.isEmpty()) {
            selectedOutput = ItemStack.EMPTY;
            result.setItem(0, ItemStack.EMPTY);
            broadcastChanges();
            return;
        }

        for (AlchemyRecipe r : recipes) {
            for (ItemStack out : r.getFilteredResults(inp)) {
                if (ItemStack.isSameItemSameTags(out, selectedOutput)) {
                    result.setItem(0, out.copy());
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

        // --------------------------------------------------
        // 1. OUTPUT SLOT SHIFT-CLICK (index == 2)
        // --------------------------------------------------
        if (index == 2) {

            if (selectedOutput.isEmpty())
                return ItemStack.EMPTY;

            ItemStack out = selectedOutput.copy();
            ItemStack inp = input.getItem(0);
            ItemStack cat = input.getItem(1);

            int maxCrafts = Math.min(inp.getCount(), cat.getCount());
            maxCrafts = Math.min(maxCrafts, out.getMaxStackSize());

            boolean crafted = false;

            for (int i = 0; i < maxCrafts; i++) {

                // Cannot push to inventory
                if (!this.moveItemStackTo(out.copy(), 3, 39, true))
                    break;

                // Consume input
                inp.shrink(1);
                if (inp.isEmpty()) input.setItem(0, ItemStack.EMPTY);

                // Consume catalyst
                cat.shrink(1);
                if (cat.isEmpty()) input.setItem(1, ItemStack.EMPTY);

                crafted = true;
            }

            if (crafted && player != null)
                level.playSound(
                        null,                                     // null = tous les joueurs proches l'entendent
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.BREWING_STAND_BREW,
                        net.minecraft.sounds.SoundSource.BLOCKS,  // stonecutter uses BLOCKS
                        0.3F,
                        1.0F
                );


            // Refresh recipes and output
            updateRecipes();
            assembleSelectedOutput();

            return result;
        }

        // --------------------------------------------------
        // 2. MOVE FROM INPUT / CATALYST SLOTS → INVENTORY
        // --------------------------------------------------
        if (index < 2) {
            if (!this.moveItemStackTo(stackInSlot, 3, 39, true))
                return ItemStack.EMPTY;
        }

        // --------------------------------------------------
        // 3. MOVE FROM INVENTORY → INPUT / CATALYST
        // --------------------------------------------------
        else {
            // Move to input (slot 0) first
            if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {

                // Move to catalyst (slot 1)
                if (!this.moveItemStackTo(stackInSlot, 1, 2, false))
                    return ItemStack.EMPTY;
            }
        }

        // --------------------------------------------------
        // Final slot cleanup
        // --------------------------------------------------
        if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, stackInSlot);

        return result;
    }


    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate(
                (level, pos) -> player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64,
                true
        );
    }

    public List<AlchemyRecipe> getCurrentRecipes() { return recipes; }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((lvl, pos) -> this.clearContainer(player, this.input));
    }
}
