package net.ravadael.tablemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.ravadael.tablemod.block.custom.AlchemyTableBlock;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

public class AlchemyTableBlockEntity extends BlockEntity implements MenuProvider {

    private final SimpleContainer inventory = new SimpleContainer(3);

    // Nombre de joueurs utilisant LA table
    private int playersUsing = 0;

    // Compteur interne pour jouer le son ambiant
    private int ambientSoundTimer = 0;

    public AlchemyTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_TABLE_BE.get(), pos, state);
    }

    // ============================================================
    //  MÉCANISME PRINCIPAL : appelé par ton Block (use)
    // ============================================================
    public void addUser(Player player) {
        playersUsing++;

        // se lance à 0 si c’est le premier joueur
        ambientSoundTimer = 0;
    }

    public void removeUser(Player player) {
        if (playersUsing > 0)
            playersUsing--;
    }

    public boolean hasUsers() {
        return playersUsing > 0;
    }

    // ============================================================
    //  TICK SERVEUR : toutes les 5 ticks (~0.25s)
    // ============================================================
    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemyTableBlockEntity be) {

        // Si plus aucun joueur n’utilise la table → éteindre
        if (!be.hasUsers()) {
            if (state.getValue(AlchemyTableBlock.LIT)) {
                level.setBlock(pos, state.setValue(AlchemyTableBlock.LIT, false), 3);
            }
            return;
        }

        // S’assurer que la table est allumée
        if (!state.getValue(AlchemyTableBlock.LIT)) {
            level.setBlock(pos, state.setValue(AlchemyTableBlock.LIT, true), 3);
        }

        // ---------------------------------------------------------
        // Jouer un bruit de bougie toutes les 60 ticks (~3 secondes)
        // ---------------------------------------------------------
        be.ambientSoundTimer++;
        if (be.ambientSoundTimer > 60) {
            be.ambientSoundTimer = 0;

            level.playSound(
                    null,
                    pos,
                    SoundEvents.CANDLE_AMBIENT,
                    SoundSource.BLOCKS,
                    0.4F,
                    1.0F
            );
        }
    }

    // ============================================================
    //  Sauvegarde / Chargement
    // ============================================================
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.fromTag(tag.getList("Items", 10));
        playersUsing = tag.getInt("Users");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Items", inventory.createTag());
        tag.putInt("Users", playersUsing);
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    // ============================================================
    //  MENU
    // ============================================================
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AlchemyTableMenu(id, inv, level, worldPosition);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("container.tablemod.alchemy");
    }
}
