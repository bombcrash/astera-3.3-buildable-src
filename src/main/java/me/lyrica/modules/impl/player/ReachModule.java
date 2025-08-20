package me.lyrica.modules.impl.player;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.NumberSetting;

@RegisterModule(name = "Reach", description = "Allows you to modify the distance at which you can interact with blocks.", category = Module.Category.PLAYER)
public class ReachModule extends Module {
    public static ReachModule INSTANCE;
    
    public ReachModule() {
        INSTANCE = this;
    }

    public NumberSetting amount = new NumberSetting("Amount", "The maximum distance at which you will be able to interact with blocks.", 6.0, 0.0, 8.0);

    @Override
    public String getMetaData() {
        return String.valueOf(amount.getValue().doubleValue());
    }
}
