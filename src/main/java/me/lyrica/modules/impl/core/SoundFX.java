package me.lyrica.modules.impl.core;

import me.lyrica.modules.RegisterModule;
import me.lyrica.modules.Module;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.ModeSetting;

@RegisterModule(name = "SoundFX", description = "SoundFX", category = Module.Category.CORE)
public final class SoundFX extends Module {
    public final NumberSetting volume = new NumberSetting("Volume", "Volume of sound effects", 100, 0, 100);
    public final BooleanSetting totem = new BooleanSetting("TotemPop", "Totem pop sound", false);
    public final ModeSetting scrollSound = new ModeSetting(
        "ScrollSound",
        "Scroll sound mode",
        SoundFX.ScrollSound.Custom.name(),
        new String[]{
            SoundFX.ScrollSound.Custom.name(),
            SoundFX.ScrollSound.OFF.name(),
            SoundFX.ScrollSound.KeyBoard.name(),
            SoundFX.ScrollSound.HLife.name(),
            SoundFX.ScrollSound.Rollover.name()
        }
    );
    public final BooleanSetting killSound = new BooleanSetting("KillSound", "Kill sound", true);
    public final BooleanSetting enable = new BooleanSetting("Enable", "Modül açıldığında ses çal", true);
    public final BooleanSetting disable = new BooleanSetting("Disable", "Modül kapandığında ses çal", true);

    @Override
    public void onDisable() {
    }

    public enum ScrollSound {
        Custom, OFF, KeyBoard, HLife, Rollover
    }
}