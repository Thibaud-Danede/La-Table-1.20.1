package net.ravadael.tablemod.screen;// (optionnel) package com.tontonmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

/**
 * Écran pour l'Alchemy Table – Forge 1.20.1
 * - Texture unique: assets/<modid>/textures/gui/alchemy_table.png (256x256)
 * - Fond: 200x220 depuis (0,0)
 * - Boutons (3 états) dans la même texture:
 *      Normal     : u=0, v=166, w=16, h=18
 *      Sélectionné: u=0, v=184, w=16, h=18
 *      Hover      : u=0, v=202, w=16, h=18
 * - Scrollbar rail: u=176, v=0,  w=12, h=56
 * - Scrollbar thumb: u=188, v=0, w=12, h=15
 */
public class AlchemyTableScreen extends AbstractContainerScreen<AlchemyTableMenu> {

    // ----- A adapter si ton modid est différent -----
    private static final String MODID = "tablemod";
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MODID, "textures/gui/alchemy_table.png");

    // Taille réelle du PNG
    private static final int TEX_W = 256, TEX_H = 256;

    // Fond principal (zone utile du PNG)
    private static final int BG_U = 0, BG_V = 0, BG_W = 200, BG_H = 220;

    // ----- Grille des "recettes"/entrées cliquables -----
    // Position (relative à leftPos/topPos) de la 1re cellule (en haut-gauche)
    private static final int GRID_X = 8;
    private static final int GRID_Y = 18;

    // Taille d'une cellule cliquable (== taille du fond de bouton)
    private static final int CELL_W = 16;
    private static final int CELL_H = 18;

    // Espacements et dimensions de la grille visible
    private static final int GAP_X = 2;
    private static final int GAP_Y = 2;
    private static final int COLS  = 3;   // colonnes visibles
    private static final int ROWS  = 3;   // lignes visibles (donc 9 cellules affichées à la fois)

    // ----- Sprites des 3 états de bouton dans la texture -----
    private static final int BTN_U             = 0;
    private static final int BTN_V_NORMAL      = 166;
    private static final int BTN_V_SELECTED    = 184;
    private static final int BTN_V_HOVER       = 202;

    // ----- Scrollbar (rail dessiné dans la texture + thumb) -----
    private static final int SCROLL_TRACK_U = 176, SCROLL_TRACK_V = 0,   SCROLL_TRACK_W = 12, SCROLL_TRACK_H = 56;
    private static final int SCROLL_THUMB_U = 188, SCROLL_THUMB_V = 0,   SCROLL_THUMB_W = 12, SCROLL_THUMB_H = 15;

    // Position (relative à leftPos/topPos) de la gouttière dans le fond
    private static final int SCROLL_X = 152;
    private static final int SCROLL_Y = 18;
    private static final int SCROLL_H = 54; // hauteur visible de la gouttière (dans ton fond)

    // ----- État UI -----
    // Index de scroll en lignes (0..maxScrollRows)
    private int scrollIndex = 0;
    // Index sélectionné parmi toutes les entrées (linéaire). -1 = rien
    private int selectedIndex = -1;

    public AlchemyTableScreen(AlchemyTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = BG_W;
        this.imageHeight = BG_H;
    }

    // ----------- Rendu -----------
    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Fond
        g.blit(TEXTURE, leftPos, topPos, BG_U, BG_V, BG_W, BG_H, TEX_W, TEX_H);

        // Grille d’entrées (affichage page courante selon scrollIndex)
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
                    // Rien à dessiner pour cette case (hors plage)
                    cellGlobalIndex++;
                    continue;
                }

                boolean hovered  = isMouseOverCell(mouseX, mouseY, x, y);
                boolean selected = (cellGlobalIndex == selectedIndex);

                int v = hovered ? BTN_V_HOVER : (selected ? BTN_V_SELECTED : BTN_V_NORMAL);
                g.blit(TEXTURE, x, y, BTN_U, v, CELL_W, CELL_H, TEX_W, TEX_H);

                // TODO: dessiner l’icône/l’item correspondant à cellGlobalIndex ici si tu veux
                // ex: g.renderItem(stack, x + 1, y + 1);

                cellGlobalIndex++;
            }
        }

        // Scrollbar: rail + thumb
        int sx = leftPos + SCROLL_X;
        int sy = topPos  + SCROLL_Y;

        // Rail (optionnel si déjà dans le fond, mais ici on le dessine)
        g.blit(TEXTURE, sx, sy, SCROLL_TRACK_U, SCROLL_TRACK_V, SCROLL_TRACK_W, SCROLL_TRACK_H, TEX_W, TEX_H);

        // Position du curseur (thumb)
        int thumbOffset = getScrollPixelOffset(totalRows);
        g.blit(TEXTURE, sx, sy + thumbOffset, SCROLL_THUMB_U, SCROLL_THUMB_V, SCROLL_THUMB_W, SCROLL_THUMB_H, TEX_W, TEX_H);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    // ----------- Interactions -----------
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
        // Détection clic sur cellule (alignée exactement avec le rendu)
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
                    onCellClicked(selectedIndex); // TODO: ta logique côté menu/réseau si besoin
                    return true;
                }
                cellGlobalIndex++;
            }
        }

        // Clic dans la gouttière du scroll: repositionne le thumb
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

    // ----------- Helpers -----------

    /** Nombre total d'entrées à afficher. Adapte selon tes données/menu. */
    private int getTotalEntries() {
        // TODO: remplace par la vraie source (par ex. menu.getAvailableRecipes().size())
        // Valeur de secours: 12 pour montrer le scroll
        return 12;
    }

    /** Offset vertical (en pixels) du thumb de scrollbar en fonction du scrollIndex courant. */
    private int getScrollPixelOffset(int totalRows) {
        int maxScroll = Math.max(0, totalRows - ROWS);
        if (maxScroll == 0) return 0;
        int track = SCROLL_H - SCROLL_THUMB_H;
        return (int) (track * (scrollIndex / (float) maxScroll));
    }

    private boolean isMouseOverCell(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + CELL_W && mouseY >= y && mouseY < y + CELL_H;
    }

    /** Appelé quand une cellule (case) est cliquée. Branche ici ta logique. */
    private void onCellClicked(int globalIndex) {
        // TODO: par exemple, envoyer un paquet au serveur pour choisir une recette:
        // menu.choose(globalIndex);  ou  sendToServer(new SelectRecipePacket(globalIndex));
    }
}
