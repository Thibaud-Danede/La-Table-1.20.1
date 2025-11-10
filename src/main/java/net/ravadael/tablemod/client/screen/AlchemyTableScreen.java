// net.ravadael.tablemod.client.screen.AlchemyTableScreen
package net.ravadael.tablemod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.ravadael.tablemod.TableMod;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

public class AlchemyTableScreen extends AbstractContainerScreen<AlchemyTableMenu> {
    private static final ResourceLocation TEX = new ResourceLocation(TableMod.MOD_ID, "textures/gui/alchemy_table.png");

    public AlchemyTableScreen(AlchemyTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEX);
        g.blit(TEX, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // barre de progression (ex: 24x16 à la pos 79,34)
        int w = this.menu.getProgressScaled(24);
        g.blit(TEX, this.leftPos + 79, this.topPos + 34, 176, 0, w, 16);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, partial);
        this.renderTooltip(g, mouseX, mouseY);
    }
}
