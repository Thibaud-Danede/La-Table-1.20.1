package net.ravadael.tablemod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.ravadael.tablemod.block.ModBlocks;
import net.ravadael.tablemod.item.ModCreativeModTabs;
import net.ravadael.tablemod.item.ModItems;
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


}
