package me.lyrica.modules.impl.combat;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PacketReceiveEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

@RegisterModule(name = "TotemPopCounter", description = "Counts totem pops for each player", category = Module.Category.MISCELLANEOUS)
public class TotemPopCounter extends Module {
    private final Map<String, Integer> popList = new HashMap<>();
    public final BooleanSetting announceToChat = new BooleanSetting("AnnounceToChat", "Announces totem pops in chat", true);
    public final BooleanSetting resetOnDeath = new BooleanSetting("ResetOnDeath", "Resets counter when player dies", true);

    @Override
    public void onEnable() {
        popList.clear();
    }

    @SubscribeEvent
    public void onPacket(PacketReceiveEvent event) {
        if (getNull()) return;
        
        if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 35) {
                Entity entity = packet.getEntity(mc.world);
                if (entity instanceof PlayerEntity) {
                    String name = entity.getName().getString();
                    popList.merge(name, 1, Integer::sum);
                    
                    if (announceToChat.getValue()) {
                        Lyrica.CHAT_MANAGER.message(Formatting.LIGHT_PURPLE + name + Formatting.GRAY + " has popped " + 
                            Formatting.RED + popList.get(name) + Formatting.GRAY + " totem" + 
                            (popList.get(name) == 1 ? "" : "s"), "totem-pop-" + name);
                    }
                }
            } else if (packet.getStatus() == 3 && resetOnDeath.getValue()) {
                Entity entity = packet.getEntity(mc.world);
                if (entity instanceof PlayerEntity) {
                    String name = entity.getName().getString();
                    if (popList.containsKey(name)) {
                        if (announceToChat.getValue()) {
                            Lyrica.CHAT_MANAGER.message(Formatting.LIGHT_PURPLE + name + Formatting.GRAY + " died after popping " + 
                                Formatting.RED + popList.get(name) + Formatting.GRAY + " totem" + 
                                (popList.get(name) == 1 ? "" : "s"), "totem-pop-" + name);
                        }
                        popList.remove(name);
                    }
                }
            }
        }
    }

    @Override
    public String getMetaData() {
        return String.valueOf(popList.size());
    }
} 