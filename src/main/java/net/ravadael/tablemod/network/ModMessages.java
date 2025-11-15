
package net.ravadael.tablemod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.ravadael.tablemod.TableMod;

import java.util.Optional;

public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TableMod.MOD_ID, "messages"),
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
                SelectAlchemyResultPacket.class,
                SelectAlchemyResultPacket::toBytes,
                SelectAlchemyResultPacket::new,
                SelectAlchemyResultPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void sendSelectResult(ItemStack result) {
        INSTANCE.sendToServer(new SelectAlchemyResultPacket(result));
    }
}
