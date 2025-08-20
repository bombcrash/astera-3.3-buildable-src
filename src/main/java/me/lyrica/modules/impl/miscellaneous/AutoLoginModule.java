package me.lyrica.modules.impl.miscellaneous;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PacketReceiveEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.StringSetting;
import me.lyrica.utils.system.Timer;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

@RegisterModule(name = "AutoLogin", description = "Automatically logs in on cracked servers.", category = Module.Category.MISCELLANEOUS)
public class AutoLoginModule extends Module {
    public StringSetting password = new StringSetting("Password", "The password to use when logging in.", "password");

    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if(getNull() || !timer.hasTimeElapsed(10000)) return;

        if(event.getPacket() instanceof GameMessageS2CPacket packet) {
            String s = packet.content().getString().toLowerCase();

            if(s.contains("/register")) {
                mc.getNetworkHandler().sendChatCommand("register " + password.getValue() + " " + password.getValue());
                Lyrica.CHAT_MANAGER.tagged("Registered successfully.", getName());
                timer.reset();
            } else if(s.contains("/login")) {
                mc.getNetworkHandler().sendChatCommand("login " + password.getValue());
                Lyrica.CHAT_MANAGER.tagged("Logged in as " + mc.getSession().getUsername() + ".", getName());
                timer.reset();
            }
        }
    }
}
