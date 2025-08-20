package me.lyrica.modules.impl.movement;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.NumberSetting;

@RegisterModule(name = "NoJumpDelay", description = "Allows you to modify the delay between jumps.", category = Module.Category.MOVEMENT)
public class NoJumpDelayModule extends Module {
    public static NoJumpDelayModule INSTANCE;
    
    public NoJumpDelayModule() {
        INSTANCE = this;
    }

    public NumberSetting ticks = new NumberSetting("Ticks", "The amount of ticks that have to be waited for before jumping again.", 1, 0, 20);

    @Override
    public String getMetaData() {
        return String.valueOf(ticks.getValue());
    }
}
