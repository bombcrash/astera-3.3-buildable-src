package me.lyrica.modules.impl.combat;

import me.lyrica.events.SubscribeEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.BooleanSetting;
import me.lyrica.settings.impl.NumberSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

@RegisterModule(name = "ChatMusic", description = "Sends custom messages after eliminating players", category = Module.Category.MISCELLANEOUS)
public class ChatMusic extends Module {
    private final NumberSetting messageDelay = new NumberSetting("TPS", "Delay between messages (ms)", "500", 0.0, 2000.0, 50.0);
    private final BooleanSetting addSignature = new BooleanSetting("Signature", "Add signature to messages", true);
    
    private final String[] messages = {
        "I'm a fuckin' kamikaze crashing into everything",
        "You beat me, Islamic Nazi, that means there is no such thing",
        "I've been goin' for your jugular since Craig G, Duck Alert",
        "Wedgie in my underwear, the whole bedsheet and the comforter"
    };
    
    private int currentMessageIndex = 0;
    private long lastMessageTime = 0;

    @SubscribeEvent
    public void onPlayerDeath(PlayerEntity target, DamageSource source) {
        if (!isToggled() || getNull()) return;
        
        if (source.getAttacker() == mc.player && target != mc.player) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMessageTime >= messageDelay.getValue().doubleValue()) {
                String message = messages[currentMessageIndex];
                if (addSignature.getValue()) {
                    message += " ⋆ ᴀsᴛᴇʀᴀ";
                }
                
                mc.player.sendMessage(Text.literal("/msg " + target.getName().getString() + " " + message), false);
                
                currentMessageIndex = (currentMessageIndex + 1) % messages.length;
                lastMessageTime = currentTime;
            }
        }
    }
} 