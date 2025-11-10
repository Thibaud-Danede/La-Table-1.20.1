// net.ravadael.tablemod.menu.ModMenus
package net.ravadael.tablemod.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import net.ravadael.tablemod.TableMod;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TableMod.MOD_ID);

    public static final RegistryObject<MenuType<AlchemyTableMenu>> ALCHEMY_TABLE_MENU =
            MENUS.register("alchemy_table",
                    () -> IForgeMenuType.create(AlchemyTableMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}

