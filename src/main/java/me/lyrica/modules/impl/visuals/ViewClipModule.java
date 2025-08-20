package me.lyrica.modules.impl.visuals;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.NumberSetting;

@RegisterModule(name = "View", description = "Makes your camera clip through walls and allows you to change the camera's distance from yourself.", category = Module.Category.VISUALS)
public class ViewClipModule extends Module {
    public BooleanSetting extend = new BooleanSetting("Extend", "Changes the distance of the third person camera from yourself.", false);
    public NumberSetting distance = new NumberSetting("Distance", "The distance of the third person camera from your character.", new BooleanSetting.Visibility(extend, true), 4.0f, -50.0f, 50.0f);

    @Override
    public String getMetaData() {
        return extend.getValue() ? String.valueOf(distance.getValue().floatValue()) : "Vanilla";
    }
}
