package me.lyrica.modules.impl.miscellaneous;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;

@RegisterModule(name = "MouseFix", description = "Fixes multiple mouse issues.", category = Module.Category.MISCELLANEOUS)
public class MouseFixModule extends Module {
    public BooleanSetting customDebounce = new BooleanSetting("CustomDebounce", "Implements a custom debounce timer on mouse inputs.", true);
}
