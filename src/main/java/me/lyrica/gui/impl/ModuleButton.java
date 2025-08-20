package me.lyrica.gui.impl;

import lombok.Getter;
import lombok.Setter;
import me.lyrica.Lyrica;
import me.lyrica.gui.ClickGuiScreen;
import me.lyrica.modules.Module;
import me.lyrica.gui.api.Button;
import me.lyrica.gui.api.Frame;
import me.lyrica.managers.SoundManager;
import me.lyrica.modules.impl.core.SoundFX;
import me.lyrica.settings.Setting;
import me.lyrica.settings.impl.*;
import me.lyrica.utils.graphics.Renderer2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import me.lyrica.utils.animations.Animation;
import me.lyrica.utils.animations.Easing;

import java.awt.*;
import java.util.ArrayList;

@Getter @Setter
public class ModuleButton extends Button {
    private final Module module;
    private boolean open = false;
    private boolean hovered, prevHovered;
    private final ArrayList<Button> buttons = new ArrayList<>();
    private final Animation settingsAnimation = new Animation(0, 0, 150, Easing.Method.EASE_IN_OUT_CUBIC);

    public ModuleButton(Module module, Frame parent, int height) {
        super(parent, height, module.getDescription());
        this.module = module;

        for(Setting setting : module.getSettings()) {
            if(setting instanceof BooleanSetting s) {
                buttons.add(new BooleanButton(s, parent, height));
            } else if(setting instanceof NumberSetting s) {
                buttons.add(new NumberButton(s, parent, height));
            } else if(setting instanceof CategorySetting s) {
                buttons.add(new CategoryButton(s, parent, height));
            } else if(setting instanceof BindSetting s) {
                buttons.add(new BindButton(s, parent, height));
            } else if(setting instanceof ModeSetting s) {
                buttons.add(new ModeButton(s, parent, height));
            } else if(setting instanceof WhitelistSetting s) {
                buttons.add(new WhitelistButton(s, parent, height));
            } else if(setting instanceof StringSetting s) {
                buttons.add(new StringButton(s, parent, height));
            } else if(setting instanceof ColorSetting s) {
                buttons.add(new ColorButton(s, parent, height));
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(this.isHovering(mouseX, mouseY) && Lyrica.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty())
            Lyrica.CLICK_GUI.getDescriptionFrame().setDescription(this.getDescription());

        boolean wasHovered = hovered;
        hovered = this.isHovering(mouseX, mouseY);

        boolean toggled = module.isToggled();
        Color guiColor = me.lyrica.Lyrica.MODULE_MANAGER.getModule(me.lyrica.modules.impl.core.ClickGuiModule.class).color.getColor();
        Color shadowColor = new Color(0, 0, 0, 100);
        Color baseColor = new Color(40, 40, 60, 180);
        Color hoverColor = new Color(guiColor.getRed(), guiColor.getGreen(), guiColor.getBlue(), 120);
        Color toggledColor = new Color(guiColor.getRed(), guiColor.getGreen(), guiColor.getBlue(), 220);
        Color borderColor = new Color(guiColor.getRed(), guiColor.getGreen(), guiColor.getBlue(), 120);
        Color fillColor = toggled ? toggledColor : (hovered ? hoverColor : baseColor);

        // moduleLines kontrolü: Yalnızca ayar açıkken çiz
        me.lyrica.modules.impl.core.ClickGuiModule mioGui = me.lyrica.Lyrica.MODULE_MANAGER.getModule(me.lyrica.modules.impl.core.ClickGuiModule.class);
        if (mioGui != null && mioGui.moduleLines != null && mioGui.moduleLines.getValue()) {
            // Sadece moduleLines açıkken: gölge, çizgi, glow ve fillColor ile arka plan
            Renderer2D.renderQuad(context.getMatrices(), getX() + getPadding() + 2, getY() + 3, getX() + getWidth() - getPadding() + 2, getY() + getHeight() + 2, shadowColor);
            if (mioGui.line.getValue()) {
                Color lineColor = mioGui.lineColor.getColor();
                int lineThickness = mioGui.lineThickness.getValue().intValue();
                float lineY = getY() + getHeight() - lineThickness;
                float startX = getX() + 4;
                float endX = getX() + getWidth() - 4;
                Renderer2D.renderQuad(context.getMatrices(), startX, lineY, endX, lineY + lineThickness, lineColor);
            }
            if (mioGui.lineGlow.getValue()) {
                Color glowColor = mioGui.lineGlowColor.getColor();
                int glowThickness = mioGui.lineGlowThickness.getValue().intValue();
                int blur = mioGui.lineGlowBlur.getValue().intValue();
                float lineY = getY() + getHeight() - mioGui.lineThickness.getValue().intValue();
                float startX = getX() + 4;
                float endX = getX() + getWidth() - 4;
                float[] kernel = gaussianKernel(blur);
                for (int i = -blur; i <= blur; i++) {
                    int alpha = (int)(glowColor.getAlpha() * kernel[Math.abs(i)]);
                    Color stepColor = new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), alpha);
                    Renderer2D.renderQuad(context.getMatrices(), startX, lineY + i, endX, lineY + i + glowThickness, stepColor);
                }
            }
            // Sadece fillColor ile arka plan (aktif/pasif ayrımı fillColor ile)
            Renderer2D.renderQuad(context.getMatrices(), getX() + getPadding(), getY(), getX() + getWidth() - getPadding(), getY() + getHeight() - 1, fillColor);
        } else {
            // moduleLines kapalıysa: Sadece aktif modülün arka planı çizilsin
            if (toggled) {
                Renderer2D.renderQuad(context.getMatrices(), getX() + getPadding(), getY(), getX() + getWidth() - getPadding(), getY() + getHeight() - 1, toggledColor);
            }
        }

        Lyrica.FONT_MANAGER.drawTextWithShadow(context, (toggled ? "" : net.minecraft.util.Formatting.GRAY ) + module.getName(), getX() + getTextPadding(), getY() + 2, Color.WHITE);

        if (hovered && !wasHovered) {
            Lyrica.SOUND_MANAGER.playScroll();
        }

        // Sağda + veya - simgesi
        if (mioGui != null && mioGui.plus != null && mioGui.plus.getValue()) {
        String symbol = open ? "-" : "+";
        int symbolWidth = Lyrica.FONT_MANAGER.getWidth(symbol);
        int symbolX = getX() + getWidth() - getPadding() - symbolWidth - 4;
        int symbolY = getY() + 2;
        Lyrica.FONT_MANAGER.drawTextWithShadow(context, symbol, symbolX, symbolY, Color.WHITE);
        }

        // Animasyon ilerlemesi
        settingsAnimation.get(); // sadece animasyonu güncel tut
        // Hover edilmiş alt buton açıklamasını Frame tetikleyecek.
        prevHovered = hovered;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if(isHovering(mouseX, mouseY)) {
            if(button == 0) {
                module.setToggled(!module.isToggled());
                playClickSound();
            } else if(button == 1) {
                open = !open;
                settingsAnimation.get(open ? 1 : 0);
                playClickSound();
            }
        }

        if(open) {
            int visibleCount = getVisibleSettingsCount();
            int idx = 0;
            for(Button b : buttons) {
                if(!b.isVisible()) continue;
                if(idx++ >= visibleCount) break;
                b.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        int visibleCountR = getVisibleSettingsCount();
        int idxR = 0;
        for(Button b : buttons) {
            if(!b.isVisible()) continue;
            if(idxR++ >= visibleCountR) break;
            b.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int visibleCountD = getVisibleSettingsCount();
        int idxD = 0;
        for (Button b : buttons) {
            if(!b.isVisible()) continue;
            if(idxD++ >= visibleCountD) break;
            b.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if(open) {
            int visibleCountK = getVisibleSettingsCount();
            int idxK = 0;
            for(Button b : buttons) {
                if(!b.isVisible()) continue;
                if(idxK++ >= visibleCountK) break;
                b.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if(open) {
            int visibleCountC = getVisibleSettingsCount();
            int idxC = 0;
            for(Button b : buttons) {
                if(!b.isVisible()) continue;
                if(idxC++ >= visibleCountC) break;
                b.charTyped(chr, modifiers);
            }
        }
    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (open) {
            int visibleCountS = getVisibleSettingsCount();
            int idxS = 0;
            for (Button b : buttons) {
                if(!b.isVisible()) continue;
                if(idxS++ >= visibleCountS) break;
                if (b.isHovering(mouseX, mouseY)) {
                    b.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                    return;
                }
            }
        }
    }

    // Gaussian kernel fonksiyonu
    private static float[] gaussianKernel(int radius) {
        float[] kernel = new float[radius + 1];
        float sigma = radius / 2.0f;
        float sum = 0;
        for (int i = 0; i <= radius; i++) {
            kernel[i] = (float)Math.exp(-0.5 * (i * i) / (sigma * sigma));
            sum += (i == 0 ? kernel[i] : kernel[i] * 2);
        }
        for (int i = 0; i <= radius; i++) {
            kernel[i] /= sum;
        }
        return kernel;
    }

    // Yeni: Frame tarafında kullanılmak üzere görünür ayar sayısını al
    public int getVisibleSettingsCount() {
        long visibleTotal = buttons.stream().filter(Button::isVisible).count();
        if (visibleTotal == 0) return 0;
        float progress = settingsAnimation.get();
        return (int) Math.ceil(visibleTotal * progress);
    }

    // Yeni: animasyon progress'i dışarıya aç
    public float getSettingsAnimationProgress() {
        return settingsAnimation.get();
    }
}
