package me.lyrica.gui.api;

import lombok.Getter;
import lombok.Setter;
import me.lyrica.Lyrica;
import me.lyrica.gui.ClickGuiScreen;
import me.lyrica.gui.impl.WhitelistButton;
import me.lyrica.modules.Module;
import me.lyrica.gui.impl.ModuleButton;
import me.lyrica.modules.impl.core.ClickGuiModule;
import me.lyrica.utils.graphics.Renderer2D;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import java.io.InputStream;

@Getter @Setter
public class Frame {
    private final Module.Category category;
    private int x, y, width, height, totalHeight, dragX = 0, dragY = 0, textPadding = 3;
    public boolean open = true, dragging = false;
    private final ArrayList<Button> buttons = new ArrayList<>();
    private String searchText = "";

    private static final String CLASS_HASH = calculateClassHash();
    private static String calculateClassHash() {
        try (InputStream is = Frame.class.getResourceAsStream("/me/lyrica/gui/api/Frame.class")) {
            if (is == null) return "missing";
            return DigestUtils.sha256Hex(is);
        } catch (Exception e) {
            return "error";
        }
    }

    public Frame(Module.Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        for(Module module : Lyrica.MODULE_MANAGER.getModules(category)) buttons.add(new ModuleButton(module, this, height));
    }

    

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(dragging) {
            setX(mouseX - dragX);
            setY(mouseY - dragY);
        }

        this.totalHeight = height;

        boolean hovering = isHovering(mouseX, mouseY);
        Color shadowColor = new Color(0, 0, 0, 120);
        Color guiColor = Lyrica.MODULE_MANAGER.getModule(ClickGuiModule.class).color.getColor();
        Color baseColor = new Color(guiColor.getRed(), guiColor.getGreen(), guiColor.getBlue(), 180);
        Color hoverColor = new Color(Math.min(guiColor.getRed() + 30, 255), Math.min(guiColor.getGreen() + 30, 255), Math.min(guiColor.getBlue() + 30, 255), 220);
        Color borderColor = new Color(guiColor.getRed(), guiColor.getGreen(), guiColor.getBlue(), 180);
        Color fillColor = hovering ? hoverColor : baseColor;

        Renderer2D.renderQuad(context.getMatrices(), x, y, x + width, y + height, fillColor);
        float lineWidth = (float) Lyrica.MODULE_MANAGER.getModule(ClickGuiModule.class).lineWidth.getValue();
        //Renderer2D.renderOutline(context.getMatrices(), x, y, x + width, y + height, borderColor, lineWidth); // KATEGORİ BAŞLIĞI OUTLINE'I KALDIRILDI
        Lyrica.FONT_MANAGER.drawTextWithShadow(context, category.getName(), x + textPadding, y + 2, Color.WHITE);

