package me.lyrica.managers;

import lombok.Getter;
import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.KeyInputEvent;
import me.lyrica.events.impl.MouseInputEvent;
import me.lyrica.utils.IMinecraft;
import me.lyrica.utils.chat.ChatUtils;
import me.lyrica.utils.input.KeyboardUtils;

import java.util.HashMap;

@Getter
public class    MacroManager implements IMinecraft {
    private final HashMap<String, Integer> macros;

    public MacroManager() {
        macros = new HashMap<>();
        Lyrica.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (mc.player == null || mc.world == null) return;

        for (String key : macros.keySet()) {
            int value = macros.get(key);
            if (event.getKey() != value) continue;

            if (key == null) {
                Lyrica.CHAT_MANAGER.error("An error happened while executing the " + ChatUtils.getPrimary() + KeyboardUtils.getKeyName(value) + ChatUtils.getSecondary() + " macro.");
                continue;
            }

            String[] split = key.split(";");
            for (String str : split) {
                if (str.startsWith("/")) {
                    mc.player.networkHandler.sendChatCommand(str.substring(1));
                } else {
                    if (str.startsWith(Lyrica.COMMAND_MANAGER.getPrefix())) {
                        Lyrica.COMMAND_MANAGER.execute(str);
                    } else {
                        mc.player.networkHandler.sendChatMessage(str);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseInputEvent event) {
        if (mc.player == null || mc.world == null) return;

        for (String key : macros.keySet()) {
            int value = macros.get(key);
            if (value != (-event.getButton() - 1)) continue;

            if (key == null) {
                Lyrica.CHAT_MANAGER.error("An error happened while executing the " + ChatUtils.getPrimary() + KeyboardUtils.getKeyName(value) + ChatUtils.getSecondary() + " macro.");
                continue;
            }

            if (key.startsWith("/")) {
                mc.player.networkHandler.sendChatCommand(key.substring(1));
            } else {
                mc.player.networkHandler.sendChatMessage(key);
            }
        }
    }

    public String getKey(int value) {
        for (String key : macros.keySet()) {
            if (value == macros.get(key)) {
                return key;
            }
        }

        return null;
    }

    public int getValue(String key) {
        return macros.get(key);
    }

    public boolean containsKey(String key) {
        return macros.containsKey(key);
    }

    public boolean containsValue(int key) {
        return macros.containsValue(key);
    }

    public void add(String key, int value) {
        macros.put(key, value);
    }

    public void remove(String key) {
        macros.remove(key);
    }

    public void clear() {
        macros.clear();
    }
}