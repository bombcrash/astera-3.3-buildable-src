package me.lyrica.modules.impl.movement;

import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;

@RegisterModule(name = "AutoWalk", description = "Automatically walks at all times.", category = Module.Category.MOVEMENT)
public class AutoWalkModule extends Module {
    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        mc.options.forwardKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) return;
        mc.options.forwardKey.setPressed(false);
    }
}
