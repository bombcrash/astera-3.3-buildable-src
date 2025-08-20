package me.lyrica.gui;

import lombok.Getter;
import lombok.Setter;
import me.lyrica.Lyrica;
import me.lyrica.gui.api.DescriptionFrame;
import me.lyrica.modules.Module;
import me.lyrica.modules.impl.core.ClickGuiModule;
import me.lyrica.gui.api.Button;
import me.lyrica.gui.api.Frame;
import me.lyrica.utils.color.ColorUtils;
import me.lyrica.utils.graphics.Renderer2D;
import me.lyrica.utils.system.Timer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import me.lyrica.utils.animations.Animation;
import me.lyrica.utils.animations.Easing;
import com.mojang.blaze3d.systems.RenderSystem;

import java.awt.*;
import java.util.ArrayList;

@Getter @Setter
public class ClickGuiScreen extends Screen {
    private final ArrayList<Frame> frames = new ArrayList<>();
    private final ArrayList<Button> buttons = new ArrayList<>();
    private final DescriptionFrame descriptionFrame;

    private final Timer lineTimer = new Timer();
    private boolean showLine = false;
    private Color colorClipboard = null;

    private String searchText = "";
    private boolean searchBarFocused = false;

    /* Fade-in animasyonu */
    private Animation fadeInAnim = new Animation(0, 1, 150, Easing.Method.EASE_OUT_QUAD);

    public ClickGuiScreen() {
        super(Text.literal(Lyrica.MOD_ID + "-click-gui"));

        int x = 6;
        for(Module.Category category : Module.Category.values()) {
            frames.add(new Frame(category, x, 5, 110, 15));
            x += 111;
        }

        this.descriptionFrame = new DescriptionFrame(x, 3, 200, 13);
    }

    @Override
    protected void init() {
        super.init();
        // GUI açılırken animasyonu sıfırla
        fadeInAnim = new Animation(0, 1, 300, Easing.Method.EASE_OUT_QUAD);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        float guiAlpha = fadeInAnim.get();

        if (lineTimer.hasTimeElapsed(400L)){
            showLine = !showLine;
            lineTimer.reset();
        }

        for (Frame frame : frames) frame.setSearchText(searchText);

        for(Frame frame : frames) frame.render(context, mouseX, mouseY, delta);

        // Search bar'ı Debug kategorisinin en altına çiz
        Frame debugFrame = frames.stream().filter(f -> f.getCategory() == me.lyrica.modules.Module.Category.DEBUG).findFirst().orElse(null);
        if (debugFrame != null) {
            int barWidth = debugFrame.getWidth() - 8;
            int barHeight = 14;
            int barX = debugFrame.getX() + 4;
            int barY = debugFrame.getY() + debugFrame.getTotalHeight() + 6;
            Renderer2D.renderQuad(context.getMatrices(), barX, barY, barX + barWidth, barY + barHeight, new Color(30, 30, 40, 220));
            Renderer2D.renderOutline(context.getMatrices(), barX, barY, barX + barWidth, barY + barHeight, Color.WHITE, 1.0f);
            String shown = searchText.isEmpty() && !searchBarFocused ? "Search..." : searchText;
            Color textColor = searchText.isEmpty() && !searchBarFocused ? Color.GRAY : Color.WHITE;
            Lyrica.FONT_MANAGER.drawTextWithShadow(context, shown, barX + 4, barY + 2, textColor);
        }

        // Ekranı karartan overlay: açılışta %100 siyah, 300ms sonunda şeffaf
        int overlayAlpha = (int) ((1f - guiAlpha) * 255f);
        if (overlayAlpha > 0) {
            Renderer2D.renderQuad(context.getMatrices(), 0, 0, this.width, this.height, new Color(0, 0, 0, overlayAlpha));
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for(Frame frame : frames) frame.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Search bar'a tıklama kontrolü
        Frame debugFrame = frames.stream().filter(f -> f.getCategory() == me.lyrica.modules.Module.Category.DEBUG).findFirst().orElse(null);
        if (debugFrame != null) {
            int barWidth = debugFrame.getWidth() - 8;
            int barHeight = 14;
            int barX = debugFrame.getX() + 4;
            int barY = debugFrame.getY() + debugFrame.getTotalHeight() + 6;
            if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY && mouseY <= barY + barHeight) {
                searchBarFocused = true;
                return true;
            } else {
                searchBarFocused = false;
            }
        }
        for (Frame frame : frames) {
            frame.mouseClicked(mouseX, mouseY, button);
        }

        descriptionFrame.mouseClicked(mouseX, mouseY, button);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) {
            frame.mouseReleased(mouseX, mouseY, button);
        }

        descriptionFrame.mouseReleased(mouseX, mouseY, button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Frame frame : frames) {
            frame.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBarFocused) {
            if (keyCode == 259 && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                for (Frame frame : frames) frame.setSearchText(searchText);
                return true;
            }
        }
        for (Frame frame : frames) {
            frame.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchBarFocused) {
            if (chr >= 32 && chr != 127) {
                searchText += chr;
                for (Frame frame : frames) frame.setSearchText(searchText);
            }
            return true;
        }
        for (Frame frame : frames) {
            frame.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if(Lyrica.MODULE_MANAGER.getModule(ClickGuiModule.class).blur.getValue()) applyBlur();
        Renderer2D.renderQuad(context.getMatrices(), 0, 0, this.width, this.height, new Color(0, 0, 0, 100));
    }

    @Override
    public void close() {
        super.close();
        Lyrica.MODULE_MANAGER.getModule(ClickGuiModule.class).setToggled(false);
        searchText = "";
        searchBarFocused = false;
        for (Frame frame : frames) frame.setSearchText("");
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public static Color getButtonColor(int index, int alpha) {
        Color color = Lyrica.MODULE_MANAGER.getModule(ClickGuiModule.class).isRainbow() ? ColorUtils.getOffsetRainbow(index*10L) : Lyrica.MODULE_MANAGER.getModule(ClickGuiModule.class).color.getColor();
        return ColorUtils.getColor(color, alpha);
    }
}
