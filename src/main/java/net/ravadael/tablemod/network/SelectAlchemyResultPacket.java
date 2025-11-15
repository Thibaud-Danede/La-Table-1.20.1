
package net.ravadael.tablemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

import java.util.function.Supplier;

public class SelectAlchemyResultPacket {
    private final ItemStack selectedOutput;

    public SelectAlchemyResultPacket(ItemStack selectedOutput) {
        this.selectedOutput = selectedOutput;
    }

    public SelectAlchemyResultPacket(FriendlyByteBuf buf) {
        this.selectedOutput = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(selectedOutput);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof AlchemyTableMenu menu) {
                menu.setSelectedOutput(selectedOutput);
            }
        });
        context.setPacketHandled(true);
    }
}
