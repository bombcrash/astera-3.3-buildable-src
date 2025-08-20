package me.lyrica.gui.impl;

import me.lyrica.Lyrica;
import me.lyrica.gui.ClickGuiScreen;
import me.lyrica.gui.api.Button;
import me.lyrica.gui.api.Frame;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.utils.graphics.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

import java.awt.*;

public class BooleanButton extends Button {
    private final BooleanSetting setting;

    public BooleanButton(BooleanSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(setting.getValue())Renderer2D.renderQuad(context.getMatrices(), getX() + getPadding() + 1, getY(), getX() + getWidth() - getPadding() - 1, getY() + getHeight() - 1, ClickGuiScreen.getButtonColor(getY(), 100));
        Lyrica.FONT_MANAGER.drawTextWithShadow(context, (setting.getValue() ? "" : Formatting.GRAY) + setting.getTag(), getX() + getTextPadding() + 1, getY() + 2, Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if(isHovering(mouseX, mouseY) && button == 0) {
            setting.setValue(!setting.getValue());
            playClickSound();
        }
    }
}
