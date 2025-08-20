package me.lyrica.modules.impl.visuals;

import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PlayerUpdateEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.ModeSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

//@RegisterModule(name = "FullBright", description = "Allows you to see in the dark by modifying the game's brightness.", category = Module.Category.VISUALS)
public class FullBrightModule extends Module {
    public static FullBrightModule INSTANCE;
    
    public FullBrightModule() {
        INSTANCE = this;
    }

    public ModeSetting mode = new ModeSetting("Mode", "The way that will be used to change the game's brightness.", "Gamma", new String[]{"Gamma", "Potion"});

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (!mode.getValue().equalsIgnoreCase("Potion")) return;

        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE));
        }
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        if (!mode.getValue().equalsIgnoreCase("Potion")) return;

        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (!mode.getValue().equalsIgnoreCase("Potion")) return;

        if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @Override
    public String getMetaData() {
        return mode.getValue();
    }
}
