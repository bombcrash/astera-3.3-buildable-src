package me.lyrica.utils.minecraft;

import me.lyrica.mixins.accessors.ClientWorldAccessor;
import me.lyrica.utils.IMinecraft;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.*;

public class NetworkUtils implements IMinecraft {
    public static void sendIgnoredPacket(Packet<?> packet) {
        mc.getNetworkHandler().getConnection().send(packet, null, true);
    }

    public static void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        try (PendingUpdateManager pendingUpdateManager = ((ClientWorldAccessor)mc.world).invokeGetPendingUpdateManager().incrementSequence();){
            Packet<ServerPlayPacketListener> packet = packetCreator.predict(pendingUpdateManager.getSequence());
            mc.getNetworkHandler().sendPacket(packet);
        }
    }
}
