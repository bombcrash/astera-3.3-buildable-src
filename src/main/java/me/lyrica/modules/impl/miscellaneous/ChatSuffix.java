package me.lyrica.modules.impl.miscellaneous;

import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.ChatInputEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;

@RegisterModule(name = "ChatSuffix", description = "Adds a suffix to your chat messages", category = Module.Category.MISCELLANEOUS)
public class ChatSuffix extends Module {
    private final String SUFFIX = " ⋆ ᴀsᴛᴇʀᴀ";
    private final BooleanSetting ignoreCommands = new BooleanSetting("Ignore Commands", "Don't add suffix to commands", true);

    public ChatSuffix() {
        super();
    }

    @SubscribeEvent
    public void onChat(ChatInputEvent event) {
        String message = event.getMessage();
            
        if (message.startsWith("/") && ignoreCommands.getValue()) {
            return;
        }

        if (!message.endsWith(SUFFIX)) {
            event.setCancelled(true);
            mc.getNetworkHandler().sendChatMessage(message + SUFFIX);
        }
    }
} 