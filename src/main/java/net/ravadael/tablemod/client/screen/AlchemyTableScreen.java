
package net.ravadael.tablemod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.ravadael.tablemod.TableMod;
import net.ravadael.tablemod.menu.AlchemyTableMenu;
import net.ravadael.tablemod.network.SelectAlchemyRecipePacket;
import net.ravadael.tablemod.network.TableModNetworking;
import net.ravadael.tablemod.recipe.AlchemyRecipe;

import java.util.List;

public class AlchemyTableScreen extends AbstractContainerScreen<AlchemyTableMenu> {

    @Override
    protected void init() {
        super.init();
        this.recipeList = menu.getAvailableRecipes(); // 👈 This is critical
        this.scrollAmount = 0.0F;
    }

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TableMod.MOD_ID, "textures/gui/alchemy_table.png");

    private List<AlchemyRecipe> recipeList;

    public AlchemyTableScreen(AlchemyTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (recipeList == null) return; // Skip rendering if recipe list isn't ready
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        if (recipeList.size() > 15) {
            int scrollbarHeight = 54;
            int handleHeight = 15;
            int maxScroll = Math.max(1, (recipeList.size() + 4) / 5 - 3);
            int scrollY = topPos + 18 + (int)((scrollAmount / maxScroll) * (scrollbarHeight - handleHeight));

            guiGraphics.blit(TEXTURE, leftPos + 119, scrollY, 176, 0, 6, handleHeight);
        }


        this.recipeList = this.menu.getAvailableRecipes();

        int offset = (int)scrollAmount * 5;
        for (int i = 0; i < 15 && i + offset < recipeList.size(); i++) {
            AlchemyRecipe recipe = recipeList.get(i + offset);
            ItemStack resultStack = recipe.getResultItem(minecraft.level.registryAccess());

            int slotX = leftPos + 45 + (i % 5) * 22;
            int slotY = topPos + 20 + (i / 5) * 22;

            guiGraphics.renderItem(resultStack, slotX, slotY);
            guiGraphics.renderItemDecorations(this.font, resultStack, slotX, slotY);

            if (isHovering(slotX, slotY, 16, 16, mouseX, mouseY)) {
                guiGraphics.renderTooltip(this.font, resultStack, mouseX, mouseY);
            }
        }

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.recipeList != null) {
            int offset = (int)scrollAmount * 5;
            for (int i = 0; i < 15 && i + offset < recipeList.size(); i++) {
                int slotX = leftPos + 45 + (i % 5) * 22;
                int slotY = topPos + 20 + (i / 5) * 22;

                if (isHovering(slotX, slotY, 16, 16, mouseX, mouseY)) {
                    TableModNetworking.sendToServer(new SelectAlchemyRecipePacket(menu.containerId, i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private float scrollAmount = 0.0F;
    private boolean scrolling = false;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int rows = (recipeList.size() + 4) / 5; // 5 per row
        int visibleRows = 3; // ~3 visible
        int maxScroll = Math.max(0, rows - visibleRows);

        scrollAmount = (float)Math.max(0, Math.min(scrollAmount - delta * 0.25F, maxScroll));
        return true;
    }

}
