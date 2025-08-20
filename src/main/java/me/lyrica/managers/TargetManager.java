package me.lyrica.managers;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.ClientConnectEvent;
import me.lyrica.events.impl.PlayerDeathEvent;
import me.lyrica.events.impl.TargetDeathEvent;
import me.lyrica.events.impl.TickEvent;
import me.lyrica.modules.impl.combat.KillAuraModule;
import me.lyrica.modules.impl.combat.AutoCrystalModule;
import me.lyrica.utils.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;

public class TargetManager implements IMinecraft {
    private final ArrayList<Target> targets = new ArrayList<>();

    public TargetManager() {
        Lyrica.EVENT_HANDLER.subscribe(this);
    }

    @SubscribeEvent
    public void onClientConnect(ClientConnectEvent event) {
        targets.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if(mc.player == null || mc.world == null) return;

        PlayerEntity caTarget = Lyrica.MODULE_MANAGER.getModule(AutoCrystalModule.class).getTarget();
        Entity kaTarget = Lyrica.MODULE_MANAGER.getModule(KillAuraModule.class).target;

        synchronized (targets) {
            targets.removeIf(t -> System.currentTimeMillis() - t.time > 15000); // Remove targets if 15 seconds since last time they were targeted has passed

            if(caTarget != null) targets.add(new Target(caTarget));

            if(kaTarget instanceof PlayerEntity) targets.add(new Target((PlayerEntity) kaTarget));
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(mc.player == null || mc.world == null || !isTarget(event.getPlayer())) return;

        synchronized (targets) {
            Lyrica.EVENT_HANDLER.post(new TargetDeathEvent(event.getPlayer()));
            targets.remove(getTarget(event.getPlayer()));
        }
    }

    private Target getTarget(PlayerEntity player) {
        for(Target target : targets) {
            if(target.player == player) return target;
        }
        return null;
    }

    public boolean isTarget(PlayerEntity player) {
        for(Target target : targets) {
            if(target.player == player) return true;
        }
        return false;
    }

    private class Target {
        private final PlayerEntity player;
        private final long time;

        public Target(PlayerEntity player) {
            this.player = player;
            this.time = System.currentTimeMillis();
        }
    }
}
