package net.ravadael.tablemod.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.ravadael.tablemod.TableMod;
import net.ravadael.tablemod.block.ModBlocks;
import net.ravadael.tablemod.client.screen.AlchemyTableScreen;
import net.ravadael.tablemod.menu.ModMenus;

@Mod.EventBusSubscriber(modid = TableMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        MenuScreens.register(ModMenus.ALCHEMY_TABLE_MENU.get(), AlchemyTableScreen::new);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.ALCHEMY_TABLE.get(), RenderType.cutout());
    }
}

