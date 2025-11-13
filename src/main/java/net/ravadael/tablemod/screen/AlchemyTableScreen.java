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
        //System.out.println("[AlchemyScreen] Recipes to render: " + menu.getCurrentRecipes().size());
        int buttonsPerRow = 4;
        int buttonSize = 18;

        for (int i = 0; i < recipes.size(); i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            int x = leftPos + 60 + col * buttonSize;
            int y = topPos + 10 + row * buttonSize;

            guiGraphics.renderItem(recipes.get(i).getResultItem(minecraft.level.registryAccess()), x, y);
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

        for (int i = 0; i < recipes.size(); i++) {
            int row = i / buttonsPerRow;
            int col = i % buttonsPerRow;
            int x = leftPos + 60 + col * buttonSize;
            int y = topPos + 10 + row * buttonSize;

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {

                menu.clientSelectRecipe(i); // for GUI update
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i); // for server logic
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}