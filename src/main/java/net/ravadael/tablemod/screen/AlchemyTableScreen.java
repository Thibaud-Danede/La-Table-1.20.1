package net.ravadael.tablemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.ravadael.tablemod.menu.AlchemyTableMenu;
import net.ravadael.tablemod.recipe.AlchemyRecipe;

import java.util.List;

public class AlchemyTableScreen extends AbstractContainerScreen<AlchemyTableMenu> {

    private static final int MAX_VISIBLE_RECIPES = 12;
    private static final int RECIPES_PER_ROW = 4;
    private static final int RECIPE_BUTTON_SIZE = 18;
    private int scrollOffset = 0;
    private boolean isScrolling = false;
    private double scrollAmount = 0;

    private static final ResourceLocation TEXTURE = new ResourceLocation("tablemod", "textures/gui/alchemy_table.png");

    public AlchemyTableScreen(AlchemyTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Render result preview
        List<AlchemyRecipe> recipes = menu.getCurrentRecipes();
        int totalRows = (int) Math.ceil(recipes.size() / (double) RECIPES_PER_ROW);
        int maxScroll = Math.max(0, totalRows - 3); // 3 rows visible (12 recipes)

        if (maxScroll > 0) {
            int scrollbarX = leftPos + 119;
            int scrollbarY = topPos + 15 + (int)(41.0 * scrollOffset / maxScroll);
            guiGraphics.blit(TEXTURE, scrollbarX, scrollbarY, 176, 0, 12, 15);
        } else {
            guiGraphics.blit(TEXTURE, leftPos + 119, topPos + 15, 188, 0, 12, 15);
        }

        //System.out.println("[AlchemyScreen] Recipes to render: " + menu.getCurrentRecipes().size());

        int buttonsPerRow = 4;
        int visibleRows = 3;
        int buttonSize = 18;
        int startIndex = scrollOffset * buttonsPerRow;

        for (int i = 0; i < visibleRows * buttonsPerRow; i++) {
            int index = startIndex + i;
            if (index >= recipes.size()) break;

            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            int x = leftPos + 60 + col * buttonSize;
            int y = topPos + 10 + row * buttonSize;

            guiGraphics.renderItem(recipes.get(index).getResultItem(minecraft.level.registryAccess()), x, y);
        }

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, 8, 6, 4210752, false);
        guiGraphics.drawString(font, playerInventoryTitle, 8, 72, 4210752, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<AlchemyRecipe> recipes = menu.getCurrentRecipes();
        int buttonsPerRow = 4;
        int buttonSize = 18;
        int startIndex = scrollOffset * buttonsPerRow;

        for (int i = 0; i < Math.min(recipes.size() - startIndex, 12); i++) {
            int index = startIndex + i;
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            int x = leftPos + 60 + col * buttonSize;
            int y = topPos + 10 + row * buttonSize;

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                menu.clientSelectRecipe(index); // correct index
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int recipeCount = menu.getCurrentRecipes().size();
        int rows = (int) Math.ceil(recipeCount / (double) RECIPES_PER_ROW);
        int maxScroll = Math.max(0, rows - 3); // 3 visible rows

        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta));
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}





