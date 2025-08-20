package me.lyrica.modules.impl.debug;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;

@RegisterModule(name = "ExpectionPatcher", description = "Patches the expection.", category = Module.Category.DEBUG)
public class ExpectionPatcher extends Module {
    public ExpectionPatcher() {
    }
}