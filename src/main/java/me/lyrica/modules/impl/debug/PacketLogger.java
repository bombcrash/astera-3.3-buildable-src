package me.lyrica.modules.impl.debug;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;

@RegisterModule(name = "PacketLogger", description = "Logs all packets.", category = Module.Category.DEBUG)
public class PacketLogger extends Module {
    public PacketLogger() {
    }
}