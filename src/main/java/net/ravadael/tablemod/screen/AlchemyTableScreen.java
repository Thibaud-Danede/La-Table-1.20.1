
package net.ravadael.tablemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.ravadael.tablemod.TableMod;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

import java.util.List;

public class AlchemyTableScreen extends AbstractContainerScreen<AlchemyTableMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TableMod.MOD_ID, "textures/gui/alchemy_table.png");

    private static final int BUTTON_WIDTH = 16;
    private static final int BUTTON_HEIGHT = 16;
    private static final int BUTTONS_PER_ROW = 3;
    private static final int BUTTONS_PER_COLUMN = 4;
    private static final int MAX_VISIBLE = BUTTONS_PER_ROW * BUTTONS_PER_COLUMN;
    private static final int BUTTON_START_X = 60;
    private static final int BUTTON_START_Y = 17;

    public AlchemyTableScreen(AlchemyTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        menu.refreshRecipeList(); // update recipes if input changed

        // Render scroll bar
        if (menu.canScroll()) {
            int barX = leftPos + 119;
            int barY = topPos + 15;
            graphics.blit(TEXTURE, barX, barY, 176, 0, 12, 56); // track

            float scrollProgress = menu.startIndex / (float)(menu.recipeResults.size() - MAX_VISIBLE);
            int thumbY = (int)(scrollProgress * 44);
            graphics.blit(TEXTURE, barX, barY + thumbY, 188, 0, 12, 15); // thumb
        }

        // Render recipe buttons
        List<ItemStack> visible = menu.getVisibleRecipes();
        for (int i = 0; i < visible.size(); i++) {
            int row = i / BUTTONS_PER_ROW;
            int col = i % BUTTONS_PER_ROW;
            int x = leftPos + BUTTON_START_X + col * (BUTTON_WIDTH + 4);
            int y = topPos + BUTTON_START_Y + row * (BUTTON_HEIGHT + 4);
            graphics.renderItem(visible.get(i), x, y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<ItemStack> visible = menu.getVisibleRecipes();
        for (int i = 0; i < visible.size(); i++) {
            int row = i / BUTTONS_PER_ROW;
            int col = i % BUTTONS_PER_ROW;
            int x = leftPos + BUTTON_START_X + col * (BUTTON_WIDTH + 4);
            int y = topPos + BUTTON_START_Y + row * (BUTTON_HEIGHT + 4);
            if (mouseX >= x && mouseX < x + BUTTON_WIDTH && mouseY >= y && mouseY < y + BUTTON_HEIGHT) {
                // Set the result to slot 2
                menu.getBlockEntity().setItem(2, visible.get(i).copy());
                return true;
            }
        }

        // Handle scroll wheel
        if (menu.canScroll()) {
            double relX = mouseX - (leftPos + 119);
            double relY = mouseY - (topPos + 15);
            if (relX >= 0 && relX < 12 && relY >= 0 && relY < 56) {
                if (button == 0) menu.scrollUp();
                else if (button == 1) menu.scrollDown();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
