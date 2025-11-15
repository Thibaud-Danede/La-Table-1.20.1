package net.ravadael.tablemod.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.ravadael.tablemod.menu.AlchemyTableMenu;
import net.ravadael.tablemod.network.ModMessages;
import net.ravadael.tablemod.recipe.AlchemyRecipe;

import java.util.ArrayList;
import java.util.List;

public class AlchemyTableScreen extends AbstractContainerScreen<AlchemyTableMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("tablemod", "textures/gui/alchemy_table.png");

    // Grid settings
    private static final int COLS = 4;
    private static final int ROWS = 3;
    private static final int MAX_VISIBLE = COLS * ROWS;

    // Button layout
    private static final int GRID_X = 52;
    private static final int GRID_Y = 15;
    private static final int BTN_W = 16;
    private static final int BTN_H = 18;
    private static final int SPACE_X = 16;
    private static final int SPACE_Y = 18;

    private int scrollOffset = 0;
    private int selectedIndex = -1;

    private boolean isScrolling = false;   // dragging scrollbar

    private ItemStack lastInput = ItemStack.EMPTY;

    public AlchemyTableScreen(AlchemyTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    // -------------------------------------------
    //  COLLECT ALL RESULTS (filtered list)
    // -------------------------------------------
    private List<ItemStack> collectResults() {
        ItemStack input = menu.getInputItem();
        List<ItemStack> list = new ArrayList<>();

        for (AlchemyRecipe r : menu.getCurrentRecipes()) {
            list.addAll(r.getFilteredResults(input));
        }

        return list;
    }

    // -------------------------------------------
    // LOGIC REFRESH (called every tick)
    // -------------------------------------------
    @Override
    protected void containerTick() {
        super.containerTick();

        ItemStack inp = menu.getInputItem();
        if (!ItemStack.isSameItemSameTags(inp, lastInput)) {
            selectedIndex = -1;
            scrollOffset = 0;
            lastInput = inp.copy();
        }

        List<ItemStack> results = collectResults();
        int total = results.size();

        int totalRows = (int)Math.ceil(total / (double)COLS);
        int maxScroll = Math.max(0, totalRows - ROWS);

        if (scrollOffset > maxScroll)
            scrollOffset = maxScroll;

        if (selectedIndex >= total)
            selectedIndex = -1;
    }

    // -------------------------------------------
    //  RENDER BACKGROUND + GRID
    // -------------------------------------------
    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        gfx.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        List<ItemStack> results = collectResults();
        int count = results.size();
        int totalRows = (int)Math.ceil(count / (double)COLS);
        int maxScroll = Math.max(0, totalRows - ROWS);

        // -------------------------------------------
        // Scrollbar
        // -------------------------------------------
        int barX = leftPos + 119;
        int barY = topPos + 15;

        if (maxScroll > 0) {
            int knobY = barY + (int)(41 * scrollOffset / (double)maxScroll);
            gfx.blit(TEXTURE, barX, knobY, 176, 0, 12, 15);
        } else {
            gfx.blit(TEXTURE, barX, barY, 188, 0, 12, 15);
        }

        // -------------------------------------------
        // Render buttons
        // -------------------------------------------
        int start = scrollOffset * COLS;

        for (int i = 0; i < MAX_VISIBLE; i++) {
            int idx = start + i;
            if (idx >= count) break;

            int row = i / COLS;
            int col = i % COLS;

            int x = leftPos + GRID_X + col * SPACE_X;
            int y = topPos + GRID_Y + row * SPACE_Y;

            boolean hovered = mouseX >= x && mouseX < x + BTN_W && mouseY >= y && mouseY < y + BTN_H;
            boolean selected = idx == selectedIndex;

            // Button states (normal / hover / selected)
            if (selected)
                gfx.blit(TEXTURE, x, y, 0, 184, BTN_W, BTN_H);
            else if (hovered)
                gfx.blit(TEXTURE, x, y, 0, 202, BTN_W, BTN_H);
            else
                gfx.blit(TEXTURE, x, y, 0, 166, BTN_W, BTN_H);

            ItemStack stack = results.get(idx);
            gfx.renderItem(stack, x, y + 1);

            if (hovered)
                gfx.renderTooltip(font, stack, mouseX, mouseY);
        }
    }


    // -------------------------------------------
    // RENDER TEXT + VANILLA TOOLTIP SUPPORT
    // -------------------------------------------
    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(font, title, 8, 6, 0x404040, false);
        gfx.drawString(font, playerInventoryTitle, 8, 72, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTicks);

        // 🔥 REQUIRED FOR TOOLTIP SUPPORT OUTSIDE THE GRID
        this.renderTooltip(gfx, mouseX, mouseY);
    }


    // -------------------------------------------
    // CLICK HANDLER (item selection)
    // -------------------------------------------
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {

            // Scrollbar click detection
            if (clickScrollbar(mx, my))
                return true;

            List<ItemStack> results = collectResults();
            int count = results.size();
            int start = scrollOffset * COLS;

            for (int i = 0; i < MAX_VISIBLE; i++) {
                int idx = start + i;
                if (idx >= count) break;

                int row = i / COLS;
                int col = i % COLS;

                int x = leftPos + GRID_X + col * SPACE_X;
                int y = topPos + GRID_Y + row * SPACE_Y;

                if (mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H) {

                    selectedIndex = idx;

                    ItemStack chosen = results.get(idx).copy();
                    ModMessages.sendSelectResult(chosen);

                    if (minecraft.player != null)
                        minecraft.player.playSound(SoundEvents.UI_STONECUTTER_SELECT_RECIPE,
                                0.3F, 1.0F);

                    return true;
                }
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    // -------------------------------------------
    // SCROLLBAR DRAGGING
    // -------------------------------------------
    private boolean clickScrollbar(double mx, double my) {
        int barX = leftPos + 119;
        int barY = topPos + 15;

        if (mx >= barX && mx < barX + 12 && my >= barY && my < barY + 56) {
            isScrolling = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        isScrolling = false;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (!isScrolling) return false;

        List<ItemStack> results = collectResults();
        int count = results.size();

        int totalRows = (int)Math.ceil(count / (double)COLS);
        int maxScroll = Math.max(0, totalRows - ROWS);

        if (maxScroll <= 0) return false;

        double barY = my - (topPos + 15);
        barY = Math.max(0, Math.min(barY, 41));

        scrollOffset = (int)Math.round((barY / 41D) * maxScroll);
        return true;
    }

    // -------------------------------------------
    // MOUSE SCROLLWHEEL
    // -------------------------------------------
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        List<ItemStack> results = collectResults();
        int total = results.size();

        int totalRows = (int)Math.ceil(total / (double)COLS);
        int maxScroll = Math.max(0, totalRows - ROWS);

        if (maxScroll > 0) {
            scrollOffset -= (int) delta;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        }

        return true;
    }
}
