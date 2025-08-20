package me.lyrica.modules.impl.core;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.SettingChangeEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.ModeSetting;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import net.minecraft.client.MinecraftClient;

@RegisterModule(name = "RichPresence", description = "Enabled the discord presence for the client.", category = Module.Category.CORE)
public class RPCModule extends Module {
    private final RichPresence rpc = new RichPresence();
    private int ticks = 0;

    // Default settings
    public BooleanSetting showUid = new BooleanSetting("Show UID", "Show UID in Discord Rich Presence", true);
    public BooleanSetting showPlayerName = new BooleanSetting("Show Player Name", "Show player name in Discord Rich Presence", true);
    public BooleanSetting showServer = new BooleanSetting("Show Server", "Show server in Discord Rich Presence", true);
    
    // Image settings
    public ModeSetting largeImage = new ModeSetting("Large Image", "Select large image asset", "asterabig", new String[]{"asterabig", "cat1", "cat2", "cat3", "dog1", "godmodule-dog", "idk1", "spiderman1"});
    public ModeSetting smallImage = new ModeSetting("Small Image", "Select small image asset", "safellc", new String[]{"safellc", "small1", "small2", "small3"});

    public RPCModule() {
        super();
    }

    @Override
    public void onEnable() {
        DiscordIPC.start(1389689035903209663L, null);
        rpc.setStart(Lyrica.UPTIME/1000);
        updatePresence();
    }

    @Override
    public void onDisable() {
        DiscordIPC.stop();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if(ticks > 0) {
            ticks--;
            return;
        }
        updatePresence();
        ticks = 200;
    }


    private void updatePresence() {
        StringBuilder details = new StringBuilder();
        if (showUid.getValue()) {
        }
        if (showServer.getValue()) {
            String server = "Singleplayer";
            try {
                if (MinecraftClient.getInstance().getCurrentServerEntry() != null &&
                        MinecraftClient.getInstance().getCurrentServerEntry().address != null) {
                    server = MinecraftClient.getInstance().getCurrentServerEntry().address;
                }
            } catch (Exception ignored) {
            }
            details.append("Server: ").append(server).append("\n");  //better
        }
        rpc.setDetails(details.toString().trim());

    }
}
