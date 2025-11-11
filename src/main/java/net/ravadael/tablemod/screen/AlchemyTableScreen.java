
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

    private static final int BUTTON_WIDTH = 18;
    private static final int BUTTON_HEIGHT = 18;
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
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        menu.refreshRecipeList();

        List<ItemStack> visible = menu.getVisibleRecipes();
        for (int i = 0; i < visible.size(); i++) {
            int row = i / BUTTONS_PER_ROW;
            int col = i % BUTTONS_PER_ROW;
            int x = leftPos + BUTTON_START_X + col * BUTTON_WIDTH;
            int y = topPos + BUTTON_START_Y + row * BUTTON_HEIGHT;

            // Draw background slot (button frame)
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            graphics.blit(TEXTURE, x, y, 176, 72, 18, 18);
            graphics.renderItem(visible.get(i), x + 1, y + 1);
        }

        // Draw scrollbar if needed
        if (menu.canScroll()) {
            int scrollbarX = leftPos + 119;
            int scrollbarY = topPos + 15;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(TEXTURE, scrollbarX, scrollbarY, 176, 0, 12, 56); // track

            float scrollProgress = menu.getTotalRecipeCount() <= MAX_VISIBLE ? 0f :
                    menu.getStartIndex() / (float)(menu.getTotalRecipeCount() - MAX_VISIBLE);
            int thumbY = (int)(scrollProgress * 44);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(TEXTURE, scrollbarX, scrollbarY + thumbY, 188, 0, 12, 15); // thumb
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
            int x = leftPos + BUTTON_START_X + col * BUTTON_WIDTH;
            int y = topPos + BUTTON_START_Y + row * BUTTON_HEIGHT;
            if (mouseX >= x && mouseX < x + BUTTON_WIDTH && mouseY >= y && mouseY < y + BUTTON_HEIGHT) {
                menu.getBlockEntity().setItem(2, visible.get(i).copy());
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
