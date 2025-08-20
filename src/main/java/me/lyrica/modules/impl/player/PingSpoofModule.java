package me.lyrica.modules.impl.player;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PacketReceiveEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.modules.impl.miscellaneous.FastLatencyModule;
import me.lyrica.settings.impl.NumberSetting;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;

import java.util.concurrent.ConcurrentLinkedQueue;

@RegisterModule(name = "PingSpoof", description = "Delays packets to spoof your ping.", category = Module.Category.PLAYER)
public class PingSpoofModule extends Module {
    public NumberSetting delay = new NumberSetting("Delay", "The delay of to send the packets at.", 200, 0, 2000);

    private final ConcurrentLinkedQueue<DelayedPacket> queue = new ConcurrentLinkedQueue<>();

    @Override
    public void onEnable() {
        if (Lyrica.MODULE_MANAGER.getModule(FastLatencyModule.class).isToggled()) setToggled(false);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (getNull() || Lyrica.MODULE_MANAGER.getModule(FastLatencyModule.class).isToggled()) return;

        if (event.getPacket() instanceof KeepAliveS2CPacket packet) {
            event.setCancelled(true);
            queue.add(new DelayedPacket(packet, System.currentTimeMillis()));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (getNull()) return;

        DelayedPacket packet = queue.peek();
        if (packet == null) return;

        if (System.currentTimeMillis() - packet.time() >= delay.getValue().intValue()) {
            mc.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(queue.poll().packet().getId()));
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(delay.getValue().intValue());
    }

    private record DelayedPacket(KeepAliveS2CPacket packet, long time) {}
}
