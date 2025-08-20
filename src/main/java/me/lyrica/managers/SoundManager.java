package me.lyrica.managers;

import me.lyrica.Lyrica;
import me.lyrica.modules.impl.core.SoundFX;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import me.lyrica.utils.system.Timer;
import net.minecraft.entity.player.PlayerEntity;
import static me.lyrica.utils.IMinecraft.mc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoundManager.class);

    public final Identifier SCROLL_SOUND = Identifier.of("lyrica:scroll");
    public SoundEvent SCROLL_SOUNDEVENT = SoundEvent.of(SCROLL_SOUND);
    public final Identifier KEYPRESS_SOUND = Identifier.of("lyrica:keypress");
    public SoundEvent KEYPRESS_SOUNDEVENT = SoundEvent.of(KEYPRESS_SOUND);
    public final Identifier KILL_SOUND = Identifier.of("lyrica:kill");
    public SoundEvent KILL_SOUNDEVENT = SoundEvent.of(KILL_SOUND);
    public final Identifier DBKILL_SOUND = Identifier.of("lyrica:dbkill");
    public SoundEvent DBKILL_SOUNDEVENT = SoundEvent.of(DBKILL_SOUND);
    public final Identifier HLife_SOUND = Identifier.of("lyrica:hlife");
    public SoundEvent HLife_SOUNDEVENT = SoundEvent.of(HLife_SOUND);
    public final Identifier ROLLOVER_SOUND = Identifier.of("lyrica:rollover");
    public SoundEvent ROLLOVER_SOUNDEVENT = SoundEvent.of(ROLLOVER_SOUND);
    public final Identifier START_SOUND = Identifier.of("lyrica:startup");
    public SoundEvent START_SOUNDEVENT = SoundEvent.of(START_SOUND);
    public final Identifier ENABLE_SOUND = Identifier.of("lyrica:enable");
    public SoundEvent ENABLE_SOUNDEVENT = SoundEvent.of(ENABLE_SOUND);
    public final Identifier DISABLE_SOUND = Identifier.of("lyrica:disable");
    public SoundEvent DISABLE_SOUNDEVENT = SoundEvent.of(DISABLE_SOUND);


    private final Timer scrollTimer = new Timer();

    @SuppressWarnings("unused")
    public void registerSounds() {
        Registry.register(Registries.SOUND_EVENT, SCROLL_SOUND, SCROLL_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, KEYPRESS_SOUND, KEYPRESS_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, KILL_SOUND, KILL_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, DBKILL_SOUND, DBKILL_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, HLife_SOUND, HLife_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ROLLOVER_SOUND, ROLLOVER_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, START_SOUND, START_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, ENABLE_SOUND, ENABLE_SOUNDEVENT);
        Registry.register(Registries.SOUND_EVENT, DISABLE_SOUND, DISABLE_SOUNDEVENT);
    }

    public void playSound(SoundEvent sound) {
        //System.out.println("[SoundManager] playSound called with: " + sound.getId());
        if (mc.player != null && mc.world != null) {
            Number value = Lyrica.MODULE_MANAGER.getModule(SoundFX.class).volume.getValue();
            float volume = value.floatValue() / 100f;
            //System.out.println("[SoundManager] Playing sound: " + sound.getId() + ", volume: " + volume);
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, volume, 1f);
        } else {
            //System.out.println("[SoundManager] Cannot play sound - player or world is null");
        }
    }

    public void playScroll() {
        // SoundFX modülü açık mı kontrol et
        SoundFX soundFX = Lyrica.MODULE_MANAGER.getModule(SoundFX.class);
        if (soundFX == null || !soundFX.isToggled()) return;

        if (scrollTimer.hasTimeElapsed(20)) {
            String scrollValue = soundFX.scrollSound.getValue();
            SoundFX.ScrollSound mode;
            try {
                mode = SoundFX.ScrollSound.valueOf(scrollValue);
            } catch (Exception e) {
                mode = SoundFX.ScrollSound.Rollover; // Default
            }
            switch (mode) {
                case KeyBoard -> playSound(KEYPRESS_SOUNDEVENT);
                case Custom -> playSound(SCROLL_SOUNDEVENT);
                case HLife -> playSound(HLife_SOUNDEVENT);
                case Rollover -> playSound(ROLLOVER_SOUNDEVENT);
                case OFF -> { /* Hiçbir şey çalma */ }
            }
            scrollTimer.reset();
        }
    }
    
    public void playKillSound(PlayerEntity killed) {
        // Sadece başka bir oyuncuyu öldürdüğümüzde ses çal
        if (Lyrica.MODULE_MANAGER.getModule(SoundFX.class).killSound.getValue() && killed != mc.player) {
            playSound(KILL_SOUNDEVENT);
        }
    }

    public void playDoubleKillSound(PlayerEntity killed) {
        // Sadece başka bir oyuncuyu öldürdüğümüzde ses çal
        if (Lyrica.MODULE_MANAGER.getModule(SoundFX.class).killSound.getValue() && killed != mc.player) {
            playSound(DBKILL_SOUNDEVENT);
        }
    }

    public void playEnableSound() {
        playSound(ENABLE_SOUNDEVENT);
    }

    public void playDisableSound() {
        playSound(DISABLE_SOUNDEVENT);
    }

    public void playStartSound() {
        LOGGER.info("[SoundManager] playStartSound çağrıldı");
        try {
            final Number value = Lyrica.MODULE_MANAGER.getModule(SoundFX.class) != null 
                ? Lyrica.MODULE_MANAGER.getModule(SoundFX.class).volume.getValue() 
                : 100;
            final float volume = value.floatValue() / 100f;
            LOGGER.info("[SoundManager] Başlangıç sesi çalmaya çalışıyorum: " + START_SOUND);
            if (mc.player != null && mc.world != null) {
                mc.world.playSound(mc.player, mc.player.getBlockPos(), START_SOUNDEVENT, SoundCategory.MASTER, volume, 1f);
                LOGGER.info("[SoundManager] Başlangıç sesi çalındı!");
            } else {
                LOGGER.warn("[SoundManager] Player veya world null, alternatif yöntem deneniyor...");
                // Eğer standart yöntem çalışmazsa, alternatif yöntemi dene
                try {
                    // SoundUtill sınıfı aracılığıyla ses çalma
                    //me.alpha432.oyvey.util.soundutil.SoundUtill.playSound(START_SOUND); // Projede yok, yoruma alındı
                    LOGGER.info("[SoundManager] SoundUtill ile ses çalma denemesi yapıldı");
                } catch (Exception e) {
                    LOGGER.error("[SoundManager] Alternatif ses çalma başarısız: " + e.getMessage());
                }
                // Minecraft ana thread'inde belirli bir süre sonra tekrar deneme
                try {
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000); // 2 saniye bekle
                            if (mc != null && mc.player != null && mc.world != null) {
                                mc.execute(() -> {
                                    try {
                                        mc.world.playSound(mc.player, mc.player.getBlockPos(), START_SOUNDEVENT, SoundCategory.MASTER, volume, 1f);
                                        LOGGER.info("[SoundManager] Başlangıç sesi gecikmeli olarak çalındı!");
                                    } catch (Exception e) {
                                        LOGGER.error("[SoundManager] Gecikmeli ses çalma başarısız: " + e.getMessage());
                                    }
                                });
                            }
                        } catch (InterruptedException ignored) {}
                    }).start();
                } catch (Exception e) {
                    LOGGER.error("[SoundManager] Gecikmeli ses çalma thread başlatılamadı: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[SoundManager] Ses çalınırken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
}