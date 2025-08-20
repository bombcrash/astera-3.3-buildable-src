package me.lyrica.modules.impl.visuals;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.NumberSetting;

@RegisterModule(name = "Aspect", description = "Modifies the game's aspect ratio.", category = Module.Category.VISUALS)
public class AspectRatioModule extends Module {
    public NumberSetting ratio = new NumberSetting("Ratio", "The aspect ratio that will be applied to the game's rendering.", 1.78f, 0.0f, 5.0f);

    @Override
    public String getMetaData() {
        return String.valueOf(ratio.getValue().floatValue());
    }
}
