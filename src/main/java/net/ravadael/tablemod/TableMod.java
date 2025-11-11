package net.ravadael.tablemod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.ravadael.tablemod.block.ModBlocks;
import net.ravadael.tablemod.block.entity.ModBlockEntities;
import net.ravadael.tablemod.client.screen.AlchemyTableScreen;
import net.ravadael.tablemod.item.ModCreativeModTabs;
import net.ravadael.tablemod.item.ModItems;
import net.ravadael.tablemod.menu.ModMenus;
import net.ravadael.tablemod.recipe.ModRecipeTypes;
import org.slf4j.Logger;

@Mod(TableMod.MOD_ID)
public class TableMod {
    public static final String MOD_ID = "tablemod";
    private static final Logger LOGGER = LogUtils.getLogger();

    // (Option conseillée : constructeur sans argument)
    public TableMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Creative tab, items, blocks
        ModCreativeModTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        // ⬇️ AJOUTS IMPORTANTS POUR L’ALCHEMY TABLE
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus); // BlockEntity (inventaire + logique)
        ModMenus.MENUS.register(modEventBus);                  // Menu (container serveur)
        ModRecipeTypes.SERIALIZERS.register(modEventBus);      // Recette custom
        ModRecipeTypes.TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // si tu as des PacketHandlers / setup serveur, mets-les ici
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // si tu ajoutes tes items/blocs dans tes tabs custom, fais-le ici
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // hooks serveur si besoin
    }

}
