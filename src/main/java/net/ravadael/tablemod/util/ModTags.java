package net.ravadael.tablemod.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.ravadael.tablemod.TableMod;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> METAL_DETECTOR_VALUABLES = tag("metal_detector_valuables");


        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(TableMod.MOD_ID, name));
        }

    }

    public static class Items {

    }
}
