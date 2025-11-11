
package net.ravadael.tablemod.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ravadael.tablemod.TableMod;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, TableMod.MOD_ID);

    public static final RegistryObject<MenuType<AlchemyTableMenu>> ALCHEMY_TABLE_MENU =
            MENU_TYPES.register("alchemy_table_menu", () ->
                    IForgeMenuType.create((windowId, inv, buf) ->
                            new AlchemyTableMenu(windowId, inv, buf)
                    )
            );


}
