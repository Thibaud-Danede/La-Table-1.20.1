package net.ravadael.tablemod.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.ravadael.tablemod.TableMod;
import net.ravadael.tablemod.block.ModBlocks;
import net.ravadael.tablemod.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, TableMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(ModTags.Blocks.METAL_DETECTOR_VALUABLES)
                .add(ModBlocks.RUBY_ORE.get()).addTag(Tags.Blocks.ORES);
        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.RUBY_BLOCK.get());
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.RUBY_BLOCK.get(),
                      ModBlocks.RAW_RUBY.get(),
                      ModBlocks.SOUND_BLOCK.get()
                );

    }
}
