
package net.ravadael.tablemod.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.ravadael.tablemod.TableMod;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> ALCHEMY_FUEL = ItemTags.create(new ResourceLocation(TableMod.MOD_ID, "alchemy_fuel"));
    }
}
