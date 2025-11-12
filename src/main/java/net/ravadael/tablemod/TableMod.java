package net.ravadael.tablemod;

import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;
import net.ravadael.tablemod.menu.ModMenuTypes;

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
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        ModBlockEntities.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
        ModMenuTypes.MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
}
