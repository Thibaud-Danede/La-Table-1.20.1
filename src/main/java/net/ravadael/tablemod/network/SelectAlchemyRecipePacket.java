
package net.ravadael.tablemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.ravadael.tablemod.menu.AlchemyTableMenu;

import java.util.function.Supplier;

public class SelectAlchemyRecipePacket {
    private final int containerId;
    private final int recipeIndex;

    public SelectAlchemyRecipePacket(int containerId, int recipeIndex) {
        this.containerId = containerId;
        this.recipeIndex = recipeIndex;
    }

    public SelectAlchemyRecipePacket(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.recipeIndex = buf.readVarInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(recipeIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu.containerId == containerId &&
                player.containerMenu instanceof AlchemyTableMenu menu) {

                menu.selectRecipeIndex(recipeIndex);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
