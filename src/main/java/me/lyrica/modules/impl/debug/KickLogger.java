package me.lyrica.modules.impl.debug;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;

@RegisterModule(name = "KickLogger", description = "Logs all kicks.", category = Module.Category.DEBUG)
public class KickLogger extends Module {
    public KickLogger() {
    }
}