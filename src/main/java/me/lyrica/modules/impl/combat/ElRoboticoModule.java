package me.lyrica.modules.impl.combat;

import me.lyrica.Lyrica;
import me.lyrica.events.SubscribeEvent;
import me.lyrica.events.impl.PlayerUpdateEvent;
import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.*;
import me.lyrica.modules.impl.combat.BlockerModule;
import me.lyrica.modules.impl.combat.AutoCrystalModule;
import me.lyrica.utils.minecraft.EntityUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.network.PlayerListEntry;

@RegisterModule(name = "ShittyTinkoModule", description = "Automatically enables certain modules based on conditions.", category = Module.Category.COMBAT)
public class ElRoboticoModule extends Module {
    
    public CategorySetting blockerCategory = new CategorySetting("Blocker", "Settings for automatic Blocker activation");
    public BooleanSetting blockerEnabled = new BooleanSetting("Enabled", "Enables automatic Blocker activation", new CategorySetting.Visibility(blockerCategory), true);
    public NumberSetting pingThreshold = new NumberSetting("PingThreshold", "Activates Blocker when target's ping is below this value", new CategorySetting.Visibility(blockerCategory), 30, 0, 1000);
    public BooleanSetting debug = new BooleanSetting("Debug", "Shows target's ping in chat", new CategorySetting.Visibility(blockerCategory), false);

    private boolean wasBlockerEnabled = false;
    private PlayerEntity lastTarget = null;

    @SubscribeEvent
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        BlockerModule blockerModule = Lyrica.MODULE_MANAGER.getModule(BlockerModule.class);
        if (blockerModule == null) return;

        // Check if ElRobotico's Blocker feature is enabled
        if (!blockerEnabled.getValue()) {
            // If Blocker was enabled by us, disable it
            if (wasBlockerEnabled && blockerModule.isToggled()) {
                blockerModule.setToggled(false);
                wasBlockerEnabled = false;
            }
            return;
        }

        // Get current target from AutoCrystal or TargetManager
        PlayerEntity target = Lyrica.MODULE_MANAGER.getModule(AutoCrystalModule.class).getTarget();
        if (target == null) {
            if (wasBlockerEnabled && blockerModule.isToggled()) {
                blockerModule.setToggled(false);
                wasBlockerEnabled = false;
            }
            lastTarget = null;
            return;
        }

        // Get target's ping from PlayerListEntry
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(target.getUuid());
        if (entry == null) return;
        
        int targetPing = entry.getLatency();

        // Debug message for target's ping
        if (debug.getValue() && (lastTarget != target || lastTarget == null)) {
            Lyrica.CHAT_MANAGER.message("Target: " + target.getName().getString() + " Ping: " + targetPing + "ms", "elrobotico-debug");
            lastTarget = target;
        }

        // Enable/Disable Blocker based on target's ping
        if (targetPing <= pingThreshold.getValue().intValue()) {
            if (!blockerModule.isToggled()) {
                blockerModule.setToggled(true);
                wasBlockerEnabled = true;
            }
        } else {
            if (wasBlockerEnabled && blockerModule.isToggled()) {
                blockerModule.setToggled(false);
                wasBlockerEnabled = false;
            }
        }
    }

    @Override
    public void onDisable() {
        // Disable Blocker if it was enabled by us
        if (wasBlockerEnabled) {
            BlockerModule blockerModule = Lyrica.MODULE_MANAGER.getModule(BlockerModule.class);
            if (blockerModule != null && blockerModule.isToggled()) {
                blockerModule.setToggled(false);
            }
            wasBlockerEnabled = false;
        }
        lastTarget = null;
    }
} 