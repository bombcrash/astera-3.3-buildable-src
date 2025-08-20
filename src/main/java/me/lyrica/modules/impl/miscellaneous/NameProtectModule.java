package me.lyrica.modules.impl.miscellaneous;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.StringSetting;

@RegisterModule(name = "NameHider", description = "Hides your current in game name.", category = Module.Category.CORE)
public class NameProtectModule extends Module {
    public StringSetting name = new StringSetting("Name", "The name to use as a replacement.", "Tinkoprof");
}
