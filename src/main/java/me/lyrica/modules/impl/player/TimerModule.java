package me.lyrica.modules.impl.player;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.NumberSetting;

@RegisterModule(name = "Timer", description = "Makes your game run at a faster tick speed.", category = Module.Category.PLAYER)
public class TimerModule extends Module {
    public NumberSetting multiplier = new NumberSetting("Multiplier", "The multiplier that will be added to the game's speed.", 1.0f, 0.0f, 20.0f);

    @SubscribeEvent(priority = Integer.MIN_VALUE)
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        Lyrica.WORLD_MANAGER.setTimerMultiplier(multiplier.getValue().floatValue());
    }

    @Override
    public void onEnable() {
        Lyrica.WORLD_MANAGER.setTimerMultiplier(multiplier.getValue().floatValue());
    }

    @Override
    public void onDisable() {
        Lyrica.WORLD_MANAGER.setTimerMultiplier(1.0f);
    }

    @Override
    public String getMetaData() {
        return String.valueOf(multiplier.getValue().floatValue());
    }
}
