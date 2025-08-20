package me.lyrica.modules.impl.miscellaneous;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.NumberSetting;

@RegisterModule(name = "CustomFOV", description = "Gives you more customizability for the games FOV.", category = Module.Category.MISCELLANEOUS)
public class FOVModifierModule extends Module {
    public NumberSetting fov = new NumberSetting("FOV", "The FOV you want to use.", 120, 50, 150);
    public BooleanSetting items = new BooleanSetting("Items", "Modify items FOV as well.", false);

    @Override
    public String getMetaData() {
        return fov.getValue().intValue() + "";
    }
}
