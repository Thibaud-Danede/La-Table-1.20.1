package net.ravadael.tablemod.screen;// (optionnel) package com.tontonmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.ravadael.tablemod.block.entity.AlchemyTableBlockEntity;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

import java.util.ArrayList;
import java.util.List;

public class AlchemyTableScreen extends AbstractContainerScreen<AlchemyTableMenu> {

    private static final String MODID = "tablemod";
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/alchemy_table.png");

    private static final int TEX_W = 256, TEX_H = 256;

    private static final int BG_U = 0, BG_V = 0, BG_W = 200, BG_H = 220;

    private static final int GRID_X = 52;
    private static final int GRID_Y = 15;

    private static final int CELL_W = 16;
    private static final int CELL_H = 18;

    private static final int GAP_X = 0;
    private static final int GAP_Y = 0;
    private static final int COLS  = 3;   // colonnes visibles
    private static final int ROWS  = 3;   // lignes visibles (donc 9 cellules affichées à la fois)

    private static final int BTN_U             = 0;
    private static final int BTN_V_NORMAL      = 166;
    private static final int BTN_V_SELECTED    = 184;
    private static final int BTN_V_HOVER       = 202;

    private static final int SCROLL_TRACK_U = 176, SCROLL_TRACK_V = 0,   SCROLL_TRACK_W = 12, SCROLL_TRACK_H = 56;
    private static final int SCROLL_THUMB_U = 188, SCROLL_THUMB_V = 0,   SCROLL_THUMB_W = 12, SCROLL_THUMB_H = 15;

    private static final int SCROLL_X = 119;
    private static final int SCROLL_Y = 15;
    private static final int SCROLL_H = 54; // hauteur visible de la gouttière (dans ton fond)

    private int selectedIndex = -1;

    private int scrollIndex = 0;

    public AlchemyTableScreen(AlchemyTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = BG_W;
        this.imageHeight = BG_H;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        menu.refreshRecipeList();
        g.blit(TEXTURE, leftPos, topPos, BG_U, BG_V, BG_W, BG_H, TEX_W, TEX_H);

        final int totalEntries = getTotalEntries();
        final int totalRows    = Mth.ceil(totalEntries / (float) COLS);
        final int maxScroll    = Math.max(0, totalRows - ROWS);

        int firstRow = Mth.clamp(scrollIndex, 0, maxScroll);
        int cellGlobalIndex = firstRow * COLS;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = leftPos + GRID_X + col * (CELL_W + GAP_X);
                int y = topPos  + GRID_Y + row * (CELL_H + GAP_Y);

                if (cellGlobalIndex >= totalEntries) {
                    cellGlobalIndex++;
                    continue;
                }

                boolean hovered  = isMouseOverCell(mouseX, mouseY, x, y);
                boolean selected = (cellGlobalIndex == selectedIndex);

                int v = hovered ? BTN_V_HOVER : (selected ? BTN_V_SELECTED : BTN_V_NORMAL);
                g.blit(TEXTURE, x, y, BTN_U, v, CELL_W, CELL_H, TEX_W, TEX_H);

                if (cellGlobalIndex < menu.getVisibleRecipes().size()) {
                    g.renderItem(menu.getVisibleRecipes().get(cellGlobalIndex), x + 1, y + 1);
                }

                cellGlobalIndex++;
            }
        }

        int sx = leftPos + SCROLL_X;
        int sy = topPos  + SCROLL_Y;

        g.blit(TEXTURE, sx, sy, SCROLL_TRACK_U, SCROLL_TRACK_V, SCROLL_TRACK_W, SCROLL_TRACK_H, TEX_W, TEX_H);

        int thumbOffset = getScrollPixelOffset(totalRows);
        g.blit(TEXTURE, sx, sy + thumbOffset, SCROLL_THUMB_U, SCROLL_THUMB_V, SCROLL_THUMB_W, SCROLL_THUMB_H, TEX_W, TEX_H);


        menu.refreshRecipeList(); // refresh dynamically every frame

        /*System.out.println(
                "Input: " + menu.getBlockEntity().getItem(0) +
                        " | Fuel: " + menu.getBlockEntity().getItem(1) +
                        " | Recipe count: " + menu.getVisibleRecipes().size()
        );*/

    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        final int totalEntries = getTotalEntries();
        final int totalRows    = Mth.ceil(totalEntries / (float) COLS);
        final int maxScroll    = Math.max(0, totalRows - ROWS);

        if (maxScroll > 0) {
            scrollIndex = Mth.clamp(scrollIndex - (int) Math.signum(delta), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final int totalEntries = getTotalEntries();
        final int firstRow     = scrollIndex;
        int cellGlobalIndex    = firstRow * COLS;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = leftPos + GRID_X + col * (CELL_W + GAP_X);
                int y = topPos  + GRID_Y + row * (CELL_H + GAP_Y);

                if (cellGlobalIndex < totalEntries &&
                        mouseX >= x && mouseX < x + CELL_W &&
                        mouseY >= y && mouseY < y + CELL_H) {

                    selectedIndex = cellGlobalIndex;
                    onCellClicked(selectedIndex);
                    return true;
                }
                cellGlobalIndex++;
            }
        }

        if (mouseX >= leftPos + SCROLL_X && mouseX < leftPos + SCROLL_X + SCROLL_THUMB_W &&
                mouseY >= topPos + SCROLL_Y && mouseY < topPos + SCROLL_Y + SCROLL_H) {

            final int totalRows = Mth.ceil(getTotalEntries() / (float) COLS);
            final int maxScroll = Math.max(0, totalRows - ROWS);
            if (maxScroll > 0) {
                int rel = (int) (mouseY - (topPos + SCROLL_Y)) - SCROLL_THUMB_H / 2;
                rel = Mth.clamp(rel, 0, SCROLL_H - SCROLL_THUMB_H);
                float ratio = rel / (float) (SCROLL_H - SCROLL_THUMB_H);
                scrollIndex = Mth.clamp(Math.round(ratio * maxScroll), 0, maxScroll);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int getTotalEntries() {
        return 12;
    }

    private int getScrollPixelOffset(int totalRows) {
        int maxScroll = Math.max(0, totalRows - ROWS);
        if (maxScroll == 0) return 0;
        int track = SCROLL_H - SCROLL_THUMB_H;
        return (int) (track * (scrollIndex / (float) maxScroll));
    }

    private boolean isMouseOverCell(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + CELL_W && mouseY >= y && mouseY < y + CELL_H;
    }

    private void onCellClicked(int globalIndex) {
        if (globalIndex >= 0 && globalIndex < menu.getVisibleRecipes().size()) {
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, globalIndex);
            menu.getPlayer().level().playSound(
                    null,
                    menu.getPlayer().blockPosition(),
                    SoundEvents.UI_STONECUTTER_SELECT_RECIPE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }
    }

}
