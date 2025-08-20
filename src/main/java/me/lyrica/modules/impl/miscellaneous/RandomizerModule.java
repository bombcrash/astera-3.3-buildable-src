package me.lyrica.modules.impl.miscellaneous;

import me.lyrica.modules.Module;
import me.lyrica.modules.RegisterModule;
import me.lyrica.settings.impl.NumberSetting;
import me.lyrica.utils.minecraft.InventoryUtils;

@RegisterModule(name = "Randomizer", description = "Randomly switches between hotbar slots.", category = Module.Category.MISCELLANEOUS)
public class RandomizerModule extends Module {
    private int currentSlot = 0;
    private boolean running = false;
    private Thread randomizerThread;

    public NumberSetting delay = new NumberSetting("TPS", "The delay between slot switches in ticks.", 100, 1, 1000);

    @Override
    public void onEnable() {
        running = true;
        randomizerThread = new Thread(this::run);
        randomizerThread.start();
    }

    @Override
    public void onDisable() {
        running = false;
        if (randomizerThread != null) {
            randomizerThread.interrupt();
            randomizerThread = null;
        }
    }

    private void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            if (getNull()) continue;
            
            InventoryUtils.switchSlot("Normal", currentSlot, mc.player.getInventory().selectedSlot);
            currentSlot = (currentSlot + 1) % 9;
            
            try {
                Thread.sleep(delay.getValue().longValue() * 50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
} 