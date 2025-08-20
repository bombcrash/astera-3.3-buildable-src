package me.lyrica.modules.impl.miscellaneous;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.settings.impl.StringSetting;
import me.lyrica.utils.system.FileUtils;
import me.lyrica.utils.system.MathUtils;
import me.lyrica.utils.system.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RegisterModule(name = "Spammer", description = "Spams messages in chat from a text file.", category = Module.Category.MISCELLANEOUS)
public class SpammerModule extends Module {
    public StringSetting fileName = new StringSetting("FileName", "The name of the spammer text file.", "spammer.txt");
    public NumberSetting delay = new NumberSetting("Delay", "The delay for the announcer.", 5, 0, 30);
    public BooleanSetting greenText = new BooleanSetting("GreenText", "Makes your message green.", false);
    public BooleanSetting shuffled = new BooleanSetting("Shuffled", "Sends the spammer messages out of order.", false);

    private final Timer timer = new Timer();
    private List<String> messages = new ArrayList<>();
    private int line;

    @Override
    public void onEnable() {
        line = 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if(getNull()) return;

        File file = new File(Lyrica.MOD_NAME + "/Client/" + fileName.getValue());
        messages = FileUtils.readLines(file);

        if(!messages.isEmpty() && timer.hasTimeElapsed(delay.getValue().intValue() * 1000)) {
            if(line >= messages.size()) line = 0;

            String message = shuffled.getValue() ? messages.get((int) MathUtils.random(messages.size(), 0)) : messages.get(line);

            mc.player.networkHandler.sendChatMessage((greenText.getValue() ? "> " : "") + message);
            line++;
            timer.reset();
        }
    }
}
