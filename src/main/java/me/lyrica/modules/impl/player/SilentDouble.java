package me.lyrica.modules.impl.player;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;

@RegisterModule(name = "SilentDouble", description = "Prevents you from being nbt logged.", category = Module.Category.PLAYER)
public class SilentDouble extends Module {
    public SilentDouble() {
    }
}