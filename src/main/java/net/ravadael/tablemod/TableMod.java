package net.ravadael.tablemod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.ravadael.tablemod.block.ModBlocks;
import net.ravadael.tablemod.block.entity.ModBlockEntities;
import net.ravadael.tablemod.item.ModCreativeModTabs;
import net.ravadael.tablemod.item.ModItems;
import net.ravadael.tablemod.network.ModMessages;
import net.ravadael.tablemod.recipe.ModRecipes;
import net.ravadael.tablemod.menu.ModMenuTypes;

@Mod(TableMod.MOD_ID)
public class TableMod {
    public static final String MOD_ID = "tablemod";

    // (Option conseillée : constructeur sans argument)
    public TableMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Creative tab, items, blocks
        ModCreativeModTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        ModBlockEntities.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
        ModMenuTypes.MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModRecipes.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModMessages.register();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
}