        if(open) {
            int tempHeight = height;
            
            // Kategori başlığı ile ilk modül arasında ekstra boşluk
            int categoryPadding = 2;
            tempHeight += categoryPadding;
            
            for(Button button : buttons) {
                if (button instanceof me.lyrica.gui.impl.ModuleButton mb) {
                    if (!searchText.isEmpty() && !mb.getModule().getName().toLowerCase().contains(searchText.toLowerCase())) continue;
                }
                tempHeight += button.getHeight();
                if(button instanceof me.lyrica.gui.impl.ModuleButton moduleButton && (moduleButton.isOpen() || moduleButton.getSettingsAnimationProgress() > 0.01f)) {
                    int visibleCount = moduleButton.getVisibleSettingsCount();
                    int idx=0;
                    for(Button b : moduleButton.getButtons()) {
                        if(!b.isVisible()) continue;
                        if(idx++ >= visibleCount) break;
                        tempHeight += b.getHeight();
                    }
                }
            }
            
            totalHeight = height;
            
            // Kategori başlığı ile ilk modül arasında ekstra boşluk
            totalHeight += categoryPadding;
            
            for(Button button : buttons) {
                if (button instanceof me.lyrica.gui.impl.ModuleButton mb) {
                    if (!searchText.isEmpty() && !mb.getModule().getName().toLowerCase().contains(searchText.toLowerCase())) continue;
                }
                button.setX(x);
                button.setY(y + totalHeight);
                button.render(context, mouseX, mouseY, delta);
                totalHeight += button.getHeight();

                if(button instanceof me.lyrica.gui.impl.ModuleButton moduleButton && (moduleButton.isOpen() || moduleButton.getSettingsAnimationProgress() > 0.01f)) {
                    int visibleCount = moduleButton.getVisibleSettingsCount();
                    int idx=0;
                    for(Button b : moduleButton.getButtons()) {
                        b.getSetting().getVisibility().update();
                        b.setVisible(b.getSetting().getVisibility().isVisible());
                        if(!b.isVisible()) continue;
                        if(idx++ >= visibleCount) break;

                        b.setX(x);
                        b.setY(y + totalHeight);
                        b.render(context, mouseX, mouseY, delta);
                        if(b.isHovering(mouseX, mouseY) && Lyrica.CLICK_GUI.getDescriptionFrame().getDescription().isEmpty()) {
                            Lyrica.CLICK_GUI.getDescriptionFrame().setDescription(b.getDescription());
                        }
                        totalHeight += b.getHeight();
                    }
                }
            }
            Renderer2D.renderOutline(context.getMatrices(), x, y + height, x + width, y + totalHeight, borderColor, lineWidth);
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if(isHovering(mouseX, mouseY)) {
            if(button == 0) {
                dragging = true;
                dragX = (int) (mouseX - getX());
                dragY = (int) (mouseY - getY());
            } else if(button == 1) {
                open = !open;
            }
        }

        if(open) {
            // Arama yapılıyorsa sadece görünür modüllere tıklamayı işle
            for(Button b : buttons) {
                if (b instanceof me.lyrica.gui.impl.ModuleButton mb) {
                    // Eğer arama yapılıyorsa ve modül aramaya uymuyor, tıklamayı işleme
                    if (!searchText.isEmpty() && !mb.getModule().getName().toLowerCase().contains(searchText.toLowerCase())) 
                        continue;
                }
                b.mouseClicked(mouseX, mouseY, button);
            }
        }
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }

        // Arama yapılıyorsa sadece görünür modülleri işle
        for(Button b : buttons) {
            if (b instanceof me.lyrica.gui.impl.ModuleButton mb) {
                if (!searchText.isEmpty() && !mb.getModule().getName().toLowerCase().contains(searchText.toLowerCase())) 
                    continue;
            }
            b.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (x <= mouseX && x + width > mouseX) {
            // Önce alt butonlara ilet
            for (Button b : buttons) {
                if (b instanceof me.lyrica.gui.impl.ModuleButton mb) {
                    if (!searchText.isEmpty() && !mb.getModule().getName().toLowerCase().contains(searchText.toLowerCase())) 
                        continue;
                }
                
                if (b.isHovering(mouseX, mouseY)) {
                    b.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                    return;
                }
            }
            // Hiçbiri işlemezse frame scroll
            if (verticalAmount < 0) {
                setY(getY() - Lyrica.MODULE_MANAGER.getModule(ClickGuiModule.class).scrollSpeed.getValue().intValue());
            } else if (verticalAmount > 0) {
                setY(getY() + Lyrica.MODULE_MANAGER.getModule(ClickGuiModule.class).scrollSpeed.getValue().intValue());
            }
        }
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Button b : buttons) {
            if (b instanceof me.lyrica.gui.impl.ModuleButton mb) {
                if (!searchText.isEmpty() && !mb.getModule().getName().toLowerCase().contains(searchText.toLowerCase())) 
                    continue;
            }

            b.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (open) {
            for (Button button : buttons) {
                if (button instanceof me.lyrica.gui.impl.ModuleButton mb) {
                    if (!searchText.isEmpty() && !mb.getModule().getName().toLowerCase().contains(searchText.toLowerCase())) 
                        continue;
                }
                button.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }

    public void charTyped(char chr, int modifiers) {
        if (open) {
            for (Button button : buttons) {
                if (button instanceof me.lyrica.gui.impl.ModuleButton mb) {
                    if (!searchText.isEmpty() && !mb.getModule().getName().toLowerCase().contains(searchText.toLowerCase())) 
                        continue;
                }
                button.charTyped(chr, modifiers);
            }
        }
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return x <= mouseX && y <= mouseY && x + width > mouseX && y + height > mouseY;
    }

    public void setSearchText(String searchText) { this.searchText = searchText == null ? "" : searchText; }
}
