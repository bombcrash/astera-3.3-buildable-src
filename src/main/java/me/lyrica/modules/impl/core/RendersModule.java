package me.lyrica.modules.impl.core;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.ColorSetting;
import me.lyrica.settings.impl.ModeSetting;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.utils.color.ColorUtils;

import java.awt.*;

@RegisterModule(name = "Visuals", description = "Manages the client world renders.", category = Module.Category.CORE, persistent = true, drawn = false)
public class RendersModule extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "The mode for the place render.", "Fade", new String[]{"Fade", "Shrink"});
    public NumberSetting duration = new NumberSetting("Duration", "The duration for the place render.", 300, 0, 1000);
    public ModeSetting renderMode = new ModeSetting("VisualMode", "The rendering that will be applied to the blocks highlighted.", "Both", new String[]{"Fill", "Outline", "Both"});
    public ColorSetting fillColor = new ColorSetting("FillColor", "The color used for the fill rendering.", new ModeSetting.Visibility(renderMode, "Fill", "Both"), ColorUtils.getDefaultFillColor());
    public ColorSetting outlineColor = new ColorSetting("OutlineColor", "The color used for the outline rendering.", new ModeSetting.Visibility(renderMode, "Outline", "Both"), ColorUtils.getDefaultOutlineColor());

    public Color getColor(String mode, Color color, float scale) {
        if(mode.equalsIgnoreCase("Fade")) return ColorUtils.getColor(color, (int) (color.getAlpha() * scale));
        return color;
    }
}
