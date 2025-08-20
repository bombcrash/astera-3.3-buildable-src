package me.lyrica.modules.impl.core;

import me.lyrica.Lyrica;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.ColorSetting;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.settings.impl.CategorySetting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

@RegisterModule(name = "ClickGui", description = "Allows you to change and interact with the client's modules and settings through a GUI.", category = Module.Category.CORE, drawn = false, bind = GLFW.GLFW_KEY_RIGHT_SHIFT)
public class ClickGuiModule extends Module {
    public BooleanSetting sounds = new BooleanSetting("Sounds", "Plays Minecraft UI sounds when interacting with the client's GUI.", true);
    public BooleanSetting blur = new BooleanSetting("UIBlur", "Whether or not to blur the background behind the GUI.", true);
    public NumberSetting scrollSpeed = new NumberSetting("ScrollSpeed", "The speed at which the scrolling of the frames will be at.", 15, 1, 50);
    public NumberSetting lineWidth = new NumberSetting("LineWidth", "The thickness of the outline in the ClickGUI.", 1.5f, 0.5f, 5.0f);
    public ColorSetting color = new ColorSetting("Color", "The color that will be used in the GUI.", new ColorSetting.Color(new Color(130, 202, 255), true, false));
    public CategorySetting lineCategory = new CategorySetting("Line", "Çizgi ile ilgili ayarlar.");
    public CategorySetting lineGlowCategory = new CategorySetting("LineGlow", "Glow ile ilgili ayarlar.");
    public BooleanSetting line = new BooleanSetting("Line", "Modül kutusunun altına çizgi çizer.", new CategorySetting.Visibility(lineCategory), false);
    public ColorSetting lineColor = new ColorSetting("LineColor", "Çizgi rengi.", new CategorySetting.Visibility(lineCategory), new ColorSetting.Color(new Color(130, 202, 255, 255), true, false));
    public NumberSetting lineThickness = new NumberSetting("LineThickness", "Çizgi kalınlığı", new CategorySetting.Visibility(lineCategory), 2, 1, 8);
    public BooleanSetting lineGlow = new BooleanSetting("LineGlow", "Alt çizgiye glow efekti uygula.", new CategorySetting.Visibility(lineGlowCategory), false);
    public ColorSetting lineGlowColor = new ColorSetting("LineGlowColor", "Glow rengi.", new CategorySetting.Visibility(lineGlowCategory), new ColorSetting.Color(new Color(130, 202, 255, 120), true, false));
    public NumberSetting lineGlowThickness = new NumberSetting("LineGlowThickness", "Glow kalınlığı", new CategorySetting.Visibility(lineGlowCategory), 4, 1, 12);
    public NumberSetting lineGlowBlur = new NumberSetting("LineGlowBlur", "Glow blur (yayılma)", new CategorySetting.Visibility(lineGlowCategory), 6, 1, 16);
    public BooleanSetting moduleLines = new BooleanSetting("BackGround", "Modüllerin arasına çizgi çizer.", true);
    public BooleanSetting plus = new BooleanSetting("Plus", "Modül kutularında sağdaki + simgesini gösterir.", true);

    @Override
    public void onEnable() {
        if (mc.player == null) {
            setToggled(false);
            return;
        }

        mc.setScreen(Lyrica.CLICK_GUI);
    }

    @Override
    public void onDisable() {
        mc.setScreen(null);
    }

    public boolean isRainbow() {
        if(color.isSync()) return Lyrica.MODULE_MANAGER.getModule(ColorModule.class).color.isRainbow();
        return color.isRainbow();
    }
}
