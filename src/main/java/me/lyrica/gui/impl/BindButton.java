package me.lyrica.gui.impl;

import me.lyrica.Lyrica;
import me.lyrica.gui.api.Button;
import me.lyrica.gui.api.Frame;
import me.lyrica.settings.impl.BindSetting;
import me.lyrica.utils.input.KeyboardUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class BindButton extends Button {
    private final BindSetting setting;
    private boolean listening = false;

    public BindButton(BindSetting setting, Frame parent, int height) {
        super(setting, parent, height, setting.getDescription());
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Lyrica.FONT_MANAGER.drawTextWithShadow(context, setting.getTag(), getX() + getTextPadding() + 1, getY() + 2, Color.WHITE);

        String bind = listening ? "..." : KeyboardUtils.getKeyName(setting.getValue());
        String modeText = " " + Formatting.DARK_GRAY + "[" + Formatting.GRAY + setting.getBindMode().getName() + Formatting.DARK_GRAY + "]";
        
        Lyrica.FONT_MANAGER.drawTextWithShadow(context, 
            Formatting.GRAY + bind + modeText, 
            getX() + getWidth() - getTextPadding() - 1 - Lyrica.FONT_MANAGER.getWidth(bind + modeText), 
            getY() + 2, 
            Color.WHITE);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if(isHovering(mouseX, mouseY)) {
            if(button == 0) {
                listening = true;
                playClickSound();
            } else if(button == 1) {
                // Shift + Sağ Tık: Bind modunu değiştir
                if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                    
                    // Toggle ve Hold arasında geçiş yap
                    if (setting.getBindMode() == BindSetting.BindMode.TOGGLE) {
                        setting.setBindMode(BindSetting.BindMode.HOLD);
                    } else {
                        setting.setBindMode(BindSetting.BindMode.TOGGLE);
                    }
                    
                playClickSound();
            } else {
                setting.setValue(0);
                }
            }

            if(listening) {
                if (button == 1 || button == 2 || button == 3 || button == 4) {
                    setting.setValue(-button - 1);
                    listening = false;
                }
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if(listening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE) {
                setting.setValue(0);
            } else {
                setting.setValue(keyCode);
            }
            listening = false;
        }
    }
}
