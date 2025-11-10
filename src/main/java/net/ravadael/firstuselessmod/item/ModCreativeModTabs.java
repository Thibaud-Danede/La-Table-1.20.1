package net.ravadael.firstuselessmod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.ravadael.firstuselessmod.FirstUselessMod;
import net.ravadael.firstuselessmod.block.ModBlocks;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FirstUselessMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> FIRSTUSELESSMOD_TAB = CREATIVE_MOD_TABS.register("firstuselessmod_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.RUBY.get()))
                    .title(Component.translatable("creativetab.firstuselessmod_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        //Ajout des items dans menu secondaire
                        pOutput.accept(ModItems.RUBY.get());
                        pOutput.accept(ModItems.RAW_RUBY.get());
                        pOutput.accept(ModItems.METAL_DETECTOR.get());

                        //Ajout des blocks dans menu secondaire
                        pOutput.accept(ModBlocks.RUBY_BLOCK.get());
                        pOutput.accept(ModBlocks.RAW_RUBY.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MOD_TABS.register(eventBus);
    }
}
