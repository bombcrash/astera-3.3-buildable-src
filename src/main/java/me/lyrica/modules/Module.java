package me.lyrica.modules;

import lombok.Getter;
import me.lyrica.Lyrica;
import me.lyrica.events.impl.ToggleModuleEvent;
import me.lyrica.settings.Setting;
import me.lyrica.settings.impl.*;
import me.lyrica.utils.IMinecraft;
import me.lyrica.utils.animations.Animation;
import me.lyrica.utils.animations.Easing;
import me.lyrica.utils.chat.ChatUtils;
import net.minecraft.util.Formatting;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;

@Getter
public abstract class Module implements IMinecraft {
    private final String name, description;
    private final Category category;

    private final boolean persistent;
    private boolean toggled;
    private final List<Setting> settings;

    public BooleanSetting chatNotify;
    public BooleanSetting drawn;
    public BindSetting bind;

    private final Animation animationOffset;

    private static final String CLASS_HASH = calculateClassHash();
    private static String calculateClassHash() {
        try (InputStream is = Module.class.getResourceAsStream("/me/lyrica/modules/Module.class")) {
            if (is == null) return "missing";
            return DigestUtils.sha256Hex(is);
        } catch (Exception e) {
            return "error";
        }
    }

    public Module() {
        RegisterModule annotation = getClass().getAnnotation(RegisterModule.class);

        name = annotation.name();
        description = annotation.description();
        category = annotation.category();
        persistent = annotation.persistent();
        toggled = annotation.toggled();
        settings = new ArrayList<>();
        animationOffset = new Animation(300, Easing.Method.EASE_OUT_CUBIC);

        chatNotify = new BooleanSetting("ChatNotify", "Notifies you in chat whenever the module gets toggled on or off.", true);
        drawn = new BooleanSetting("Drawn", "Renders the module's name on the HUD's module list.", annotation.drawn());
        bind = new BindSetting("Bind", "The keybind that toggles the module on and off.", annotation.bind());

        if (persistent) toggled = true;
        if (toggled) {
            Lyrica.EVENT_HANDLER.subscribe(this);
        }
    }

    public boolean getNull() {
        return (mc.player == null || mc.world == null);
    }

    public void onEnable() {}
    public void onDisable() {}

    public String getMetaData() {
        return "";
    }

    public void setToggled(boolean toggled) {
        setToggled(toggled, true);
    }

    public void setToggled(boolean toggled, boolean notify) {
        if (persistent) return;
        if (toggled == this.toggled) return;

        this.toggled = toggled;
        if (Lyrica.EVENT_HANDLER != null) {
        Lyrica.EVENT_HANDLER.post(new ToggleModuleEvent(this, this.toggled));
        }

        // Tüm modüller için enable/disable sound
        me.lyrica.modules.impl.core.SoundFX soundFX = null;
        if (me.lyrica.Lyrica.MODULE_MANAGER != null) {
            soundFX = me.lyrica.Lyrica.MODULE_MANAGER.getModule(me.lyrica.modules.impl.core.SoundFX.class);
        }
        if (soundFX != null) {
            if (this.toggled && soundFX.enable != null && soundFX.enable.getValue()) {
                if (me.lyrica.Lyrica.SOUND_MANAGER != null) {
                me.lyrica.Lyrica.SOUND_MANAGER.playEnableSound();
                }
            }
        }

        if (this.toggled) {
            animationOffset.setEasing(Easing.Method.EASE_OUT_CUBIC);

            if (notify && chatNotify != null && chatNotify.getValue()) {
                String watermark = "[astera_dev]";
                if (this.toggled) {
                    if (Lyrica.CHAT_MANAGER != null) {
                    Lyrica.CHAT_MANAGER.message(Formatting.GREEN + name, "toggle-" + getName().toLowerCase());
                    }
                } else {
                    if (Lyrica.CHAT_MANAGER != null) {
                    Lyrica.CHAT_MANAGER.message(Formatting.RED + name, "toggle-" + getName().toLowerCase());
                    }
                }
            }

            onEnable();
            if (this.toggled && Lyrica.EVENT_HANDLER != null) Lyrica.EVENT_HANDLER.subscribe(this);
        } else {
            animationOffset.setEasing(Easing.Method.EASE_IN_CUBIC);

            if (Lyrica.EVENT_HANDLER != null) {
            Lyrica.EVENT_HANDLER.unsubscribe(this);
            }
            onDisable();

            // Tüm modüller için disable sound
            if (soundFX != null && soundFX.disable != null && soundFX.disable.getValue()) {
                if (me.lyrica.Lyrica.SOUND_MANAGER != null) {
                me.lyrica.Lyrica.SOUND_MANAGER.playDisableSound();
                }
            }

            if (notify && chatNotify != null && chatNotify.getValue()) {
                String watermark = "[astera_dev]";
                if (this.toggled) {
                    if (Lyrica.CHAT_MANAGER != null) {
                    Lyrica.CHAT_MANAGER.message(Formatting.GREEN + name, "toggle-" + getName().toLowerCase());
                    }
                } else {
                    if (Lyrica.CHAT_MANAGER != null) {
                    Lyrica.CHAT_MANAGER.message(Formatting.RED + name, "toggle-" + getName().toLowerCase());
                    }
                }
            }
        }
    }

    public int getBind() {
        return bind.getValue();
    }

    public void setBind(int bind) {
        this.bind.setValue(bind);
    }

    public BindSetting.BindMode getBindMode() {
        return bind.getBindMode();
    }
    
    public void setBindMode(BindSetting.BindMode bindMode) {
        this.bind.setBindMode(bindMode);
    }

    public void resetValues() {
        for (Setting uncastedSetting : settings) {
            if (uncastedSetting instanceof BooleanSetting setting) setting.resetValue();
            if (uncastedSetting instanceof NumberSetting setting) setting.resetValue();
            if (uncastedSetting instanceof ModeSetting setting) setting.resetValue();
            if (uncastedSetting instanceof StringSetting setting) setting.resetValue();
            if (uncastedSetting instanceof BindSetting setting) setting.resetValue();
            if (uncastedSetting instanceof WhitelistSetting setting) setting.clear();
            if (uncastedSetting instanceof ColorSetting setting) setting.resetValue();
        }
    }

    public Setting getSetting(String name) {
        if (settings == null) return null;
        return settings.stream().filter(s -> s.getName().equalsIgnoreCase(name) && !(s instanceof CategorySetting)).findFirst().orElse(null);
    }

    @Getter
    public enum Category {
        COMBAT("COMBAT"),
        MISCELLANEOUS("MISC"),
        VISUALS("VISUAL"),
        MOVEMENT("MOVEMENT"),
        PLAYER("PLAYER"),
        CORE("CORE"),
        DEBUG("DEBUG");

        private final String name;

        Category(String name) {
            this.name = name;
        }
    }
}
