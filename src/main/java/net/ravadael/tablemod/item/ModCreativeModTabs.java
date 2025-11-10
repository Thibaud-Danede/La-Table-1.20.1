package net.ravadael.tablemod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.ravadael.tablemod.TableMod;
import net.ravadael.tablemod.block.ModBlocks;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TableMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> FIRSTUSELESSMOD_TAB = CREATIVE_MOD_TABS.register("tablemod_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.RUBY.get()))
                    .title(Component.translatable("creativetab.tablemod_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        //Ajout des items dans menu secondaire
                        pOutput.accept(ModItems.RUBY.get());
                        pOutput.accept(ModItems.RAW_RUBY.get());
                        pOutput.accept(ModItems.METAL_DETECTOR.get());
                        pOutput.accept(ModItems.RUBY_STAFF.get());

                        //Ajout des blocks dans menu secondaire
                        pOutput.accept(ModBlocks.RUBY_BLOCK.get());
                        pOutput.accept(ModBlocks.RAW_RUBY.get());
                        pOutput.accept(ModBlocks.SOUND_BLOCK.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MOD_TABS.register(eventBus);
    }
}
