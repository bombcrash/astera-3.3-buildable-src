package me.lyrica.modules.impl.visuals;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.NumberSetting;

@RegisterModule(name = "HandProgress", description = "Allows you to modify the hand animation progress and eating animations.", category = Module.Category.VISUALS)
public class HandProgressModule extends Module {


    public BooleanSetting modifyMainhand = new BooleanSetting("ModifyMainhand", "Modifies the mainhand's progress.", true);
    public NumberSetting mainhandProgress = new NumberSetting("MainhandProgress", "Progress", "The progress for the mainhand.", new BooleanSetting.Visibility(modifyMainhand, true), 1.0f, -1.0f, 1.0f);
    public BooleanSetting modifyOffhand = new BooleanSetting("ModifyOffhand", "Modifies the offhand's progress.", true);
    public NumberSetting offhandProgress = new NumberSetting("OffhandProgress", "Progress", "The progress for the offhand.", new BooleanSetting.Visibility(modifyOffhand, true), 1.0f, -1.0f, 1.0f);

    public BooleanSetting staticEating = new BooleanSetting("StaticEating", "Cancel eating animation.", false);
}
