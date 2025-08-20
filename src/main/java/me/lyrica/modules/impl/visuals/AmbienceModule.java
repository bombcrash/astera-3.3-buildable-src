package me.lyrica.modules.impl.visuals;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PacketReceiveEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.ColorSetting;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.utils.color.ColorUtils;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import java.awt.*;

@RegisterModule(name = "Ambience", description = "Changes the ambience of the game", category = Module.Category.VISUALS)
public class AmbienceModule extends Module {
    public static AmbienceModule INSTANCE;
    
    public AmbienceModule() {
        INSTANCE = this;
    }

    // Ayarlar
    public ColorSetting worldColor = new ColorSetting("WorldColor", "The color of the world.", ColorUtils.getDefaultColor());
    public BooleanSetting customTime = new BooleanSetting("CustomTime", "Enable custom time.", false);
    public NumberSetting time = new NumberSetting(
            "Time",
            "The time of day.",
            new BooleanSetting.Visibility(customTime, true),
            0, 0, 24000
    );
    public ColorSetting fog = new ColorSetting("FogColor", "The color of the fog.", ColorUtils.getDefaultOutlineColor());
    public ColorSetting sky = new ColorSetting("SkyColor", "The color of the sky.", new ColorSetting.Color(new Color(0, 0, 0), false, false));
    public BooleanSetting fogDistance = new BooleanSetting("FogDistance", "Enable custom fog distance.", false);
    public NumberSetting fogStart = new NumberSetting(
            "FogStart",
            "Fog start distance.",
            new BooleanSetting.Visibility(fogDistance, true),
            50, 0, 1000
    );
    public NumberSetting fogEnd = new NumberSetting(
            "FogEnd",
            "Fog end distance.",
            new BooleanSetting.Visibility(fogDistance, true),
            100, 0, 1000
    );

    private long oldTime = 0;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (getNull()) return;
        if (customTime.getValue()) {
            // Lyrica'da zaman doğrudan set edilemiyor, Mixin ile getTimeOfDay override ediliyor.
            // AtmosphereModule örneğinde olduğu gibi, burada sadece ayar güncellenir.
            // Zamanı doğrudan değiştirmek için uygun bir yol yoksa, ayar güncellenir ve Mixin okur.
            // Yani burada ekstra bir şey yapmaya gerek yok.
        }
    }

    @Override
    public void onEnable() {
        if (getNull()) return;
        // Zamanı kaydet (Mixin ile okunan değer yoksa 0 bırakılır)
        if (mc.world != null) {
            oldTime = mc.world.getTime();
        }
    }

    @Override
    public void onDisable() {
        if (getNull()) return;
        // Zamanı eski haline döndürmek için uygun bir yol yoksa, burada bir şey yapılmaz.
        // Eğer Mixin ile desteklenirse, eski zamanı bir şekilde geri yazmak gerekebilir.
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event) {
        if (getNull()) return;
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            // Sunucudan gelen zaman güncellemelerini engelle (Mixin ile destekleniyorsa)
            event.setCancelled(true);
        }
    }
}
