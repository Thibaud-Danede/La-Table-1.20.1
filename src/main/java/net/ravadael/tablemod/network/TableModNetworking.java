
package net.ravadael.tablemod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.ravadael.tablemod.TableMod;

public class TableModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TableMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId() {
        return packetId++;
    }

    public static void register() {
        INSTANCE.registerMessage(
                nextId(),
                SelectAlchemyRecipePacket.class,
                SelectAlchemyRecipePacket::toBytes,
                SelectAlchemyRecipePacket::new,
                SelectAlchemyRecipePacket::handle
        );
    }

    public static void sendToServer(Object msg) {
        INSTANCE.sendToServer(msg);
    }
}
